package lcc.editorx.frame;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import lcc.editorx.editor.TextEditorE;
import lcc.editorx.editor.syntax.SyntaxManager;
import lcc.editorx.frame.AppMenu.MenuNode;
import lcc.editorx.frame.config.FileRecord;
import lcc.editorx.widget.DlgAbout;
import lcc.editorx.widget.DlgCanteenRecord;
import lcc.editorx.widget.DlgColor;
import lcc.editorx.widget.DlgHexCode;
import lcc.editorx.widget.DlgJavaPo;
import lcc.editorx.widget.DlgRename;
import lcc.editorx.widget.DlgSystemInfo;

/**
 * 应用的各种响应处理
 * @author lcc
 *
 */
public class AppAction {
	/**
	 * 菜单项响应处理
	 * @param menucode 菜单代码
	 */
	public static void onMenuResponse(AppMenu.MenuNode node) {
		//////////////////////////////////////////////////
		//“文件”菜单
		if ("打开".equals(node.code)) {
			AppFrame frame = AppFrame.getInstance();
			FileDialog dlg = new FileDialog(frame.getShell(),SWT.OPEN);
			dlg.setFilterExtensions(new String[]{"*.*","*.sql","*.xml","*.java","*.cpp","*.h","*.txt"});  
			dlg.setFilterNames(new String[]{"All Files(*.*)","SQL(*.sql)","XML(*.xml)","JAVA(*.java)","C++ cpp(*.cpp)","C++ h(*.h)","Text(*.txt)"}); 
			String filepath = dlg.open();
			if (filepath == null) {
				return ;
			}
			
			onOpen(filepath);
		} else if ("保存".equals(node.code)) {
			TextEditorE editor = AppFrame.getInstance().getActiveEditor();
			onSave(editor,false);
		} else if ("另存为".equals(node.code)) {
			TextEditorE editor = AppFrame.getInstance().getActiveEditor();
			onSave(editor,true);
		} else if ("新建".equals(node.code)) {
			AppFrame.getInstance().createEditor();
			updateMenuState();
		} else if ("M_FILERECORD".equals(node.code)) {//文件历史记录
			File f = new File(node.text);
			if (!f.exists() || !f.isFile()) {
				FileRecord.deleteRecord(node.text);
				node.menuItem.dispose();
				AppFrame.messageBox("无效的文件路径\r"+node.text);
			} else {			
				onOpen(node.text);
			}
		}
		
		//////////////////////////////////////////////////////
		//“查看”菜单
		else if ("回到当前行".equals(node.code)) {
			TextEditorE editor = AppFrame.getInstance().getActiveEditor();
			if (editor != null) {
				editor.scrollToCurLine();
			}
		} else if ("窗口最前".equals(node.code)) {
			boolean topmost = node.menuItem.getSelection();
			AppFrame.getInstance().setTopWindow(topmost);
		} else if ("自动换行".equals(node.code)) {
			TextEditorE editor = AppFrame.getInstance().getActiveEditor();
			if (editor != null) {
				boolean wrap = node.menuItem.getSelection();
				wrap = editor.wrap(wrap);
				node.menuItem.setSelection(wrap);
			}
		} else if ("重新加载文件".equals(node.code)) {
			TextEditorE editor = AppFrame.getInstance().getActiveEditor();
			String filepath = editor.getFilepath();
			if (filepath == null || "".equals(filepath)) {
				AppFrame.messageBox("当前编辑器窗口尚未加载任何文件");
				return ;
			}
			if (editor.isModified()
				&& !AppFrame.confirm("当前文件内容已更改，如果继续，讲丢失修改的内容，是否继续?")) {
				return ;
			}
			editor.openFile(filepath);
		} else if ("复制文件路径".equals(node.code)) {
			TextEditorE editor = AppFrame.getInstance().getActiveEditor();
			if (editor == null) {
				AppFrame.messageBox("当前无活动的编辑器窗口");
				return ;
			}
			String filepath = editor.getFilepath();
			if (filepath == null || "".equals(filepath)) {
				AppFrame.messageBox("当前编辑器窗口尚未加载任何文件");
				return ;
			}
			
			Clipboard clipboard = new Clipboard(AppFrame.getInstance().getDisplay());
			clipboard.setContents(new String[]{filepath},new Transfer[]{TextTransfer.getInstance()});
		} else if ("none".equals(node.code) || SyntaxManager.getInstance().isSupportedLanguage(node.code)) {
			if (!node.menuItem.getSelection()) {
				return ;
			}
			TextEditorE oldEditor = AppFrame.getInstance().getActiveEditor();
			if (oldEditor == null) {
				AppFrame.messageBox("当前无活动的编辑器窗口");
				return ;
			}
			if (node.code.equals(oldEditor.getLanguage())) {
				return;
			}
			
			//关闭旧的编辑器
			CTabItem curItem = AppFrame.getInstance().getActiveTabItem();
			final String title = curItem.getText();
			final int index = AppFrame.getInstance().getTabFolder().indexOf(curItem);
			curItem.setData(null);
			curItem.dispose();
			curItem = null;
			
			//创建新的编辑器
			TextEditorE newEditor = AppFrame.getInstance().createEditor(index,title);
			newEditor.copy(oldEditor);
			SyntaxManager.getInstance().initEditorSyntax(newEditor, node.code);
		} 
		
		//////////////////////////////////////////////////////
		//“编码”菜单
		else if (TextEditorE.isSupportedCharset(node.code)) {
			if (!node.menuItem.getSelection()) {
				return ;
			}
			
			TextEditorE editor = AppFrame.getInstance().getActiveEditor();
			if (editor != null) {
				String result = editor.setSelectedEncode(node.code);
				if (!node.code.equalsIgnoreCase(result)) {
					AppMenu.getInstance().checkMenuItem(node.code, false);
					AppMenu.getInstance().checkMenuItem(result, true);
					AppFrame.messageBox("不支持当前编码");
					return ;
				}
			}
		} else if ("带BOM".equals(node.code)) {
			TextEditorE editor = AppFrame.getInstance().getActiveEditor();
			if (editor != null) {
				editor.setHasBOM(node.menuItem.getSelection());
			}
		}
		
		//////////////////////////////////////////////////////
		//“工具”菜单
		else if ("关于".equals(node.code)) {
			DlgAbout dlg = new DlgAbout();
			dlg.open();
		} else if ("字数统计".equals(node.code)) {
			TextEditorE editor = AppFrame.getInstance().getActiveEditor();
			if (editor != null) {
				editor.setHasBOM(node.menuItem.getSelection());
			}
			String str = editor.getTextWidget().getSelectionText();
			if (str == null || "".equals(str)) {
				str = editor.getText();
			}
			
			int charLength = str.length();
			int byteLength = 0;
			try {
				byteLength = str.getBytes(editor.getSelectedEncode()).length;
			}catch (Exception e) {
			}
			
			String result = "字符数："+charLength+"\r\n字节数："+byteLength+"\r\n字符编码："+editor.getSelectedEncode();
			AppFrame.messageBox(result);
		} else if ("编码查看".equals(node.code)) {
			DlgHexCode dlg = new DlgHexCode();
			dlg.open();
		} else if ("生成Java Po类".equals(node.code)) {
			DlgJavaPo dlg = new DlgJavaPo();
			dlg.open();
		} else if ("屏幕取色".equals(node.code)) {
			DlgColor dlg = new DlgColor(AppFrame.getInstance().getShell(),false);
			dlg.open();
		} else if ("文件名称批量修改".equals(node.code)) {
			DlgRename dlg = new DlgRename();
			dlg.open();
		} else if ("系统信息".equals(node.code)) {
			DlgSystemInfo dlg = new DlgSystemInfo();
			dlg.open();
		} else if ("南瑞食堂".equals(node.code)) {
			DlgCanteenRecord dlg = new DlgCanteenRecord();
			dlg.open();
		}
		
		else {
			AppFrame.messageBox(node.code);
		}
	}
	
