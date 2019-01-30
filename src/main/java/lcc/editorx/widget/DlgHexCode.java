package lcc.editorx.widget;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import lcc.editorx.editor.TextEditorE;
import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ImageManager;

/**
 * 提供进制转换以及字符编码查看功能
 * @author lcc
 *
 */
public class DlgHexCode extends Dialog {
	//进制转换功能
	private Text decimal = null;
	private Text hex = null;
	private Text binary = null;
	private ModifyListener convertModifyListener = null;
	
	//字符编码查看功能
	private Text text = null;
	private List<Button> lstBtnCode = null;
	private Text hexcode = null;
	
	public DlgHexCode() {
		super(AppFrame.getInstance().getShell());
		
		//进制转换响应事件
		this.convertModifyListener = new ModifyListener() {
			private boolean response = true;
			
			@Override
			public void modifyText(ModifyEvent e) {
				if (!response) {
					return ;
				}
				
				this.response = false;
				String content = ((Text)e.widget).getText();
				if (e.widget == decimal) {
					if ("".equals(content)) {
						hex.setText("");
						binary.setText("");
					} else {
						hex.setText(Integer.toHexString(Integer.valueOf(content)));
						binary.setText(Integer.toBinaryString(Integer.valueOf(content)));
					}
				} else if (e.widget == hex) {
					if ("".equals(content) || "0X".equals(content.toUpperCase())) {
						decimal.setText("");
						binary.setText("");
					} else {
						String strHex = content.toUpperCase().replace("0X", "");
						decimal.setText(String.valueOf(Integer.parseInt(strHex,16)));
						binary.setText(Integer.toBinaryString(Integer.valueOf(strHex,16)));						
					}
				} else if (e.widget == binary) {
					if ("".equals(content)) {
						decimal.setText("");
						hex.setText("");
					} else {
						decimal.setText(String.valueOf(Integer.parseInt(content,2)));
						hex.setText(Integer.toHexString(Integer.parseInt(content,2)));
					}
				}
				this.response = true;
			}
		};
	}
	
	/**
	 * 设置标题栏名称与图标
	 */
	@Override  
	protected void configureShell(Shell newShell) { 
	    super.configureShell(newShell);  
	    newShell.setText("进制转换与字符编码查看");
	    if (ImageManager.getInstance().get("EditorX.ico") != null) {
	    	newShell.setImage(ImageManager.getInstance().get("EditorX.ico"));
		}
	} 
	
	/**
	 * 设置窗体大小
	 */
	@Override  
	protected Point getInitialSize() { 
	    return new Point(lcc.utils.swt.Util.zoomForHignGDI(620),lcc.utils.swt.Util.zoomForHignGDI(230));  
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
	    createButton(parent, 0, "退出",true);
	    //createButton(parent, 1, "取消", false);
	}
	
	/**
	 * 创建对话框内容
	 */
	@Override
	protected Control createDialogArea(Composite parent) {				
		Composite composite = new Composite(parent, 0);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		//进制转换
		Composite compositeLeft = new Composite(composite, 0);
		createHexConvert(compositeLeft);
		
		//字符编码
		Composite compositeRight = new Composite(composite, 0);
		createCharCode(compositeRight);
		
	    applyDialogFont(composite);
	    return composite;
    }
	
	//字符编码
	private void createCharCode(Composite composite) {
		//分组
		Group group = new Group(composite,SWT.SHADOW_NONE);
		group.setText("字符编码");
		group.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(10)
				, lcc.utils.swt.Util.zoomForHignGDI(290), lcc.utils.swt.Util.zoomForHignGDI(130));
		
