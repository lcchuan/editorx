package lcc.utils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import lcc.editorx.frame.AppFrame;

public class JdbcHelp {
	public static enum EDatabase {
		Oracle("Oracle"),Mysql("Mysql"),DM7("达梦7"),DM6("达梦6"),Kingbase("金仓");
		private String text;
		private EDatabase(String text) {
			this.text = text;
		}
		public String toString() {
			return this.text;
		}
		public static EDatabase get(String text) {
			EDatabase[] items = EDatabase.values();
			for (int i=0; i<items.length; i++) {
				if (items[i].text.equals(text)) {
					return items[i];
				}
			}
			return null;
		}
	};
	
	public static class Database {
		public EDatabase type;
		public String ip;
		public String port;
		public String account;
		public String password;
		public String instance;
	};
	
	public static class FIELD{
		public String name;//字段名称
		public String type;//字段类型:string,number,datetime
		public String comment; //字段注释
		
		public FIELD(String name,String type,String comment) {
			this.name = name;
			this.type = type;
			this.comment = comment;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static Connection getConnection(Database info) {
		String className = null;
		String jdbcConn = null;
		final String jarName = getJarPath(info.type);
		
		if (jarName == null) {
			return null;
		}
		
		if (EDatabase.Oracle.equals(info.type)) {
			className = "oracle.jdbc.driver.OracleDriver";
			jdbcConn = "jdbc:oracle:thin:@"+info.ip+":"+info.port+":"+info.instance;
		} else if (EDatabase.Mysql.equals(info.type)) {
			className = "com.mysql.jdbc.Driver";
			jdbcConn = "jdbc:mysql://"+info.ip+":"+info.port+"/"+info.instance;
		} else if (EDatabase.DM7.equals(info.type) || EDatabase.DM6.equals(info.type)) {
			className = "dm.jdbc.driver.DmDriver";
			jdbcConn = "jdbc:dm://"+info.ip+":"+info.port+"/"+info.instance;
		} else if (EDatabase.Kingbase.equals(info.type)) {
			className = "com.kingbase.Driver";
			jdbcConn = "jdbc:kingbase://"+info.ip+":"+info.port+"/"+info.instance;
		}
		if (className == null) {
			AppFrame.messageBox("不支持的数据库类型");
			return null;
		}
		
		URLClassLoader classLoader = null;
		try {
			//动态加载数据库的jdbc jar包
			URL url = new URL("file:"+jarName);
			classLoader = new URLClassLoader(new URL[]{url}, Thread.currentThread().getContextClassLoader());
			Class c = classLoader.loadClass(className);
			Driver driver = (Driver)c.newInstance();
			
			//创建数据库连接
			Properties properties = new Properties();  
            properties.setProperty("user",info.account);  
            properties.setProperty("password", info.password);  
			return driver.connect(jdbcConn, properties);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				if (classLoader != null) {
					classLoader.close();
				}
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 获取某张数据表的字段信息
	 * @param tablename
	 * @return
	 */
	public static List<FIELD> getFileds(Connection conn,EDatabase type,String tablename) {
		List<FIELD> fileds = new ArrayList<FIELD>();		
		
		try {
			String schema = "";
			final int index = tablename.indexOf('.');
			if (index > 0) {
				schema = tablename.substring(0, index).toUpperCase();
				tablename = tablename.substring(index+1).toUpperCase();
			}
			
			StringBuilder sql = null;
			if (EDatabase.Oracle.equals(type)) {
				sql = new StringBuilder();
				sql.append("select c.COLUMN_NAME COL_NAME,c.DATA_TYPE COL_TYPE");
				sql.append(",(select comments from all_col_comments cm where cm.TABLE_NAME=c.table_name and cm.OWNER=c.owner and cm.column_name=c.COLUMN_NAME and rownum=1) COL_COMMENT");
				sql.append(",(select comments from all_tab_comments tm where tm.TABLE_NAME=c.table_name and tm.OWNER=c.owner and rownum=1) TABLE_COMMENT");
				sql.append(" from all_tab_columns c");
				sql.append(" where c.TABLE_NAME='{tablename}'");
				if (!"".equals(schema)) {
					sql.append(" AND c.owner='{schema}'");
				}
			} else if (EDatabase.Mysql.equals(type)){
				sql = new StringBuilder();
				sql.append("select c.COLUMN_NAME COL_NAME,c.DATA_TYPE COL_TYPE,C.COLUMN_COMMENT COL_COMMENT,T.TABLE_COMMENT");
				sql.append(" FROM INFORMATION_SCHEMA.COLUMNS c");
				sql.append(" inner join INFORMATION_SCHEMA.tables t");
				sql.append(" on t.table_name=c.table_name and t.table_schema=c.table_schema");
				sql.append(" where c.TABLE_NAME='{tablename}'");
				if (!"".equals(schema)) {
					sql.append(" AND c.table_schema='{schema}'");
				}
			} else if (EDatabase.DM6.equals(type)){
				sql = new StringBuilder();
				//for达梦6  达梦6 用SYSDBA.SYSTABLES可以，但是达梦7下就不行！
				sql.append("SELECT COLS.NAME COL_NAME,COLS.TYPE COL_TYPE,COLS.RESVD5 COL_COMMENT,T.RESVD5 TABLE_COMMENT");
				sql.append(" FROM SYSDBA.SYSTABLES T");
				sql.append(" INNER JOIN SYSDBA.SYSCOLUMNS COLS ON COLS.ID=T.ID");
				sql.append(" INNER JOIN SYSDBA.SYSSCHEMAS SCH ON SCH.SCHID=T.SCHID");
				sql.append(" WHERE T.NAME='{tablename}'");
				if (!"".equals(schema)) {
					sql.append(" AND SCH.NAME='{schema}'");
				}
			} else if (EDatabase.DM7.equals(type)){
				sql = new StringBuilder();
				sql.append("SELECT COLS.NAME COL_NAME,COLS.TYPE$ COL_TYPE");
				sql.append("   , CC.COMMENT$ COL_COMMENT,TC.COMMENT$ TABLE_COMMENT,COLS.LENGTH$ COL_LENGTH");
				sql.append(" FROM SYSOBJECTS T");
				sql.append(" INNER JOIN SYSCOLUMNS COLS ON COLS.ID=T.ID");
				sql.append(" LEFT JOIN SYSCOLUMNCOMMENTS CC ON CC.TVNAME=T.NAME AND COLS.NAME=CC.COLNAME");
				if (!"".equals(schema)) {
					sql.append(" AND CC.SCHNAME='{schema}'");
				}
				sql.append(" LEFT JOIN SYSTABLECOMMENTS TC ON TC.TVNAME=T.NAME AND TC.TABLE_TYPE='TABLE'");
				if (!"".equals(schema)) {
					sql.append(" AND TC.SCHNAME='{schema}'");
				}
				sql.append(" WHERE T.NAME='{tablename}'");
				if (!"".equals(schema)) {
					sql.append(" AND T.SCHID=(SELECT ID FROM SYSOBJECTS WHERE TYPE$='SCH' AND NAME='{schema}')");
				}
			} else if (EDatabase.Kingbase.equals(type)) {//金仓数据库
				sql = new StringBuilder();
				sql.append("select c.COLUMN_NAME COL_NAME,c.DATA_TYPE COL_TYPE");
				//金仓貌似不支持对表及字段的注释
				sql.append(",NULL COL_COMMENT");
				sql.append(",NULL TABLE_COMMENT");
				sql.append(" from all_tab_columns c");
				sql.append(" where c.TABLE_NAME='{tablename}'");
			} 

			if (sql.length() < 1) {
				return null;
			}
			
			String strSql = sql.toString().replace("{tablename}", tablename).replace("{schema}", schema);
			ResultSet rs = conn.createStatement().executeQuery(strSql);
			while (rs.next()) {
				fileds.add(new FIELD(toString(rs.getString(1))
						,toString(rs.getString(2))
						,toString(rs.getString(3))));
			}
			return fileds;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	//动态加载jdbc的mysql jar包时报错： java.lang.ClassNotFoundException: com.mysql.jdbc.ProfilerEventHandlerFactory
	/*public static ResultSet query(Connection conn,String sql,Object[] params) throws SQLException {
		PreparedStatement ps = conn.prepareStatement(sql);
		ParameterMetaData pmd = ps.getParameterMetaData();
		final int count = pmd.getParameterCount();
		for (int i=0; i<count; i++) {
			ps.setObject(i+1, params[i]);
		}		
		return ps.executeQuery();
	}*/
	
	private static String toString(Object obj) {
		return obj == null ? "" : obj.toString();
	}
	
	private static String getJarPath(EDatabase type) {
		String path = System.getProperty("user.dir").replace('\\','/')+"/lib";
		File folder = new File(path);
		
		if (!folder.exists() || !folder.isDirectory()) {
			AppFrame.messageBox("获取jdbc jar包失败，因为找不到文件夹【"+path+"】");
			return null;
		}
		
		//遍历文件
		File[] files = folder.listFiles();
		for (File f: files) {
			if (!f.isFile()) {
				continue;
			}
			String name = f.getName().toLowerCase();
			if (!name.endsWith(".jar")) {
				continue;
			}
			
			if (EDatabase.Oracle.equals(type)) {
				if (name.startsWith("ojdbc")) {
					return path+"/"+f.getName();
				}
			} else if (EDatabase.Mysql.equals(type)) {
				if (name.startsWith("mysql")) {
					return path+"/"+f.getName();
				}
			} else if (EDatabase.DM7.equals(type) || EDatabase.DM6.equals(type)) {
				if (name.startsWith("dmjdbc")) {
					return path+"/"+f.getName();
				}
			} else if (EDatabase.Kingbase.equals(type)) {
				if (name.startsWith("kingbase")) {
					return path+"/"+f.getName();
				}
			}
		}
		
		AppFrame.messageBox("在文件夹【"+path+"】下找不到当前数据库的jdbc驱动");
		return null;
	}
}
