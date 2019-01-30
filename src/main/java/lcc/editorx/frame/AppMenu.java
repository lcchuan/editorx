package lcc.editorx.frame;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import lcc.editorx.editor.TextEditorE;
import lcc.editorx.editor.syntax.SyntaxManager;
import lcc.editorx.frame.config.FileRecord;

public class AppMenu {
	//菜单项数据结构
	public class MenuNode {
		
		public String text = "";
		public String code = "";
		public List<MenuNode> children = null;
		//菜单类型：SWT.PUSH,SWT.CHECK,SWT.RADIO
		public int style = SWT.PUSH;
		
		public MenuItem menuItem = null;
	}
	
	//分隔符标记
	public static String SEPARATOR = "separator";
	
	//菜单数据
	private List<MenuNode> menuData;
	//菜单容器组件
	private Menu menu;
	
	//单例模式
	private static AppMenu instance = null;
	public static AppMenu getInstance() {  
		if (instance == null) {  
			instance = new AppMenu(); 
		}  
		return instance;  
	}
	private AppMenu() {
		//初始化菜单数据
		initMenuData();
	} 
	
	//初始化菜单数据
	private void initMenuData() {
		this.menuData = new ArrayList<MenuNode>();
		
		MenuNode node,childNode;
		
		////////////////////////////
		//文件
		node = new MenuNode();
		node.text = "文件";
		this.menuData.add(node);
		node.children = new ArrayList<MenuNode>();
		
		childNode = new MenuNode();
		childNode.text = "打开";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "保存";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "另存为";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "新建";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = SEPARATOR;
		node.children.add(childNode);
		
		//加载文件打开记录
		String[] files =  FileRecord.getRecords();
		if (files != null && files.length > 0) {
			for (int i=0; i<files.length; i++) {
				childNode = new MenuNode();
				childNode.text = files[i];
				childNode.code = "M_FILERECORD";
				node.children.add(childNode);
			}
		}
		
		////////////////////////////
		//查看
		node = new MenuNode();
		node.text = "查看";
		this.menuData.add(node);		
		node.children = new ArrayList<MenuNode>();
		
		childNode = new MenuNode();
		childNode.text = "回到当前行";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "窗口最前";
		childNode.style = SWT.CHECK;
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "自动换行";
		childNode.style = SWT.CHECK;
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "重新加载文件";
		node.children.add(childNode);

		childNode = new MenuNode();
		childNode.text = "复制文件路径";
		node.children.add(childNode);
		
		//分隔符
		childNode = new MenuNode();
		childNode.text = SEPARATOR;
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "不着色";
		childNode.code = "none";
		childNode.style = SWT.RADIO;
		node.children.add(childNode);
		
		String[] ls = SyntaxManager.getInstance().getSupportedLanguage();
		for (int i=0; i<ls.length; i++) {
			childNode = new MenuNode();
			childNode.text = ls[i];
			childNode.style = SWT.RADIO;
			node.children.add(childNode);
		}
		
		////////////////////////////
		//编码
		node = new MenuNode();
		node.text = "编码";
		this.menuData.add(node);		
		node.children = new ArrayList<MenuNode>();
		
		String[] encodes = TextEditorE.getSupportedCharset();
		for (int i=0; i<encodes.length; i++) {
			childNode = new MenuNode();
			childNode.code = encodes[i];
			childNode.text = encodes[i];
			if ("GBK".equalsIgnoreCase(childNode.code)) {
				childNode.text = "ANSI";
			}
			childNode.style = SWT.RADIO;
			node.children.add(childNode);
		}
		
		childNode = new MenuNode();
		childNode.text = "OTHER";
		childNode.style = SWT.RADIO;
		node.children.add(childNode);
		
		//分隔符
		childNode = new MenuNode();
		childNode.text = SEPARATOR;
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "带BOM";
		childNode.style = SWT.CHECK;
		node.children.add(childNode);
		
		////////////////////////////
		//工具
		node = new MenuNode();
		node.text = "工具";
		this.menuData.add(node);		
		node.children = new ArrayList<MenuNode>();
		
		childNode = new MenuNode();
		childNode.text = "关于";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "字数统计";
		node.children.add(childNode);
		
		//分隔符
		childNode = new MenuNode();
		childNode.text = SEPARATOR;
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "编码查看";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "生成Java Po类";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "屏幕取色";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "文件名称批量修改";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "系统信息";
		node.children.add(childNode);
		
		childNode = new MenuNode();
		childNode.text = "南瑞食堂";
		node.children.add(childNode);
		
		updateItemCode(this.menuData);
	}
	
	//加载菜单
	public Menu load() {
		Shell shell = AppFrame.getInstance().getShell();
		this.menu = new Menu(shell,SWT.BAR);
		
		//加载菜单
		loadMenu(null);
		
		//加载快捷菜单，仅支持windows平台
		loadWidgetMenu();
		
		shell.setMenuBar(this.menu);
		return this.menu;
	}
	
	//选中菜单项
	public void checkMenuItem(String itemText,boolean check) {
		MenuNode node = this.getNode(itemText);
		if (node != null) {
			node.menuItem.setSelection(check);
		}
	}
	
	/**
	 * 通过菜单名称，获取菜单节点信息
	 * @param itemText
	 * @return
	 */
	public MenuNode getNode(String itemCode) {
		return getNode(itemCode,this.menuData);
	}
	
