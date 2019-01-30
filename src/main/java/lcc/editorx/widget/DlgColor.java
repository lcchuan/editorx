package lcc.editorx.widget;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ImageManager;

/**
 * 颜色查看对话框
 * @author lcc
 *
 */
public class DlgColor extends Dialog {
	private Label colorShow = null;
	private boolean updateColorInput = true;
	
	//是否用于屏幕取色
	private boolean forColorExtract = false;
	
	private Text grb_red = null;
	private Text grb_green = null;
	private Text grb_blue = null;
	//rgb输入校验监听
	private VerifyListener grbVerifyListener = null;
	//rgb内容更改监听
	private ModifyListener grbModifyListener = null;
	
	private Text colorValue = null;
	
	private DlgColor me = null;
	
	/**
	 * 
	 * @param shell
	 * @param forColorExtract 是否用于屏幕取色
	 */
	public DlgColor(Shell shell,boolean forColorExtract) {
		super(shell);
		
		this.forColorExtract = forColorExtract;
		this.me = this;
		
		//rgb输入验证
		this.grbVerifyListener = new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				if ("".equals(e.text)) {
					return ;
				}
				
				String str = replaceString(((Text)e.widget).getText(),e.text,e.start,e.end);
				try {
					int n = Integer.valueOf(str);
					e.doit = (-1 < n && n <256);
				} catch (Exception ep) {
					e.doit = false;
				}
			}			
		};
		//rgb内容更改监听
		this.grbModifyListener = new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!updateColorInput) {
					return ;
				}
				try {
					int red = Integer.valueOf(grb_red.getText());
					int green = Integer.valueOf(grb_green.getText());
					int blue = Integer.valueOf(grb_blue.getText());
					setColor(red,green,blue);
				} catch (Exception ep) {
				}
			}
		};
	}
	
	/**
	 * 设置标题栏名称与图标
	 */
	@Override  
	protected void configureShell(Shell newShell) { 
	    super.configureShell(newShell);  
	    newShell.setText("颜色提取");
	    if (ImageManager.getInstance().get("color.ico") != null) {
	    	newShell.setImage(ImageManager.getInstance().get("color.ico"));
		}
	} 
	
	/**
	 * 设置窗体大小
	 */
	@Override  
	protected Point getInitialSize() { 
	    return lcc.utils.swt.Util.zoomForHignGDI(new Point(300,180));  
	}
	
	/**
	 * 实现非模式对话框
	 */
	@Override  
	protected int getShellStyle() {		
		return SWT.MODELESS | SWT.CLOSE;
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
		
		this.colorShow = new Label(composite, SWT.NONE);		
		colorShow.setBackground(new Color(Display.getCurrent(),new RGB(128,128,128)));
		this.colorShow.setBounds(lcc.utils.swt.Util.zoomForHignGDI(2), lcc.utils.swt.Util.zoomForHignGDI(2)
				, lcc.utils.swt.Util.zoomForHignGDI(60), lcc.utils.swt.Util.zoomForHignGDI(60));
		
		Label label = new Label(composite, SWT.NONE);
		label.setText("R");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(73), lcc.utils.swt.Util.zoomForHignGDI(3)
				, lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.grb_red = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.grb_red.setBounds(lcc.utils.swt.Util.zoomForHignGDI(90), lcc.utils.swt.Util.zoomForHignGDI(2)
				, lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.grb_red.addVerifyListener(this.grbVerifyListener);
		this.grb_red.addModifyListener(this.grbModifyListener);
		
		label = new Label(composite, SWT.NONE);
		label.setText("G");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(73), lcc.utils.swt.Util.zoomForHignGDI(25)
				, lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.grb_green = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.grb_green.setBounds(lcc.utils.swt.Util.zoomForHignGDI(90), lcc.utils.swt.Util.zoomForHignGDI(24)
				, lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.grb_green.addVerifyListener(this.grbVerifyListener);
		this.grb_green.addModifyListener(this.grbModifyListener);
		
		label = new Label(composite, SWT.NONE);
		label.setText("B");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(73), lcc.utils.swt.Util.zoomForHignGDI(47)
				, lcc.utils.swt.Util.zoomForHignGDI(10), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.grb_blue = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.grb_blue.setBounds(lcc.utils.swt.Util.zoomForHignGDI(90), lcc.utils.swt.Util.zoomForHignGDI(46)
				, lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.grb_blue.addVerifyListener(this.grbVerifyListener);
		this.grb_blue.addModifyListener(this.grbModifyListener);
		
		label = new Label(composite, SWT.NONE);
		label.setText("web");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(55), lcc.utils.swt.Util.zoomForHignGDI(69)
				, lcc.utils.swt.Util.zoomForHignGDI(30), lcc.utils.swt.Util.zoomForHignGDI(20));
		this.colorValue = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.colorValue.setBounds(lcc.utils.swt.Util.zoomForHignGDI(90), lcc.utils.swt.Util.zoomForHignGDI(68)
				, lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(20));
		//输入验证
		this.colorValue.addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				if ("".equals(e.text)) {
					return ;
				}
				
				String str = replaceString(((Text)e.widget).getText(),e.text,e.start,e.end);
				for (int i=str.length()-1; i>-1; i--) {
					char c = str.charAt(i);
					if ('#' == c) {
						if (i != 0) {
							e.doit = false;
							return ;
						}
					} else if (('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F')) {						
					} else {
						e.doit = false;
						return ;
					}
				}
				
				if (str.length() > 7 || (str.length() == 7 && str.charAt(0) != '#')) {
					e.doit = false;
					return ;
				}
			}			
		});
		//内容更改监听
		this.colorValue.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (!updateColorInput) {
					return ;
				}
				String value = colorValue.getText();
				RGB rgb = getRGB(value);
				if (rgb != null) {
					setColor(rgb);
				}
			}
		});
		
		Button btnSelectColor = new Button(composite, SWT.NONE);
		btnSelectColor.setText("颜色选择");
		btnSelectColor.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				ColorDialog dlg = new ColorDialog(AppFrame.getInstance().getShell());
				RGB rgb = dlg.open();
				if (rgb != null) {
					me.setColor(rgb);
				}
			}
		});
		
		//屏幕取色按钮
		if (!forColorExtract) {
			btnSelectColor.setBounds(lcc.utils.swt.Util.zoomForHignGDI(180), lcc.utils.swt.Util.zoomForHignGDI(10)
					, lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(30));
			
			Button btnSreenColorExtract = new Button(composite, SWT.NONE);
			btnSreenColorExtract.setBounds(lcc.utils.swt.Util.zoomForHignGDI(180), lcc.utils.swt.Util.zoomForHignGDI(50)
					, lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(30));
			btnSreenColorExtract.setText("屏幕取色");
			btnSreenColorExtract.addSelectionListener(new SelectionListener(){	
				@Override
				public void widgetDefaultSelected(SelectionEvent arg0) {
				}
	
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					AppFrame.getInstance().getShell().setMinimized(true);
					
					ScreenColorExtract colorExtract = new ScreenColorExtract(me);
					colorExtract.run();
				}
			});
		} else {
			btnSelectColor.setBounds(lcc.utils.swt.Util.zoomForHignGDI(180), lcc.utils.swt.Util.zoomForHignGDI(30)
					, lcc.utils.swt.Util.zoomForHignGDI(70), lcc.utils.swt.Util.zoomForHignGDI(40));
		}
		
		setColor(0,128,255);
	    applyDialogFont(composite);
	    return composite;
    }
	
	/**
	 * 设置颜色显示
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setColor(int red,int green,int blue) {
		if (red < 0) {
			red = 0;
		}
		if (green < 0) {
			green = 0;
		}
		if (blue < 0) {
			blue = 0;
		}
		if (255 < red) {
			red = 255;
		}
		if (255 < green) {
			green = 255;
		}
		if (255 < blue) {
			blue = 255;
		}
		setColor(new RGB(red,green,blue));
	}
	
	/**
	 * 设置颜色显示
	 * @param updateGRBInput 是否更新grb的三个输入框内容
	 */
	public void setColor(RGB rgb) {
		Color color = new Color(Display.getCurrent(),rgb);
		this.colorShow.setBackground(color);
		
		this.updateColorInput = false;
		this.grb_red.setText(String.valueOf(rgb.red));
		this.grb_red.setSelection(this.grb_red.getText().length());
		this.grb_green.setText(String.valueOf(rgb.green));
		this.grb_green.setSelection(this.grb_green.getText().length());
		this.grb_blue.setText(String.valueOf(rgb.blue));
		this.grb_blue.setSelection(this.grb_blue.getText().length());
		this.colorValue.setText(getColorValue(rgb));
		this.colorValue.setSelection(this.colorValue.getText().length());
		this.updateColorInput = true;
	}
	
	/**
	 * 获取设置的颜色值
	 * @return
	 */
	public RGB getRGB() {
		try {
			return new RGB(Integer.valueOf(this.grb_red.getText())
					,Integer.valueOf(this.grb_green.getText())
					,Integer.valueOf(this.grb_blue.getText()));
		} catch (Exception e) {
			return null;
		}
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
	
	private String getColorValue(RGB rgb) {
		String color = "#";
		String r = Integer.toHexString(rgb.red);
		String g = Integer.toHexString(rgb.green);
		String b = Integer.toHexString(rgb.blue);
		
		color += (r.length() < 2 ? "0"+r : r);
		color += (g.length() < 2 ? "0"+g : g);
		color += (b.length() < 2 ? "0"+b : b);
		return color.toUpperCase();
	}
	
	private RGB getRGB(String color) {
		if (color == null || color.length() < 3) {
			return null;
		}
		
		//去掉开始处的'#'
		if (color.charAt(0) == '#') {
			color = color.substring(1);
		}
		
		String r,g,b;
		if (color.length() == 3) {
			r = String.valueOf(color.charAt(0))+color.charAt(0);
			g = String.valueOf(color.charAt(1))+color.charAt(1);
			b = String.valueOf(color.charAt(2))+color.charAt(2);
		} else if (color.length() == 6) {
			r = color.substring(0, 2);
			g = color.substring(2, 4);
			b = color.substring(4);
		} else {
			return null;
		}
		
		try {
			return new RGB(Integer.parseInt(r, 16),Integer.parseInt(g, 16),Integer.parseInt(b, 16));
		} catch (Exception e) {
			return null;
		}
	}
}