		Label label = new Label(group, SWT.NONE);
		label.setText("文本");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(20)
				, lcc.utils.swt.Util.zoomForHignGDI(60), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.text = new Text(group,SWT.SINGLE|SWT.BORDER);
		this.text.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(20)
				, lcc.utils.swt.Util.zoomForHignGDI(200), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.text.addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent arg0) {
				getCharHexCode();
			}
		});
		
		this.lstBtnCode = new ArrayList<Button>();
		String[] encodes = TextEditorE.getSupportedCharset();
		int x = 10;
		for (int i=0; i<encodes.length; i++) {
			int width = 80;
			Button btn = new Button(group,SWT.RADIO);
			btn.setText(encodes[i]);
			if (encodes[i].length() < 4) {
				width = 45;
			} else if (encodes[i].length() < 6) {
				width = 60;
			}
			btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(x,50,width,20));
			btn.addSelectionListener(new SelectionListener(){	
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}

				@Override
				public void widgetSelected(SelectionEvent arg0) {
					getCharHexCode();
				}
			});
			if (i == 0) {
				btn.setSelection(true);
			}
			this.lstBtnCode.add(btn);
			x += width;
		}
		
		label = new Label(group, SWT.NONE);
		label.setText("hex");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(80)
				, lcc.utils.swt.Util.zoomForHignGDI(60), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.hexcode = new Text(group,SWT.SINGLE|SWT.BORDER|SWT.READ_ONLY);
		this.hexcode.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(80)
				, lcc.utils.swt.Util.zoomForHignGDI(200), lcc.utils.swt.Util.zoomForHignGDI(20));
	}
	
	private String getCharHexCode() {
		final String str = this.text.getText();		
		if ("".equals(str)) {
			this.hexcode.setText("");
			return "";
		}
		
		String charset = "";
		for (int i=0; i<this.lstBtnCode.size(); i++) {
			if (this.lstBtnCode.get(i).getSelection()) {
				charset = this.lstBtnCode.get(i).getText();
				break;
			}
		}
		if ("".equals(charset)) {
			AppFrame.messageBox("请选择编码");
			return "";
		}
		
		String hex;
		byte[] bytes = null;
		try {
			bytes = str.getBytes(charset);
			hex = "";
			for (int i=0 ;i<bytes.length; i++) {
				if (i > 0) {
					hex += ' ';
				}
			
				//将字节转换为正整数
				int c = bytes[i] & 0x000000FF;
				hex += Integer.toHexString(c).toUpperCase();
			}
		} catch (Exception e) {
			hex = "遭遇异常";
		}
		this.hexcode.setText(hex);
		return hex;
	}
	
	//进制转换
	private void createHexConvert(Composite composite) {
		//分组
		Group group = new Group(composite,SWT.SHADOW_NONE);
		group.setText("进制转换");
		group.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(10)
				, lcc.utils.swt.Util.zoomForHignGDI(290), lcc.utils.swt.Util.zoomForHignGDI(130));
		
		Label label = new Label(group, SWT.NONE);
		label.setText("decimal");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(20)
				, lcc.utils.swt.Util.zoomForHignGDI(60), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.decimal = new Text(group,SWT.SINGLE|SWT.BORDER);
		this.decimal.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(20)
				, lcc.utils.swt.Util.zoomForHignGDI(200), lcc.utils.swt.Util.zoomForHignGDI(20));
		//内容更改事件
		this.decimal.addModifyListener(this.convertModifyListener);
		//输入检验，仅接受正整数
		this.decimal.addVerifyListener(new VerifyListener(){
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
		
		label = new Label(group, SWT.NONE);
		label.setText("hex");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(50)
				, lcc.utils.swt.Util.zoomForHignGDI(60), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.hex = new Text(group,SWT.SINGLE|SWT.BORDER);
		this.hex.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(50)
				, lcc.utils.swt.Util.zoomForHignGDI(200), lcc.utils.swt.Util.zoomForHignGDI(20));
		//内容更改事件
		this.hex.addModifyListener(this.convertModifyListener);
		//输入检验
		this.hex.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				if (!"".equals(e.text)) {
					String str = replaceString(((Text)e.widget).getText(),e.text,e.start,e.end);
					if (!"0x".equals(str.toLowerCase())) {
						if (str.toLowerCase().startsWith("0x")) {
							str = str.substring(2);
						}
						try {
							Integer.parseInt(str,16);
						} catch (Exception ep) {
							e.doit = false;
							return ;
						}
					}
				}
			}			
		});
		
		label = new Label(group, SWT.NONE);
		label.setText("binary");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(80)
				, lcc.utils.swt.Util.zoomForHignGDI(60), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.binary = new Text(group,SWT.SINGLE|SWT.BORDER);
		this.binary.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(80)
				, lcc.utils.swt.Util.zoomForHignGDI(200), lcc.utils.swt.Util.zoomForHignGDI(20));
		//内容更改事件
		this.binary.addModifyListener(this.convertModifyListener);
		//输入检验，仅接受正整数
		this.binary.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				if (!"".equals(e.text)) {
					for (int i=e.text.length()-1; i>-1; i--) {
						char c = e.text.charAt(i);
						if (c != '0' && c != '1') {
							e.doit = false;
							return ;
						}
					}
				}
			}			
		});
	}
	
	private String replaceString(String src,String des,int start,int end) {
		StringBuffer strb = new StringBuffer();
		if (0 < start) {
			strb.append(src.substring(0,start));
		}
		strb.append(des);
		if (end < src.length()-1) {
			strb.append(src.substring(end));
		}
		return strb.toString();
	}
}
