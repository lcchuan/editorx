package lcc.editorx.frame.config;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 配置管理，利用sqlite数据库
 * @author lcc
 *
 */
public class Base {
	/**
	 * 获取配置数据库的链接
	 * @return
	 */
	public static Connection getConnection() {		
		Connection conn = null;
		try {
			//获取应用的运行目录
			final String dirApp = System.getProperty("user.dir").replace('\\','/');
			
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite://"+dirApp+"/data");
		} catch (Exception e) {
			e.printStackTrace();
			conn = null;
		}
		return conn;
	}
}
