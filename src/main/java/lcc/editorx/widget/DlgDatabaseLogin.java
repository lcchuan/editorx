package lcc.editorx.widget;

import java.sql.Connection;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ImageManager;
import lcc.editorx.frame.config.DatabaseRecord;
import lcc.utils.JdbcHelp;
import lcc.utils.JdbcHelp.Database;
import lcc.utils.JdbcHelp.FIELD;

/**
 * 数据库登陆框
 * @author lcc
 *
 */
public class DlgDatabaseLogin extends Dialog {	
	//数据库类型下拉框
	private Combo cmbDbType = null;
	//数据库IP地址
	private Text txtIp = null;
	//数据库端口
	private Text txtPort = null;
	//数据库登陆账号
	private Text txtAccount = null;
	//数据库登陆密码
	private Text txtPassword = null;
	//数据库实例名
	private Text txtInstance = null;
	
	//数据表名,for生成javaPo类功能
	private Text txtTable = null;
	private boolean forJavaPo = false;
	
	
	private Database dbInfo = null;
	private String table = null;
	private List<FIELD> fields = null;
	
	public DlgDatabaseLogin() {
		super(AppFrame.getInstance().getShell());
	}
	
	public DlgDatabaseLogin(boolean forJavaPo) {
		super(AppFrame.getInstance().getShell());
		this.forJavaPo = forJavaPo;
	}

	
	/**
	 * 设置标题栏名称与图标
	 */
	@Override  
	protected void configureShell(Shell newShell) { 
	    super.configureShell(newShell);  
	    newShell.setText("数据库");
	    if (ImageManager.getInstance().get("EditorX.ico") != null) {
	    	newShell.setImage(ImageManager.getInstance().get("EditorX.ico"));
		}
	} 
	
	/**
	 * 设置窗体大小
	 */
	@Override  
	protected Point getInitialSize() { 
	    return new Point(lcc.utils.swt.Util.zoomForHignGDI(300),lcc.utils.swt.Util.zoomForHignGDI(250));  
	}
	
