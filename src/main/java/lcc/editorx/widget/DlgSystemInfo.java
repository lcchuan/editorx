package lcc.editorx.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ImageManager;
import lcc.utils.SystemInfo;
import lcc.utils.SystemInfo.NetWorkInfo;
import lcc.utils.swt.TableHelp;

public class DlgSystemInfo extends Dialog {
	private Table _tableNetWork = null;
	
	public DlgSystemInfo() {
		super(AppFrame.getInstance().getShell());
	}
	
	/**
	 * 设置标题栏名称与图标
	 */
	@Override  
	protected void configureShell(Shell newShell) { 
	    super.configureShell(newShell);  
	    newShell.setText("系统信息查看");
	    if (ImageManager.getInstance().get("system.ico") != null) {
	    	newShell.setImage(ImageManager.getInstance().get("system.ico"));
		}
	} 
	
	/**
	 * 设置窗体大小
	 */
	@Override  
	protected Point getInitialSize() { 
	    return lcc.utils.swt.Util.zoomForHignGDI(800,260);  
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
		
		this._tableNetWork = new Table(composite, SWT.SINGLE|SWT.FULL_SELECTION|SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
		this._tableNetWork.setHeaderVisible(true);// 设置显示表头
		this._tableNetWork.setLinesVisible(true);// 设置显示表格线
		this._tableNetWork.setBounds(lcc.utils.swt.Util.zoomForHignGDI(5,5,786,170));
		//加载列信息
		ArrayList<TableHelp.Column> columns = new ArrayList<TableHelp.Column>();
		columns.add(new TableHelp.Column("mac_description","mac描述",lcc.utils.swt.Util.zoomForHignGDI(270),false,false));
		columns.add(new TableHelp.Column("mac_address","mac地址",lcc.utils.swt.Util.zoomForHignGDI(160),false,false));
		columns.add(new TableHelp.Column("ipv4","ipv4",lcc.utils.swt.Util.zoomForHignGDI(100),false,false));
		columns.add(new TableHelp.Column("ipv6","ipv6",lcc.utils.swt.Util.zoomForHignGDI(230),false,false));
		TableHelp.setColumnData(this._tableNetWork, columns);
		//添加右键菜单
		Menu menu = new Menu(_tableNetWork);
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText("复制");
		item.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int select = _tableNetWork.getSelectionIndex();
				if (select < 0) {
					return ;
				}
				TableItem item = _tableNetWork.getItem(select);
				String str = String.format("%s\n%s\n%s\n%s", item.getText(0),item.getText(1),item.getText(2),item.getText(3));
				
				Clipboard clipboard = new Clipboard(Display.getCurrent());
				clipboard.setContents(new Object[]{str}, new Transfer[]{TextTransfer.getInstance()});
				clipboard.dispose();
			}
		});
		_tableNetWork.setMenu(menu);			
		
	    applyDialogFont(composite);
	    
	    loadSystemInfo();
	    return composite;
    }
	
	private void loadSystemInfo() {
		Map<String,Object> data = null;
		List<Map<String,Object>> datas = new ArrayList<Map<String,Object>>();
		ArrayList<NetWorkInfo> macs = SystemInfo.getNetWorkInfo();
		if (macs == null || macs.size() < 1) {
			data = new HashMap<String,Object>();
			data.put("mac_description", "exception");
			datas.add(data);
		} else {
			for (int i=0; i<macs.size(); i++) {
				NetWorkInfo info = macs.get(i);
				data = new HashMap<String,Object>();
				data.put("mac_description", info.macDescription);
				data.put("mac_address", info.macAddress);
				data.put("ipv4", info.ipv4);
				data.put("ipv6", info.ipv6);
				datas.add(data);
			}
		}		
		TableHelp.loadData(this._tableNetWork, datas);
	}
}
