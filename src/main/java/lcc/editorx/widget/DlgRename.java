package lcc.editorx.widget;

import java.io.File;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ImageManager;

public class DlgRename extends Dialog {

	private DlgRename me = null;
	
	private Text folderPath = null;
	//文件名称前缀
	private Text namePrefix = null;
	//文件后缀名
	private Text nameSuffix = null;
	//文件名流水号的起始数字
	private Text startNumber = null;
	
	public DlgRename() {
		super(AppFrame.getInstance().getShell());
		this.me = this;
	}

	/**
	 * 设置标题栏名称与图标
	 */
	@Override  
	protected void configureShell(Shell newShell) { 
	    super.configureShell(newShell);  
	    newShell.setText("文件名称批量修改");
	    if (ImageManager.getInstance().get("EditorX.ico") != null) {
	    	newShell.setImage(ImageManager.getInstance().get("EditorX.ico"));
		}
	} 
	
	/**
	 * 设置窗体大小
	 */
	@Override  
	protected Point getInitialSize() { 
	    return lcc.utils.swt.Util.zoomForHignGDI(new Point(560,180));  
	}
	
	/**
	 * 实现非模式对话框
	 */
	@Override  
	protected int getShellStyle() {		
		return SWT.MODELESS | SWT.CLOSE | SWT.MIN;
	}
	
	/**
	 * 重写父类方法的意图时，将“ok”，“cancel”改为“确认”、“取消”
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	    Button btn = createButton(parent, 2, "重命名", false);
	    btn.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				String folder = me.folderPath.getText();
				String prefix = me.namePrefix.getText(); prefix = prefix == null ? "" : prefix.trim();
				String strStart = me.startNumber.getText(); strStart = strStart == null ? "" : strStart.trim();
				int start = 0;
				
				if (folder == null || "".equals(folder)) {
					AppFrame.messageBox("请选择要批量处理的文件夹");
					return ;
				}
				if ("".equals(strStart)) {
					AppFrame.messageBox("请输入起始流水号");
					return ;
				}
				try {
					start = Integer.parseInt(strStart);
				} catch (Exception e) {
					AppFrame.messageBox("起始流水号无效");
					return ;
				}
				if (start > 2000) {
					AppFrame.messageBox("起始流水号不可超过2000");
					return ;
				}
				
				if (!AppFrame.confirm("确认重命名？")) {
					return ;
				}
				int n = me.rename(folder, prefix, start);
				if (n < 0) {
					AppFrame.messageBox("重命名失败");
				} else if (n > 0){
					AppFrame.messageBox(n+"个文件重命名失败");
					return ;
				} else {
					AppFrame.messageBox("重命名成功");
				}
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
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("文件夹");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(20)
				, lcc.utils.swt.Util.zoomForHignGDI(60), lcc.utils.swt.Util.zoomForHignGDI(20));
		
		this.folderPath = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.folderPath.setBounds(lcc.utils.swt.Util.zoomForHignGDI(80), lcc.utils.swt.Util.zoomForHignGDI(20)
				, lcc.utils.swt.Util.zoomForHignGDI(420), lcc.utils.swt.Util.zoomForHignGDI(20));
		
		//文件夹选择按钮
		Button btn = new Button(composite, SWT.NONE);
		btn.setText("...");
		btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(500), lcc.utils.swt.Util.zoomForHignGDI(20)
				, lcc.utils.swt.Util.zoomForHignGDI(40), lcc.utils.swt.Util.zoomForHignGDI(20));
		btn.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DirectoryDialog dlg = new DirectoryDialog(AppFrame.getInstance().getShell());
				dlg.setFilterPath(me.folderPath.getText());
				String path = dlg.open();
				if (path == null || "".equals(path)) {
					me.folderPath.setText("");
				} else {
					me.folderPath.setText(path);
				}
			}
		});
		
		label = new Label(composite, SWT.NONE);
		label.setText("文件名前缀");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,50,60,20));
		this.namePrefix = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.namePrefix.setBounds(lcc.utils.swt.Util.zoomForHignGDI(80,50,100,20));
		
		label = new Label(composite, SWT.NONE);
		label.setText("文件后缀名");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(190,50,60,20));
		this.nameSuffix = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.nameSuffix.setBounds(lcc.utils.swt.Util.zoomForHignGDI(255,50,50,20));
		
		label = new Label(composite, SWT.NONE);
		label.setText("起始流水号");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(315,50,60,20));
		this.startNumber = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.startNumber.setBounds(lcc.utils.swt.Util.zoomForHignGDI(380,50,60,20));
		//输入检验，仅接受正整数
		this.startNumber.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				if (!"".equals(e.text)) {
					for (int i=e.text.length()-1; i>-1; i--) {
						char c = e.text.charAt(i);
						if (c < '0' || '9' < c) {
							e.doit = false;
							return ;
						}
					}
				}
			}			
		});
				
		
	    applyDialogFont(composite);
	    return composite;
    }
	
	/**
	 * 批量修改文件名
	 * @param folder 文件所在的文件夹
	 * @param prefix 文件名前缀，可以为空
	 * @param start  起始流水号
	 * @return 修改失败的文件数量
	 */
	private int rename(String folder,String prefix,int start) {
		int errcount = 0;
		File file;
		
		try {
			file = new File(folder);
		} catch (Exception e) {
			AppFrame.messageBox("读取文件夹时遭遇异常");
			return -1;
		}
		if (!file.exists()) {
			AppFrame.messageBox("文件夹不存在");
			return 0;
		}
		
		if (prefix == null) {
			prefix = "";
		}
			
		int serial = start;
		File[] files = file.listFiles();
		for (File f: files) {
			if (!f.isFile()) {
				continue;
			}
			
			try {
				if (!this.renameFile(f, prefix, serial++)) {
					errcount++;
				}
			} catch (Exception e) {
				errcount++;
			}
		}
			
		return errcount;
	}
	
	private boolean renameFile(File file,String prefix,int serial) {
		String old = file.getPath().replace('\\', '/');
		String oldname = old.substring(old.lastIndexOf("/")+1);
		String oldfolder = old.substring(0,old.lastIndexOf("/"));
		String suffix = nameSuffix.getText().trim();
		
		//序列号为4位
		String newname = prefix+String.format("%04d", serial);
		String newpath;
		
		if (!"".equals(suffix)) {
			newname += '.'+suffix;
		} else {
			int index = oldname.lastIndexOf('.');
			if (-1 < index && index <oldname.length()-1) {
				newname += '.'+oldname.substring(index+1);
			}
		}		
		newpath = oldfolder+'/'+newname;
		
		File newfile = new File(newpath);
		//新文件已存在，则放弃执行
		if (newfile.exists()) {
			return false;
		}
		return file.renameTo(newfile);
	}
}
