package lcc.editorx.frame.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import lcc.utils.JdbcHelp;

/**
 * 数据库连接记录
 * @author lcc
 *
 */
public class DatabaseRecord extends Base {
	
	/**
	 * 获取数据库记录
	 * @param type 如果为null,则返回所有记录，否则仅返回当前type下的记录
	 * @return
	 */
	public static List<JdbcHelp.Database> getRecords(JdbcHelp.EDatabase type) {
		Connection conn = getConnection();
		if (conn == null) {
			return null;
		}
		
		List<JdbcHelp.Database> records = new ArrayList<JdbcHelp.Database>();
		try {
			String sql = "SELECT TYPE,IP,PORT,INSTANCE,ACCOUNT,PASSWORD FROM T_CONFIG_RECORD_DATABASE";
			if (type == null) {
				sql += " ORDER BY TYPE ASC,CREATE_TIME DESC";
			} else {
				sql += " WHERE TYPE='"+type.toString()+"'";
				sql += " ORDER BY CREATE_TIME DESC";
			}
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				JdbcHelp.Database info = new JdbcHelp.Database();
				info.type = JdbcHelp.EDatabase.get(rs.getString(1));
				info.ip = rs.getString(2);
				info.port = rs.getString(3);
				info.instance = rs.getString(4);
				info.account = rs.getString(5);
				info.password = rs.getString(6);
				records.add(info);
			}
			return records;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	//记录
	public static boolean addRecord(JdbcHelp.Database info) {
		if (info == null || info.type == null || "".equals(info.ip) || "".equals(info.port)) {
			return true;
		}
		
		Connection conn = getConnection();
		if (conn == null) {
			return false;
		}
		
		boolean tableExisted = true;
		String sql;
		Statement stat = null;
		
		try {
			stat = conn.createStatement();
			sql = String.format("update T_CONFIG_RECORD_DATABASE set CREATE_TIME=datetime(CURRENT_TIMESTAMP,'localtime'),instance='%s',account='%s',password='%s' where type='%s' and ip='%s' and port='%s'"
					,info.instance,info.account,info.password, info.type.toString(),info.ip,info.port);
			if (stat.executeUpdate(sql) > 0) {
				return true;
			}
		} catch (Exception e) {
			//执行时抛异常，说明尚未创建表
			tableExisted = false;
		}
		
		try {
			if (!tableExisted) {//如果尚未创建数据表，则创建之
				sql = "CREATE TABLE T_CONFIG_RECORD_DATABASE(TYPE STRING(50) NOT NULL,IP STRING(50),PORT STRING(10),INSTANCE STRING(10),ACCOUNT STRING(50),PASSWORD STRING(50),CREATE_TIME TIME NOT NULL)";
				stat.execute(sql);
			}
			
			//插入记录
			sql = String.format("insert into T_CONFIG_RECORD_DATABASE(TYPE,IP,PORT,INSTANCE,ACCOUNT,PASSWORD,CREATE_TIME) VALUES('%s','%s','%s','%s','%s','%s',datetime(CURRENT_TIMESTAMP,'localtime'))"
					, info.type.toString(),info.ip,info.port,info.instance,info.account,info.password);
			stat.executeUpdate(sql);
			
			//限制每种数据库最多仅保存5条记录
			sql = "delete from T_CONFIG_RECORD_DATABASE where type='"+info.type.toString()+"' and create_time not in(select create_time from T_CONFIG_RECORD_DATABASE where type='"+info.type.toString()+"' order by create_time desc limit 5)";
			stat.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
		
		return true;
	}
}
