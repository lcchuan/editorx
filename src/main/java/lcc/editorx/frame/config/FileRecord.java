package lcc.editorx.frame.config;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件打开记录管理
 * @author lcc
 *
 */
public class FileRecord extends Base {
	/**
	 * 获取已打开的文件记录
	 * @return 已打开文件的绝对路径
	 */
	public static String[] getRecords() {
		Connection conn = getConnection();
		if (conn == null) {
			return null;
		}
		
		List<String> files = new ArrayList<String>();
		try {
			String sql = "SELECT FILE_PATH FROM T_CONFIG_RECORD_FILE ORDER BY CREATE_TIME DESC";
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				files.add(rs.getString(1));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
		return files.toArray(new String[]{});
	}
	
	//删除文件历史记录
	public static boolean deleteRecord(String file) {
		if (file == null || "".equals(file.trim())) {
			return true;
		}
		
		Connection conn = getConnection();
		if (conn == null) {
			return false;
		}
		try {
			Statement stat = conn.createStatement();
			String sql = String.format("delete from T_CONFIG_RECORD_FILE where FILE_PATH='%s'", file);
			stat.executeUpdate(sql);
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
		}
	}
	
	//记录已打开的文件路径
	public static boolean addRecord(String file) {
		if (file == null || "".equals(file.trim())) {
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
			sql = String.format("update T_CONFIG_RECORD_FILE set CREATE_TIME=datetime(CURRENT_TIMESTAMP,'localtime') where FILE_PATH='%s'", file);
			if (stat.executeUpdate(sql) > 0) {
				return true;
			}
		} catch (Exception e) {
			//执行时抛异常，说明尚未创建表
			tableExisted = false;
		}
		
		try {
			if (!tableExisted) {//如果尚未创建数据表，则创建之
				sql = "CREATE TABLE T_CONFIG_RECORD_FILE(FILE_PATH STRING(500) NOT NULL UNIQUE,CREATE_TIME TIME NOT NULL)";
				stat.execute(sql);
			}
			
			//插入文件记录
			sql = String.format("insert into T_CONFIG_RECORD_FILE(FILE_PATH,CREATE_TIME) VALUES('%s',datetime(CURRENT_TIMESTAMP,'localtime'))"
					, file.trim());
			stat.executeUpdate(sql);
			
			//限制最多仅保存20条记录
			sql = "delete from T_CONFIG_RECORD_FILE where create_time not in(select create_time from T_CONFIG_RECORD_FILE order by create_time desc limit 20)";
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
