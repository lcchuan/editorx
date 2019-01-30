package lcc.editorx.editor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.AppMenu;

/**
 * 编辑器右键菜单
 * @author lcc
 *
 */
public class EditorPopupMenu {
	private static List<String> menuData = null;
	
	/**
	 * 创建菜单
	 */
	public static Menu createPopupMenu(TextEditorE editor) {
		//初始化菜单数据
		initMenuData();
				
		Menu menu = new Menu(AppFrame.getInstance().getShell(),SWT.POP_UP);
		MenuItem item;
		for (int i=0; i<menuData.size(); i++) {
			if (AppMenu.SEPARATOR.equals(menuData.get(i))) {
				item = new MenuItem(menu,SWT.SEPARATOR);	
			} else if ("插入符号".equals(menuData.get(i))){
				item = new MenuItem(menu,SWT.CASCADE);
				item.setText(menuData.get(i));
				
				String[] chars;
				//第二级子菜单
				Menu childMenu2 = new Menu(AppFrame.getInstance().getShell(),SWT.DROP_DOWN);
				//第三季子菜单
				Menu childMenu3;
				
				item.setMenu(childMenu2);
				item = new MenuItem(childMenu2,SWT.CASCADE);
				item.setText("普通");
				childMenu3 = new Menu(AppFrame.getInstance().getShell(),SWT.DROP_DOWN);
				item.setMenu(childMenu3);
				chars = new String[]{"√","×","⊕","【","】","︻","︼","「","」","『","』","※","§","○","●","☆","★","♀","♂"};
				createSymbolMenuItem(childMenu3,chars,editor);
				
				item = new MenuItem(childMenu2,SWT.CASCADE);
				item.setText("箭头");
				childMenu3 = new Menu(AppFrame.getInstance().getShell(),SWT.DROP_DOWN);
				item.setMenu(childMenu3);
				chars = new String[]{"↑","↓","←","→","↖","↗","↙","↘"};
				createSymbolMenuItem(childMenu3,chars,editor);
				
				item = new MenuItem(childMenu2,SWT.CASCADE);
				item.setText("数学");
				childMenu3 = new Menu(AppFrame.getInstance().getShell(),SWT.DROP_DOWN);
				item.setMenu(childMenu3);
				chars = new String[]{"≈","≌","≡","≠","≤","≥","≮","≯","±","∫","∮","∝","∞","∑","∏","π","∩","∪","∈","∵","∴","㏒","㏑","‰"};
				createSymbolMenuItem(childMenu3,chars,editor);
				
				item = new MenuItem(childMenu2,SWT.CASCADE);
				item.setText("单位");				
				childMenu3 = new Menu(AppFrame.getInstance().getShell(),SWT.DROP_DOWN);
				item.setMenu(childMenu3);
				chars = new String[]{"￥","℃","℉","㎜","㎝","㏕","㎞","㎡","㎎","㎏","Ω"};
				createSymbolMenuItem(childMenu3,chars,editor);
				
				item = new MenuItem(childMenu2,SWT.CASCADE);
				item.setText("希腊字母");
				childMenu3 = new Menu(AppFrame.getInstance().getShell(),SWT.DROP_DOWN);
				item.setMenu(childMenu3);
				chars = new String[]{"Ⅰ","Ⅱ","Ⅲ","Ⅳ","Ⅴ","Ⅵ","Ⅶ","Ⅷ","Ⅸ","Ⅹ","Ⅺ","Ⅻ"};
				createSymbolMenuItem(childMenu3,chars,editor);
			} else {
				item = new MenuItem(menu, SWT.PUSH);
				item.setText(menuData.get(i));
				item.setData("editor",editor);
				item.setData("type","normal");
				
				item.addSelectionListener(selectionAdapter);
			}
		}
		return menu;
	}
	
	private static SelectionAdapter selectionAdapter = new SelectionAdapter() {		
		@Override 
	    public void widgetSelected(SelectionEvent e) {
			TextEditorE editor = (TextEditorE)e.widget.getData("editor");
			boolean forSymbol = "symbol".equals(e.widget.getData("type"));
			String itemText = ((MenuItem)e.widget).getText();
			if (forSymbol) {
				editor.replaceText(itemText);
			} else {
				onItemClick(itemText,editor);
			}
	    }
		
		@Override  
        public void widgetDefaultSelected(SelectionEvent arg0) {  
        }  
	};
	
	private static void createSymbolMenuItem(Menu parent,String[] chars,TextEditorE editor) {
		MenuItem item;
		for (int i=0; i<chars.length; i++) {
			item = new MenuItem(parent, SWT.PUSH);
			item.setText(chars[i]);
			item.setData("editor",editor);
			item.setData("type","symbol");
			item.addSelectionListener(selectionAdapter);
		}
	}
	
	/**
	 * 初始化菜单数据
	 */
	private static void initMenuData() {
		if (menuData != null) {
			return ;
		}
		menuData = new ArrayList<String>();
		menuData.add("撤销");
		menuData.add("恢复");
		menuData.add(AppMenu.SEPARATOR);
		menuData.add("全选");
		menuData.add("复制");
		menuData.add("剪切");
		menuData.add("黏贴");
		menuData.add("删除");
		menuData.add(AppMenu.SEPARATOR);
		menuData.add("清除空行");
		menuData.add("清除行号");
		menuData.add("字符大写");
		menuData.add("字符小写");
		menuData.add("插入符号");
		menuData.add(AppMenu.SEPARATOR);
		menuData.add("查找/替换");
	}
	
	private static void onItemClick(String itemText,TextEditorE editor) {
		if ("撤销".equals(itemText)) {
			editor.undo();
		} else if ("恢复".equals(itemText)) {
			editor.redo();
		} else if ("全选".equals(itemText)) {
			editor.setSelectedRange(0, editor.getTextWidget().getCharCount());
		} else if ("复制".equals(itemText)) {
			editor.copy();
		} else if ("剪切".equals(itemText)) {
			editor.cut();
		} else if ("黏贴".equals(itemText)) {
			editor.getTextWidget().paste();
		} else if ("删除".equals(itemText)) {
			editor.replaceText("");
		} else if ("清除空行".equals(itemText)) {
			editor.clearNullLine();
		} else if ("清除行号".equals(itemText)) {
			editor.clearRowNumber();
		} else if ("字符大写".equals(itemText)) {
			editor.changeCase(true);
		} else if ("字符小写".equals(itemText)) {
			editor.changeCase(false);
		} else if ("查找/替换".equals(itemText)) {
			DlgFind dlg = DlgFind.getInstance(editor);
			dlg.open();
		} else {
			AppFrame.messageBox(itemText);
		}
	}
}
