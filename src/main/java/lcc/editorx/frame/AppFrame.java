package lcc.editorx.frame;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import lcc.editorx.editor.TextEditorE;
import lcc.editorx.editor.syntax.SyntaxManager;
import lcc.editorx.frame.config.TempContentRecord;

public class AppFrame {
	private Display display = null;
	private Shell shell = null;
	private CTabFolder tabFolder = null;
	private Image editorBackground = null;
	
	//是否为windows平台
	public static final boolean isWindows = System.getProperty("os.name").toLowerCase().indexOf("windows") > -1;
	
	//单例模式
	private static AppFrame instance = null;
	public static AppFrame getInstance() {  
		if (instance == null) {  
			instance = new AppFrame();  
		}  
		return instance;
	}
	private AppFrame() {
		this.display = Display.getDefault();
		this.shell = new Shell(this.display);
		
		try {
			File folder = new File(System.getProperty("user.dir")+"/res");
			if (folder.exists()) {
				File[] files = folder.listFiles();
				for (File f: files) {
					if (!f.isFile()) {
						continue;
					}
					
					if (f.getName().toLowerCase().startsWith("background")) {
						this.editorBackground = new Image(this.display,f.getPath());
						break;
					}
				}
			}
		} catch (Exception e) {
		}
	}
	
	public Display getDisplay() {
		return this.display;
	}
	
	public Shell getShell() {
		return this.shell;
	}
	
	public CTabFolder getTabFolder() {
		return this.tabFolder;
	}
	
	public Image getEditorBackground() {
		return this.editorBackground;
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		AppFrame mainWnd = AppFrame.getInstance();
		String filePath = null;
		if (args.length > 0 && !"".equals(args[0].trim())) {
			filePath = args[0].trim();
		}
		mainWnd.load(filePath);
	}
	