	public static boolean onOpen(String filepath) {
		AppFrame frame = AppFrame.getInstance();
		boolean needCreate = false;
		TextEditorE editor = frame.getActiveEditor();
		if (editor == null) {
			needCreate = true;
		} else if ((editor.getFilepath() == null || "".equals(editor.getFilepath()))
				&& "".equals(editor.getText())
				&& !editor.isModified()) {
			needCreate = false;
		} else {
			needCreate = true;
		}
		
		if (needCreate) {
			//如果默认的编辑器Tab页已有内容，则新创建一个Tab页
			editor = frame.createEditor();
		}
			
		if (editor.openFile(filepath)) {
			//文件历史记录
			FileRecord.addRecord(filepath);
			
			CTabItem tabItem = frame.getActiveTabItem();
			tabItem.setText(editor.getFileName());
			frame.setTitle(editor.getFilepath());
			updateMenuState();
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 保存事件
	 * @param editor
	 * @param saveas 是否“另存为”
	 */
	public static void onSave(TextEditorE editor,boolean saveas) {
		if (!saveas && !editor.isModified()) {
			return;
		}
		
		String filepath = saveas ? "" : editor.getFilepath();		
		
		//获取文件的保存路径及名称
		if (filepath == null || "".equals(filepath)) {
			FileDialog dlg = new FileDialog(AppFrame.getInstance().getShell(),SWT.SAVE);
			dlg.setFilterExtensions(new String[]{"*.txt","*.sql","*.xml","*.java","*.cpp","*.h","*.*"});  
			dlg.setFilterNames(new String[]{"Text(*.txt)","SQL(*.sql)","XML(*.xml)","JAVA(*.java)","C++ cpp(*.cpp)","C++ h(*.h)","All Files(*.*)"}); 
			filepath = dlg.open();
			if (filepath == null) {
				return ;
			}
			
			File file = new File(filepath);
			if (file.exists()) {
				if (!AppFrame.confirm("当前文件已存在，是否覆盖？")) {
					return ;
				}
			}
		}
		
		//保存文件
		if (!editor.saveFile(filepath)) {
			return ;
		}
		
		//文件历史记录
		FileRecord.addRecord(filepath);
		
		AppFrame frame = AppFrame.getInstance();
		CTabItem tabItem = frame.getActiveTabItem();
		tabItem.setText(editor.getFileName());
		frame.setTitle(editor.getFilepath());
	}
	
	/**
	 * 主应用的tab页切换事件
	 */
	public static void onTabChanged() {
		updateMenuState();
	}
	
	/**
	 * 根据当前活动的编辑器，更新菜单的状态，例如“自动换行”是否被选中，“编码”菜单下的哪个子项应被选中等
	 */
	public static void updateMenuState() {
		AppFrame frame = AppFrame.getInstance();
		CTabItem tabItem = frame.getActiveTabItem();
		if (tabItem == null) {
			frame.setTitle(null);
			return ;
		}
		
		TextEditorE editor = frame.getActiveEditor();
		if (editor != null) {
			frame.setTitle(editor.getFilepath());
		}
		
		AppMenu mainMenu = AppMenu.getInstance();
		mainMenu.checkMenuItem("自动换行", editor.isWrap());
		
		String language = editor.getLanguage();
		String[] ls = SyntaxManager.getInstance().getSupportedLanguage();
		mainMenu.checkMenuItem("none", false);
		for (int i=0; i<ls.length; i++) {
			mainMenu.checkMenuItem(ls[i], false);
		}
		mainMenu.checkMenuItem(language, true);
		
		//更新字符编码菜单的状态
		MenuNode node = mainMenu.getNode("OTHER");
		String encode = editor.getSelectedEncode();
		String[] supportEncodes = TextEditorE.getSupportedCharset();
		for (int i=0; i<supportEncodes.length; i++) {
			mainMenu.checkMenuItem(supportEncodes[i], false);
		}
		if (TextEditorE.isSupportedCharset(encode)) {
			mainMenu.checkMenuItem(encode, true);
			node.menuItem.setSelection(false);
			node.menuItem.setText("OTHER");
		} else {			
			node.menuItem.setSelection(true);
			node.menuItem.setText(encode);
		}
		
		mainMenu.checkMenuItem("带BOM", editor.hasBom());
	}
	
	/**
	 * 编辑器关闭响应
	 * @param editor
	 * @return true-可以关闭，false-不可以关闭
	 */
	public static boolean onEditorClose(TextEditorE editor) {
		if (!editor.isModified()) {
			return true;
		}
		
		MessageBox msgBox = new MessageBox(AppFrame.getInstance().getShell()
				,SWT.YES|SWT.NO|SWT.CANCEL|SWT.ICON_WARNING);
		msgBox.setText("退出确认");
		msgBox.setMessage("当前文件内容已更改，是否保存？");
		int result = msgBox.open();
		switch (result) {
		case SWT.YES:
			onSave(editor,false);
			return true;
		case SWT.NO:
			return true;
		default:
			return false;
		}
	}
}