	private MenuNode getNode(String itemCode,List<MenuNode> menuData) {
		if (menuData == null) {
			return null;
		}
		for (int i=0; i<menuData.size(); i++) {
			if (menuData.get(i).code.equals(itemCode)) {
				return menuData.get(i);
			}
	
			MenuNode node = getNode(itemCode,menuData.get(i).children);
			if (node != null) {
				return node;
			}
		}
		return null;
	}
	
	/**
	 * 更新菜单项的编码，如果菜单项的编码为空，则将其设置为菜单项的名称
	 * @param menuData
	 */
	private void updateItemCode(List<MenuNode> menuData) {
		if (menuData == null) {
			return ;
		}
		for (int i=0; i<menuData.size(); i++) {
			if ("".equals(menuData.get(i).code)) {
				menuData.get(i).code = menuData.get(i).text;
			}	
			updateItemCode(menuData.get(i).children);
		}
	}
	
	//加载菜单(递归方法)
	private void loadMenu(MenuNode parent) {
		List<MenuNode> data = parent == null ? this.menuData : parent.children;
		Menu parentMenu = parent == null ? this.menu : parent.menuItem.getMenu();
		
		for (int i=0; i<data.size(); i++) {
			MenuNode node = data.get(i);
			if (SEPARATOR.equals(node.text)) {
				//分隔线
				node.menuItem = new MenuItem(parentMenu,SWT.SEPARATOR);				
			}else if (node.children != null && 0 < node.children.size()) {//存在子菜单的菜单
				node.menuItem = new MenuItem(parentMenu,SWT.CASCADE);
				node.menuItem.setText(node.text);
				
				//创建子菜单
				Menu childMenu = new Menu(this.menu.getParent(),SWT.DROP_DOWN);
				node.menuItem.setMenu(childMenu);
				
				//加载子菜单
				loadMenu(node);
			} else {//没有子菜单的子菜单，既可以绑定事件的菜单				
				node.menuItem = new MenuItem(parentMenu,node.style);
				node.menuItem.setText(node.text);
				node.menuItem.setData(node);
				
				//"编码"菜单下的“OTHER”子菜单特殊处理，不响应点击事件
				if ("OTHER".equalsIgnoreCase(node.text)) {
					node.menuItem.setEnabled(false);
				} else {
					//绑定点击事件
					node.menuItem.addSelectionListener(new SelectionAdapter() {
						@Override 
					    public void widgetSelected(SelectionEvent e) {
					    	MenuNode curNode = (MenuNode)e.widget.getData();				    	
					    	AppAction.onMenuResponse(curNode);
					    }
						
						@Override  
			            public void widgetDefaultSelected(SelectionEvent arg0) {  
			            }  
					});
				}
			}
		}
	}
	
	/**
	 * 加载"./widget/"文件夹下的小工具
	 */
	private void loadWidgetMenu() {
		//该功能仅支持windows平台
		if (!AppFrame.isWindows) {
			return ;
		}
		
		String folderPath = System.getProperty("user.dir")+"/widget";
		File file = new File(folderPath);
		if (!file.exists()) {
			return ;
		}
		
		//支持的可执行的文件类型
		String[] supportType = new String[]{"exe","chm","lnk","bat"};
		ArrayList<String> lstPath = new ArrayList<String>();
		File[] files = file.listFiles();
		for (int i=0; i<files.length; i++) {
			if (!files[i].isFile()) {
				continue;
			}
			files[i].getPath();			
			
			for (int j = supportType.length-1;j>-1; j--) {
				if (files[i].getPath().toLowerCase().endsWith("."+supportType[j])) {
					lstPath.add(files[i].getPath());
					break;
				}
			}
		}
		
		if (lstPath.size() < 1) {
			return ;
		}
		
		MenuItem widgetMenu = new MenuItem(this.menu,SWT.CASCADE);
		widgetMenu.setText("Widget");
		
		//创建下拉菜单
		Menu childMenu = new Menu(this.menu.getParent(),SWT.DROP_DOWN);
		widgetMenu.setMenu(childMenu);
		
		//创建分割条
		new MenuItem(childMenu,SWT.SEPARATOR);
		MenuItem item;
		for (int i=0; i<lstPath.size(); i++) {
			item = new MenuItem(childMenu,SWT.PUSH);
			item.setText(getFileName(lstPath.get(i),false));
			item.setData(lstPath.get(i));
			item.addSelectionListener(new SelectionAdapter() {
				@Override 
			    public void widgetSelected(SelectionEvent e) {
			    	String path = e.widget.getData().toString();				    	
			    	ProcessBuilder pb;
			    	try {			    		
			    		if (path.toLowerCase().endsWith("chm")) {
			    			//hh.exe是Windows系统中读取chm文件的程序
			    			pb = new ProcessBuilder(new String[]{"hh.exe", path});
			    		} else if (path.toLowerCase().endsWith("lnk")) {
			    			pb = new ProcessBuilder("cmd", "/c", path);
			    		} else {
			    			pb = new ProcessBuilder(new String[]{path});
			    		}
			    		pb.start();
			    	} catch (Exception ep) {
			    		AppFrame.messageBox("异常");
			    	}
			    }
				
				@Override  
	            public void widgetDefaultSelected(SelectionEvent arg0) {  
	            }  
			});
		}
	}
	
	private static String getFileName(String filePath,boolean hasSuffix) {
		String fileName;
		int index = filePath.lastIndexOf('\\');
		if (index < 1) {
			fileName = filePath;
		} else {
			fileName = filePath.substring(index+1);
		}
		
		if (!hasSuffix) {
			index = fileName.lastIndexOf('.');
			if (index > -1) {
				fileName = fileName.substring(0,index);
			}
		}
		
		return fileName;
	}
}