	public void load(String filePath) {
		//设置shell的布局方式
		this.shell.setLayout(null);
		this.shell.setLayout(new FillLayout());
		//应用打开后，窗口最大化
		//this.shell.setMaximized(true);
		
		//设置应用图片
		if (ImageManager.getInstance().get("EditorX.ico") != null) {
			this.shell.setImage(ImageManager.getInstance().get("EditorX.ico"));
		}
		
		//设置应用标题
		this.setTitle(null);
		
		//创建Tab页
		this.tabFolder = new CTabFolder(this.shell,SWT.NONE|SWT.CLOSE|SWT.BORDER);
		this.tabFolder.setSimple(false);//设置圆角 
		this.tabFolder.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				AppAction.onTabChanged();
			}			
		});
		//tabFolder双击事件，创建一个新的tab页
		this.tabFolder.addMouseListener(new MouseListener(){
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
				AppFrame.getInstance().createEditor();
				AppAction.updateMenuState();
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
			}
			
		});
		//编辑器关闭事件
		this.tabFolder.addCTabFolder2Listener(new CTabFolder2Adapter(){
			@Override
			public void close(CTabFolderEvent e) {
				CTabItem item = (CTabItem)e.item;
				TextEditorE editor = (TextEditorE)item.getData();
				e.doit = AppAction.onEditorClose(editor);
				if (e.doit) {
					//取消临时内容的保存
					TempContentRecord.getInstance().deleteContent(editor);
					
					setTitle(null);
				}
			}
		});
		//应用关闭事件
		this.shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				final int count = tabFolder.getItemCount();
				for (int i=count-1; i>-1; i--) {
					CTabItem item = tabFolder.getItem(i);
					
					TextEditorE editor = (TextEditorE)item.getData();
					if (editor.isModified()) {
						tabFolder.setSelection(item);
						tabFolder.showItem(item);
						e.doit = AppAction.onEditorClose(editor);
					}
				}
			}
		});

		/*
		 * 加载主菜单,该句必须放在最后，
		 * 因为MainMenuManager.initMenuData()中会调用BaseView的构造函数，而该函数中需要一个有效的tabFolder
		 */
		AppMenu.getInstance().load();
		
		//打开shell主窗口
		this.shell.open();
		
		//打开之前的临时内容
		TempContentRecord.getInstance().showRecords();
		
		//创建默认空编辑器tab页
		TextEditorE editor = createEditor();		
		if (filePath != null && !"".equals(filePath) && editor.openFile(filePath)) {
			CTabItem tabItem = this.getActiveTabItem();
			tabItem.setText(editor.getFileName());
			this.setTitle(editor.getFilepath());
		}
		AppAction.updateMenuState();
		
		//事件转发循环.
		while (!this.shell.isDisposed()) {// 如果主窗口没有关闭,则一直循环
			if (!this.display.readAndDispatch()) {// 如果display不忙
				this.display.sleep();// display休眠
			}
		}
		//销毁display及其内容
		this.display.dispose();
	}
	
	//信息提示框
	public static void messageBox(String text) {
		lcc.utils.swt.Util.messageBox(AppFrame.getInstance().getShell(), text);
	}
	
	//确认提示框
	public static boolean confirm(String text) {
		MessageBox msgBox = new MessageBox(AppFrame.getInstance().getShell(),SWT.OK|SWT.CANCEL|SWT.ICON_QUESTION);
		msgBox.setText("确认");
		msgBox.setMessage(text);
		return msgBox.open() == SWT.OK;
	}
	
	public void setTitle(String title) {
		String t;
		if (title == null) {
			t = "EditorX";
		} else {
			t = title+" ★EditorX";
		}
		this.shell.setText(t);
	}
	
	/**
	 * 设置窗口最前
	 * @param topmost 是否窗口最前
	 */
	public void setTopWindow(boolean topmost) {
		final int type = topmost 
				? org.eclipse.swt.internal.win32.OS.HWND_TOPMOST 
				: org.eclipse.swt.internal.win32.OS.HWND_NOTOPMOST;
		org.eclipse.swt.internal.win32.OS.SetWindowPos(this.shell.handle, type,0,0,0,0
				,org.eclipse.swt.internal.win32.OS.SWP_NOMOVE|org.eclipse.swt.internal.win32.OS.SWP_NOSIZE);
		//AppFrame.messageBox("仅windows版本支持该功能");
	}
	
	public CTabItem getActiveTabItem() {
		return this.tabFolder.getSelection();
	}
	
	/**
	 * 获取当前活动的编辑器的实例
	 * @return
	 */
	public TextEditorE getActiveEditor() {
		CTabItem activeItem = this.getActiveTabItem();
		return activeItem == null ? null : (TextEditorE)activeItem.getData();
	}
	
	/**
	 * 设置修改标记，即 编辑器所在tab页（通常为当前活动tab页）的标题内容增加*标识
	 */
	public void setTabModifiedSign(boolean modified) {
		CTabItem tab = getActiveTabItem();
		if (tab != null) {
			String title = tab.getText();
			boolean hasStarSign = title.startsWith("* ");
			if (modified) {
				if (!hasStarSign) {
					tab.setText("* "+title);
				}
			} else {
				if (hasStarSign) {
					if (title.length() > 2) {
						tab.setText(title.substring(2));
					} else {
						title = "空";
					}
				}
			}
		}
	}
	
	/**
	 * 创建一个新的编辑器tab页,位置放在在最后
	 * @return
	 */
	public TextEditorE createEditor() {
		CTabFolder tabFolder = this.getTabFolder();
		return this.createEditor(tabFolder.getItemCount());
	}
	
	/**
	 * 在指定位置创建一个新的编辑器tab页
	 * @return
	 */
	public TextEditorE createEditor(int index) {
		return this.createEditor(index,"new");
	}
	
	/**
	 * 在指定位置创建一个新的编辑器tab页
	 * @return
	 */
	public TextEditorE createEditor(int index,String title) {
		CTabFolder tabFolder = this.getTabFolder();
		
		if (index < 0) {
			index = 0;
		} else if (tabFolder.getItemCount() <= index) {
			index = tabFolder.getItemCount();
		}
		
		CTabItem tabItem = new CTabItem(tabFolder, SWT.NONE ,index);
		tabItem.setText(title);	
		tabItem.setImage(ImageManager.getInstance().get("editor.ico"));
		Composite composite = new Composite(tabFolder, SWT.NONE);
		composite.setLayout(new FillLayout());		
		TextEditorE textEditor = new TextEditorE(composite,SyntaxManager.LANGUAGE_NONE);
		tabItem.setControl(composite);
		tabItem.setData(textEditor);

		tabFolder.setSelection(tabItem);
		tabFolder.showItem(tabItem);
		textEditor.focus();
		return textEditor;
	}
	
	//将内容输出至以sonsole为标题的编辑器中
	public void console(String text) {
		TextEditorE console = null;
		for (int i=tabFolder.getItemCount()-1; i>-1; i--) {
			CTabItem item = tabFolder.getItem(i);
			if ("console".equals(item.getText())) {
				console = (TextEditorE)item.getData();
				tabFolder.setSelection(item);
				tabFolder.showItem(item);
				console.focus();
				break;
			}
		}		
		if (console == null) {
			console = createEditor(999,"console");
			console.getTextWidget().setEditable(false);
		}
		
		console.setText(console.getText()+"\r\n"+text);
	}
}