	/**
	 * 重写父类方法的意图时，将“ok”，“cancel”改为“确认”、“取消”
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		String btnName = this.forJavaPo ? "确定" : "登陆";
		Button btn = createButton(parent, 2, btnName, false);
		btn.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onOkClick();
			}
		});
		
	    createButton(parent, 0, "退出",true);
	}
	
	/**
	 * 创建对话框内容
	 */
	@Override
	protected Control createDialogArea(Composite parent) {				
		Composite composite = new Composite(parent, 0);
		
		//数据库类型下拉框
		Label label = new Label(composite, SWT.NONE);
		label.setText("数据库类型");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,5,60,20));
		this.cmbDbType = new Combo(composite,SWT.READ_ONLY);
		this.cmbDbType.setBounds(lcc.utils.swt.Util.zoomForHignGDI(75,5,200,20));
		JdbcHelp.EDatabase[] items = JdbcHelp.EDatabase.values();
		for (int i=0; i<items.length; i++) {
			this.cmbDbType.add(items[i].toString());
		}
		this.cmbDbType.select(0);
		this.cmbDbType.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				final JdbcHelp.EDatabase type= JdbcHelp.EDatabase.get(((Combo)e.widget).getText());
				List<Database> infos = DatabaseRecord.getRecords(type);
				if (infos == null || infos.size() < 1) {
					if ("".equals(txtIp.getText().trim())) {
						//设置默认端口
						String port = "";
						if (type.equals(JdbcHelp.EDatabase.Oracle)) {
							port = "1521";
						} else if (type.equals(JdbcHelp.EDatabase.Mysql)) {
							port = "3306";
						} else if (type.equals(JdbcHelp.EDatabase.DM6) || type.equals(JdbcHelp.EDatabase.DM7)) {
							port = "12345";
						} else if (type.equals(JdbcHelp.EDatabase.Kingbase)) {
							port = "54321";
						}
						txtPort.setText(port);
					}
				} else {
					Database info = infos.get(0);
					txtIp.setText(info.ip);
					txtPort.setText(info.port);
					txtInstance.setText(info.instance);
					txtAccount.setText(info.account);
					txtPassword.setText(info.password);
				}
			}
		});
		
		//数据库IP地址
		label = new Label(composite, SWT.NONE);
		label.setText("数据库IP");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,30,60,20));
		this.txtIp = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.txtIp.setBounds(lcc.utils.swt.Util.zoomForHignGDI(75,30,200,20));
		//输入检验，仅接受正整数与小数点
		this.txtIp.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				if (!"".equals(e.text)) {
					for (int i=e.text.length()-1; i>-1; i--) {
						char c = e.text.charAt(i);
						if ((c < '0' || '9' < c) && '.' != c) {
							e.doit = false;
							return ;
						}
					}
				}
			}			
		});
		
		//数据库端口
		label = new Label(composite, SWT.NONE);
		label.setText("数据库端口");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,55,60,20));
		this.txtPort = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.txtPort.setBounds(lcc.utils.swt.Util.zoomForHignGDI(75,55,200,20));
		//输入检验，仅接受正整数
		this.txtPort.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				if (!"".equals(e.text)) {
					for (int i=e.text.length()-1; i>-1; i--) {
						char c = e.text.charAt(i);
						if (c < '0' || '9' < c || '.' == c) {
							e.doit = false;
							return ;
						}
					}
				}
			}			
		});
		
		//数据库实例名
		label = new Label(composite, SWT.NONE);
		label.setText("数据库实例名");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,80,60,20));
		this.txtInstance = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.txtInstance.setBounds(lcc.utils.swt.Util.zoomForHignGDI(75,80,200,20));
		
		//登陆账户
		label = new Label(composite, SWT.NONE);
		label.setText("登陆账户");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,105,60,20));
		this.txtAccount = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.txtAccount.setBounds(lcc.utils.swt.Util.zoomForHignGDI(75,105,200,20));
		
		//登陆密码
		label = new Label(composite, SWT.NONE);
		label.setText("登陆密码");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,130,60,20));
		this.txtPassword = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.txtPassword.setBounds(lcc.utils.swt.Util.zoomForHignGDI(75,130,200,20));
		
		//数据表名,for生成javaPo类功能
		if (this.forJavaPo) {
			label = new Label(composite, SWT.NONE);
			label.setText("数据表名");
			label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,155,60,20));
			this.txtTable = new Text(composite,SWT.SINGLE|SWT.BORDER);
			this.txtTable.setBounds(lcc.utils.swt.Util.zoomForHignGDI(75,155,200,20));
		}
		
	    applyDialogFont(composite);
	    
	    return composite;
    }
	
	public Database getDbInfo() {
		return this.dbInfo;
	}
	public String getTable() {
		return this.table;
	}
	public List<FIELD> getFields() {
		return this.fields;
	}
	
	private void onOkClick() {
		this.dbInfo = new Database();
		this.dbInfo.type = JdbcHelp.EDatabase.get(this.cmbDbType.getText());
		this.dbInfo.ip = this.txtIp.getText().trim();
		this.dbInfo.port = this.txtPort.getText().trim();
		this.dbInfo.account = this.txtAccount.getText().trim();
		this.dbInfo.instance = this.txtInstance.getText().trim();
		this.dbInfo.password = this.txtPassword.getText().trim();
		this.table = this.forJavaPo ? this.txtTable.getText().trim() : "";
		
		if (this.dbInfo.type == null) {
			AppFrame.messageBox("请先选择数据库类型");
			return ;
		}
		if ("".equals(this.dbInfo.ip)) {
			AppFrame.messageBox("请输入数据库Ip");
			return ;
		}
		if ("".equals(this.dbInfo.port)) {
			AppFrame.messageBox("请输入数据库端口");
			return ;
		}
		if ("".equals(this.dbInfo.account)) {
			AppFrame.messageBox("请输入登陆账户");
			return ;
		}
		if ("".equals(this.dbInfo.instance)) {
			AppFrame.messageBox("请输入数据库实例名");
			return ;
		}
		if (this.forJavaPo && "".equals(this.dbInfo.account)) {
			AppFrame.messageBox("请输入数据表名");
			return ;
		}
		
		Connection conn = null;
		try {
			conn = JdbcHelp.getConnection(this.dbInfo);
			if (conn == null) {
				AppFrame.messageBox("数据库连接失败");
				return ;
			}
			
			if (this.forJavaPo) {
				this.fields = JdbcHelp.getFileds(conn, this.dbInfo.type, this.table);
			}
			
			//保存数据库连接信息
			DatabaseRecord.addRecord(this.dbInfo);
			
			this.setReturnCode(SWT.OK);
			this.close();
		} catch (Exception e) {
			e.printStackTrace();
			AppFrame.messageBox("遭遇异常");
		} finally {
			try {
				if (conn != null) {
					conn.close();
				}
			} catch (Exception e) {
			}
		}
	}
}
