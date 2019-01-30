package lcc.editorx.frame.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import lcc.editorx.editor.TextEditorE;
import lcc.editorx.frame.AppFrame;

/**
 * 编辑器中的临时内容保存，防止编辑器突然崩溃后，内容丢失
 * @author lcc
 *
 */
public class TempContentRecord extends Base {
	
	//需要保存临时内容的编辑器,Vector线程安全
	protected Vector<TextEditorE> lst_editor;
	protected Thread save_thread = null;
	
	//单例模式
	protected static TempContentRecord instance = null;
	private TempContentRecord(){
		this.lst_editor = new Vector<TextEditorE>();
	};
	public static TempContentRecord getInstance() {
		if (instance == null) {
			instance = new TempContentRecord();
		}
		return instance;
	}
	
	public void addEditorForSave(TextEditorE editor) {
		//开一个保存临时内容的线程
		if (this.save_thread == null) {
			Runnable runnable = new SaveThread(this);
			this.save_thread = new Thread(runnable);
			this.save_thread.start();
		}
				
		final int id = editor.hashCode();
		for (int i=this.lst_editor.size()-1; i>-1; i--) {
			if (this.lst_editor.get(i).hashCode() == id) {
				return;
			}
		}
		this.lst_editor.add(editor);
	}
	
	protected TextEditorE removeEditor(TextEditorE editor) {
		final int id = editor.hashCode();
		for (int i=this.lst_editor.size()-1; i>-1; i--) {
			if (this.lst_editor.get(i).hashCode() == id) {
				return this.lst_editor.remove(i);
			}
		}
		return null;
	}
	
	/**
	 * 编辑器打开后调用该函数已打开之前的临时文本记录
	 * @return 如果有临时记录且成功打开则返回true,否则返回false
	 */
	public boolean showRecords() {
		Connection conn = getConnection();
		if (conn == null) {
			return false;
		}
		
		AppFrame frame = AppFrame.getInstance();
		try {
			List<String> sqls = new ArrayList<String>();
			String sql = "SELECT id,content FROM T_CONFIG_RECORD_TEMPCONTENT ORDER BY CREATE_TIME";
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			while (rs.next()) {
				//10000保证新建的TAB页放到最后
				TextEditorE editor = frame.createEditor(10000,"history temp");
				if (editor == null) {
					return false;
				}
				editor.setText(rs.getString(2));
				
				//将id更新为当前编辑器的id
				sql = String.format("update T_CONFIG_RECORD_TEMPCONTENT set id=%d where id=%d", editor.hashCode(),rs.getInt(1));
				sqls.add(sql);
			}
			if (sqls.size() > 0) {
				for (int i=0; i<sqls.size(); i++) {
					stat.addBatch(sqls.get(i));
				}
				stat.executeBatch();
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			AppFrame.getInstance().console(e.getMessage());
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
	
	public boolean deleteContent(TextEditorE editor) {
		if (editor == null) {
			return true;
		}
		removeEditor(editor);
		final int id = editor.hashCode();
		
		Connection conn = getConnection();
		if (conn == null) {
			return false;
		}
		
		try {
			Statement stat = conn.createStatement();
			String sql = String.format("delete from T_CONFIG_RECORD_TEMPCONTENT where id=%d", id);
			stat.executeUpdate(sql);
		} catch (Exception e) {
			AppFrame.getInstance().console(e.getMessage());
		}
		return true;
	}

	//保存内容
	protected boolean saveContent(TextEditorE editor) {
		if (editor == null) {
			return true;
		}
		
		final int id = editor.hashCode();
		final String content = editor.getText().trim();
		
		Connection conn = getConnection();
		if (conn == null) {
			return false;
		}
		
		boolean tableExisted = true;
		String sql;
		PreparedStatement pstmt = null;
		
		try {
			if ("".equals(content)) {
				sql = "delete from T_CONFIG_RECORD_TEMPCONTENT where id=?";
			} else {
				sql = "update T_CONFIG_RECORD_TEMPCONTENT set CONTENT=? where id=?";
			}
			pstmt = conn.prepareStatement(sql);
			if ("".equals(content)) {
				pstmt.setInt(1, id);
			} else {
				pstmt.setString(1, content);
				pstmt.setInt(2, id);
			}
			
			if (pstmt.executeUpdate() > 0 || "".equals(content)) {
				return true;
			}
			
		} catch (Exception e) {
			//执行时抛异常，说明尚未创建表
			tableExisted = false;
		}
		
		try {
			if (!tableExisted) {//如果尚未创建数据表，则创建之
				sql = "CREATE TABLE T_CONFIG_RECORD_TEMPCONTENT(ID INT PRIMARY KEY UNIQUE NOT NULL,CONTENT TEXT,CREATE_TIME TIME NOT NULL)";
				pstmt = conn.prepareStatement(sql);
				pstmt.executeUpdate();
				
				if ("".equals(content)) {
					sql = "delete from T_CONFIG_RECORD_TEMPCONTENT where id=?";
					pstmt = conn.prepareStatement(sql);
					pstmt.setInt(1, id);
					pstmt.executeUpdate();
					return true;
				}
			}
			
			//插入记录
			sql = "insert into T_CONFIG_RECORD_TEMPCONTENT(id,content,CREATE_TIME) VALUES(?,?,datetime(CURRENT_TIMESTAMP,'localtime'))";
			pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, id);
			pstmt.setString(2, content);
			pstmt.executeUpdate();
		} catch (Exception e) {
			AppFrame.getInstance().console(e.getMessage());
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					AppFrame.getInstance().console(e.getMessage());
				}
			}
		}
		
		return true;
	}

	//新开一个保存临时内容的线程
	protected class SaveThread implements Runnable {
		TempContentRecord owner = null;
		
		public SaveThread(TempContentRecord owner) {
			this.owner = owner;
		}
	
		@Override
		public void run() {
			while (true) {
				try {
					Thread.sleep(3000);
				} catch (Exception e) {
				}
				for (int i=this.owner.lst_editor.size()-1; i>-1; i--) {
					if (this.owner.saveContent(this.owner.lst_editor.get(i))) {
						this.owner.removeEditor(this.owner.lst_editor.get(i));
					}
				}
			}
		}
	}
}
