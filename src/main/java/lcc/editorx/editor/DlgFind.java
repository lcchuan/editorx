package lcc.editorx.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ColorManager;
import lcc.editorx.frame.ImageManager;

/**
 * 查询与替换对话框
 * @author lcc
 *
 */
public class DlgFind extends Dialog {
	private TextEditorE editor = null;
	private Text txtFind = null;
	private Text txtReplace = null;
	
	private Button chkCaseSensitive = null;
	private Button chkWholeWord = null;
	private Button rdoUp = null;
	private Button rdoDown = null;
	private Label hint = null;
	
	//上一个查找到的文本的起始位置
	private int preFindedIndex = -1;
	//上一次查找的文本
	private String preFindText = null;
	
	//单例模式
	private static DlgFind instance = null;
	public static DlgFind getInstance(TextEditorE editor) {  
		if (instance == null) {  
			instance = new DlgFind();  
		}
		instance.editor = editor;
		instance.preFindedIndex = -1;
		return instance;
	}
	private DlgFind() {
		super(AppFrame.getInstance().getShell());
	}
	
	/**
	 * 创建对话框内容
	 */
	@Override
	protected Control createDialogArea(Composite parent) {				
		Composite composite = new Composite(parent, 0);
		
		Label label = new Label(composite,SWT.NONE);
		label.setText("查找");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10, 12, 40, 20));
		this.txtFind = new Text(composite,SWT.BORDER);
		this.txtFind.setBounds(lcc.utils.swt.Util.zoomForHignGDI(60, 10, 220, 22));
		this.txtFind.setFocus();
		this.txtFind.setText(editor.getTextWidget().getSelectionText());
		this.txtFind.setSelection(100);
		
		label = new Label(composite,SWT.NONE);
		label.setText("替换");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10, 38, 40, 20));
		this.txtReplace = new Text(composite,SWT.BORDER);
		this.txtReplace.setBounds(lcc.utils.swt.Util.zoomForHignGDI(60, 36, 220, 22));
		
		this.chkCaseSensitive = new Button(composite,SWT.CHECK);
		this.chkCaseSensitive.setText("区分大小写");
		this.chkCaseSensitive.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,66,100,20));
		this.chkCaseSensitive.setSelection(true);
		
		this.chkWholeWord = new Button(composite,SWT.CHECK);
		this.chkWholeWord.setText("整个字");
		this.chkWholeWord.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,88,100,20));
		
		this.rdoUp = new Button(composite,SWT.RADIO);
		this.rdoUp.setText("向上查找");
		this.rdoUp.setBounds(lcc.utils.swt.Util.zoomForHignGDI(110, 66, 70, 20));
		
		this.rdoDown = new Button(composite,SWT.RADIO);
		this.rdoDown.setText("向下查找");
		this.rdoDown.setBounds(lcc.utils.swt.Util.zoomForHignGDI(110, 88, 70, 20));
		this.rdoDown.setSelection(true);
		
		this.hint = new Label(composite,SWT.RIGHT);
		this.hint.setForeground(ColorManager.getInstance().get(new RGB(255,0,0)));
		this.hint.setBounds(lcc.utils.swt.Util.zoomForHignGDI(75,112,100,20));
		//this.setReplaceCount(120);
		
		Button btn = new Button(composite,SWT.PUSH);
		btn.setText("查找");
		btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(200, 66, 70, 22));
		btn.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				if (editor != null) {
					if ("".equals(txtFind.getText())) {
						AppFrame.messageBox("请输入要搜索的文字");
						txtFind.setFocus();
						return ;
					}
					
					if (preFindText == null || !preFindText.equals(txtFind.getText())) {
						preFindText = txtFind.getText();
						preFindedIndex = -1;
					} else {
						if (!rdoUp.getSelection()) {
							preFindedIndex += preFindText.length();
						}
					}
					preFindedIndex = editor.findText(txtFind.getText()
							             , chkCaseSensitive.getSelection()
							             , chkWholeWord.getSelection()
							             , rdoUp.getSelection()
							             , preFindedIndex);
					if (preFindedIndex < 0) {
						hint.setText("无搜索结果");
					} else {
						hint.setText("");
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}			
		});
		
		btn = new Button(composite,SWT.PUSH);
		btn.setText("替换");
		btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(200, 90, 70, 22));
		btn.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				if (editor != null) {
					if ("".equals(txtFind.getText())) {
						AppFrame.messageBox("请输入要搜索的文字");
						txtFind.setFocus();
						return ;
					}
					
					Point range = editor.getSelectedRange();
					if (range.y < 1) {
						AppFrame.messageBox("请选择要替换的文本");
						txtFind.setFocus();
						return ;
					}
					
					editor.replaceText(txtReplace.getText());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}			
		});
		
		btn = new Button(composite,SWT.PUSH);
		btn.setText("替换全部");
		btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(200, 114, 70, 24));
		btn.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				if (editor != null) {
					if ("".equals(txtFind.getText())) {
						AppFrame.messageBox("请输入要搜索的文字");
						txtFind.setFocus();
						return ;
					}
					
					int count = editor.replaceAll(txtFind.getText(), txtReplace.getText(), chkCaseSensitive.getSelection(), chkWholeWord.getSelection());
					setReplaceCount(count);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}			
		});
		
		applyDialogFont(composite);
	    return composite;
	}
	
	/**
	 * 设置被替换的数量
	 * @param count 被替换的数量，如果小于1，则清空提示
	 */
	public void setReplaceCount(int count) {
		if (count < 0) {
			this.hint.setText("");
		} else {
			this.hint.setText("共替换"+count+"处");
		}
	}
	
	/**
	 * 设置标题栏名称与图标
	 */
	@Override  
	protected void configureShell(Shell newShell) { 
	    super.configureShell(newShell);  
	    newShell.setText("查找/替换");
	    if (ImageManager.getInstance().get("EditorX.ico") != null) {
	    	newShell.setImage(ImageManager.getInstance().get("EditorX.ico"));
		}
	}
	
	/**
	 * 设置窗体大小
	 */
	@Override  
	protected Point getInitialSize() { 
	    return new Point(lcc.utils.swt.Util.zoomForHignGDI(300),lcc.utils.swt.Util.zoomForHignGDI(220));  
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
}
