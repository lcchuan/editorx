package lcc.editorx.widget;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ColorManager;
import lcc.editorx.frame.ImageManager;
import lcc.utils.swt.TableHelp;

public class DlgCanteenRecord extends Dialog {
	//早晨余额
	private Text txtBalance = null;
	//当前日期
	protected DateTime canlander = null;
	//消费人
	protected Combo cmbPerson = null;
	protected String[] reservColumns = new String[]{"其它","未知","余额","充值","备注"};
	//单次消费
	private Text txtExpense = null;
	//备注
	private Text txtRemark = null;
	
	protected Button btnSet = null;
	
	protected Font fontBold = new Font(AppFrame.getInstance().getDisplay(),"宋体",10,SWT.BOLD);
	
	//结算列表
	protected Table tablePayment = null;
	
	//当月的表格展示
	protected Label title = null;
	protected Table diagram = null;
	
	//校验文本框仅能输入数字与小数点
	protected VerifyListener numberVerifyListener = null;
	//编辑框单击后选中全部文字
	protected MouseListener txtClickSelectAllListener = null;
	
	protected static final String MAIN_PERSON = "李长川";
	protected static final String DATABASE_PATH = "C:/Users/lcc/OneDrive/canteen.db";

	public DlgCanteenRecord() {
		super(AppFrame.getInstance().getShell());

		this.numberVerifyListener = new VerifyListener(){
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
		};
		
		this.txtClickSelectAllListener = new MouseListener(){
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
				((Text)arg0.widget).selectAll();
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
			}			
		};
	}
	
	/**
	 * 设置标题栏名称与图标
	 */
	@Override  
	protected void configureShell(Shell newShell) { 
	    super.configureShell(newShell);  
	    newShell.setText("南瑞食堂记录");
	    if (ImageManager.getInstance().get("EditorX.ico") != null) {
	    	newShell.setImage(ImageManager.getInstance().get("canteen.ico"));
		}
	}
	
	/**
	 * 实现非模式对话框
	 */
	@Override  
	protected int getShellStyle() {		
		return SWT.MODELESS | SWT.CLOSE;
	}
	
	/**
	 * 设置窗体大小
	 */
	@Override  
	protected Point getInitialSize() { 
	    return new Point(lcc.utils.swt.Util.zoomForHignGDI(1180),lcc.utils.swt.Util.zoomForHignGDI(700));  
	}
	
	/**
	 * 重写父类方法的意图时，将“ok”，“cancel”改为“确认”、“取消”
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	    //createButton(parent, 0, "退出",true);
	}
	
	/**
	 * 创建对话框内容
	 */
	@Override
	protected Control createDialogArea(Composite parent) {				
		Composite composite = new Composite(parent, 0);
		
		int top = 5;
		
		//当前日期
		this.canlander = new DateTime(composite,SWT.CALENDAR);
		this.canlander.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,top,260,150));
		this.canlander.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetDefaultSelected(SelectionEvent paramSelectionEvent) {
			}

			@Override
			public void widgetSelected(SelectionEvent paramSelectionEvent) {
				final SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
				Calendar c = getSelDate();
				if (!sdfMonth.format(c.getTime()).equals(sdfMonth.format(((Calendar)diagram.getData()).getTime()))) {
					drawDiagram(c);
				}
			}
			
		});
		
		//昨日余额
		top += 153;
		Label label = new Label(composite, SWT.NONE);
		label.setText("早饭余额");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,top,50,20));
		this.txtBalance = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.txtBalance.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70,top,200,20));
		//输入检验，仅接受正整数与小数点
		this.txtBalance.addVerifyListener(this.numberVerifyListener);
		this.txtBalance.addMouseListener(this.txtClickSelectAllListener);
		
		//费用人
		top += 23;
		label = new Label(composite, SWT.NONE);
		label.setText("消费人");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,top,50,20));
		this.cmbPerson = new Combo(composite,SWT.NONE);
		this.cmbPerson.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70,top,200,20));
		this.cmbPerson.addSelectionListener(new SelectionListener(){
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent e) {
				if ("充值".equals(((Combo)e.widget).getText())) {
					txtExpense.setText("700");
				}
			}
		});
		
		//费用
		top += 23;
		label = new Label(composite, SWT.NONE);
		label.setText("单次消费");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,top,50,20));
		this.txtExpense = new Text(composite,SWT.SINGLE|SWT.BORDER);
		this.txtExpense.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70,top,200,20));
		//输入检验，仅接受正整数与小数点
		this.txtExpense.addVerifyListener(this.numberVerifyListener);
		this.txtExpense.addMouseListener(this.txtClickSelectAllListener);
		this.txtExpense.setFocus();
		
		top += 23;
		label = new Label(composite, SWT.NONE);
		label.setText("当日备注");
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,top,50,20));
		this.txtRemark = new Text(composite,SWT.MULTI|SWT.WRAP|SWT.BORDER);
		this.txtRemark.addMouseListener(this.txtClickSelectAllListener);
		this.txtRemark.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70,top,200,80));
		
		//“记录”按钮
		top += 85;
		this.btnSet = new Button(composite,SWT.PUSH);
		this.btnSet.setText("记  录");
		this.btnSet.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,top,110,23));
		this.btnSet.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onSetClick();
			}
		});
		Button btn = new Button(composite,SWT.PUSH);
		btn.setText("清除某天某人记录");
		btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(130,top,110,23));
		btn.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onClearExpenseClick();
			}
		});
		
		top += 28;
		label = new Label(composite, SWT.BORDER);
		label.setBackground(ColorManager.getInstance().get(ColorManager.RGB_BLACK));
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(6,top,270,2));
		
		top += 5;
		this.tablePayment = new Table(composite, SWT.SINGLE|SWT.FULL_SELECTION|SWT.BORDER|SWT.V_SCROLL);
		this.tablePayment.setHeaderVisible(true);// 设置隐藏表头
		this.tablePayment.setLinesVisible(true);// 设置显示表格线
		this.tablePayment.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,top,250,150));
		TableHelp.setColumnData(this.tablePayment
				, new TableHelp.Column[]{new TableHelp.Column("","",lcc.utils.swt.Util.zoomForHignGDI(22),SWT.CENTER)
						               , new TableHelp.Column("待结算月份","待结算月份",lcc.utils.swt.Util.zoomForHignGDI(120),SWT.CENTER)
						               , new TableHelp.Column("费用","费用",lcc.utils.swt.Util.zoomForHignGDI(80),SWT.CENTER)}
		);
		
		top += 160;
		btn = new Button(composite,SWT.PUSH);
		btn.setText("结  算");
		btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(70,top,110,23));
		btn.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				onRechargeClick();
			}
		});
		
		//当月消费图表
		this.title = new Label(composite, SWT.NONE|SWT.CENTER);
		this.title.setFont(this.fontBold);
		this.title.setBounds(lcc.utils.swt.Util.zoomForHignGDI(280,5,880,20));
		this.diagram = new Table(composite, SWT.SINGLE|SWT.FULL_SELECTION|SWT.BORDER|SWT.H_SCROLL|SWT.V_SCROLL);
		this.diagram.setHeaderVisible(false);// 设置隐藏表头
		this.diagram.setLinesVisible(true);// 设置显示表格线
		this.diagram.setBounds(lcc.utils.swt.Util.zoomForHignGDI(280,25,880,630));
		
		//加载数据
		showPaymentInfo();
		drawDiagram(getSelDate());
		
	    applyDialogFont(composite);
	    return composite;
    }
	
	//显示待结算信息
	protected void showPaymentInfo() {
		TableItem[] items = this.tablePayment.getItems();
		for (int i=0; i<items.length; i++) {
			if (items[i].getData() != null) {
				((TableEditor)items[i].getData()).getEditor().dispose();
				((TableEditor)items[i].getData()).dispose();
			}
		}
		this.tablePayment.removeAll();
		
		List<Map<String,Object>> datas = loadPaymentInfo();
		if (datas == null) {
			AppFrame.messageBox("获取待结算数据失败！");
			return ;
		}
		BigDecimal totalExpanse = BigDecimal.valueOf(0);
		for (int i=0; i<datas.size(); i++) {
			totalExpanse = totalExpanse.add(new BigDecimal(toString(datas.get(i).get("total_expense"))));
			TableItem item = TableHelp.addRow(this.tablePayment
					, new String[]{"",toString(datas.get(i).get("month")),toString(datas.get(i).get("total_expense"))});
			
			//设置复选框
			TableEditor editor = new TableEditor(this.tablePayment);
	        Button button = new Button(this.tablePayment, SWT.CHECK);
	        button.pack();
	        editor.minimumWidth = button.getSize().x;
	        editor.horizontalAlignment = SWT.CENTER;
	        editor.setEditor(button, item, 0);
	        item.setData(editor);
	        button.addSelectionListener(new SelectionAdapter() {
	            public void widgetSelected(SelectionEvent event) {
	            	//选择复选框时，实时计算待结算的总费用
	            	BigDecimal totalExpanse = BigDecimal.valueOf(0);
	                TableItem[] items = tablePayment.getItems();
	                for (int i=0; i<items.length; i++) {
	                	if (items[i].getData() == null) {
	                		items[i].setText(2, totalExpanse.toString());
	                		break;
	                	} else {
	                		Button check = (Button)((TableEditor)items[i].getData()).getEditor();
		                	if (check.getSelection()) {
		                		totalExpanse = totalExpanse.add(new BigDecimal(items[i].getText(2)));
		                	}
	                	}	                	
	                }
	            }
	        });
		}
		
		//总计行
		TableItem item = TableHelp.addRow(this.tablePayment, new String[]{"","总计("+totalExpanse+")","0"});
		//设置总计行的样式为红色加粗字体
		item.setFont(this.fontBold);
		item.setForeground(ColorManager.getInstance().get(new RGB(255,0,0)));
	}
	
	protected void drawDiagram(Calendar date) {
		List<Map<String,Object>> datas = loadData(date);
		if (datas == null) {
			AppFrame.messageBox("获取当月数据失败！");
			datas = new ArrayList<Map<String,Object>>();
		}
		List<Map<String,Object>> balances = loadBalance(date);
		if (balances == null) {
			AppFrame.messageBox("获取当月余额数据失败！");
			return ;
		}
		List<Map<String,Object>> lstMonthData = loadMonthData(new Calendar[]{date});
		if (lstMonthData == null) {
			AppFrame.messageBox("获取当月结算数据失败！");
			return ;
		}
		
		//计算指定月份共有多少天
		final int dayCountInMonth = date.getActualMaximum(Calendar.DAY_OF_MONTH);
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		final SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
		
		//设置标题
		String diagramTitle = sdfMonth.format(date.getTime())+"  ";
		if (lstMonthData.size() < 1 || lstMonthData.get(0).get("payment_date") == null) {
			diagramTitle += "尚未结算";
		} else {
			diagramTitle += "已于"+sdf.format(((Date)lstMonthData.get(0).get("payment_date")).getTime())+"结算";
		}
		this.title.setText(diagramTitle);
		
		//获取列信息
		boolean match = false;
		ArrayList<TableHelp.Column> columns = new ArrayList<TableHelp.Column>();
		for (int i=0; i<datas.size(); i++) {
			String name = toString(datas.get(i).get("person"));
			match = false;
			for (int j=0; j<columns.size(); j++) {
				if (name.equals(columns.get(j).field)) {
					match = true;
					break;
				}
			}
			if (!match) {
				columns.add(new TableHelp.Column(name,name,lcc.utils.swt.Util.zoomForHignGDI(70),SWT.CENTER));
			}
		}
		//MAIN_PERSON排在第一位
		match = false;
		for (int i=0; i<columns.size(); i++) {
			if (MAIN_PERSON.equals(columns.get(i).field)) {				
				if (i > 0) {
					columns.add(0, columns.remove(i));
				}
				match = true;
				break;
			}
		}
		if (!match) {
			columns.add(0, new TableHelp.Column(MAIN_PERSON,MAIN_PERSON,lcc.utils.swt.Util.zoomForHignGDI(60),SWT.CENTER));
		}
		//"其它","未知","充值","备注"等保留列依次排在最后
		for (int i=0; i<reservColumns.length; i++) {
			match = false;
			for (int j=0; j<columns.size(); j++) {
				if (reservColumns[i].equals(columns.get(j).field)) {
					if (j < columns.size()-1) {
						columns.add(columns.remove(j));
					}
					match = true;
					break;
				}
			}
			if (!match) {
				columns.add(new TableHelp.Column(reservColumns[i],reservColumns[i],lcc.utils.swt.Util.zoomForHignGDI(50),SWT.CENTER));
			}
		}
		columns.add(0, new TableHelp.Column("日期","日期",lcc.utils.swt.Util.zoomForHignGDI(40),SWT.CENTER));
		//设置"备注"列的列宽
		columns.get(columns.size()-1).width = lcc.utils.swt.Util.zoomForHignGDI(100);
		
		//设置人员下拉框中的内容
		this.cmbPerson.removeAll();
		for (int i=0; i<columns.size(); i++) {
			if ("日期".equals(columns.get(i).field) 
				|| "未知".equals(columns.get(i).field) 
				|| "余额".equals(columns.get(i).field) 
				|| "备注".equals(columns.get(i).field)) {
				continue;
			}
			this.cmbPerson.add(columns.get(i).field);
		}
		this.cmbPerson.select(0);
		
		String[] rowData = null;
		TableHelp.setColumnData(this.diagram, columns);
		//设置表头，为了自定义表头的样式，所以将第一行作为表头，并设置setHeaderVisible(false)
		rowData = new String[columns.size()];
		for (int i=0; i<columns.size(); i++) {
			rowData[i] = columns.get(i).field;
		}
		TableHelp.addRow(this.diagram, rowData);
		//填充图表内容
		final int colCount = columns.size();
		for (int i=0; i<dayCountInMonth; i++) {
			final Integer day = Integer.valueOf(i+1);
			rowData = new String[colCount];
			rowData[0] = String.valueOf(day);
			//设置余额与备注
			for (int j=0; j<balances.size(); j++) {
				if (day.equals(balances.get(j).get("day"))) {
					rowData[colCount-1] = toString(balances.get(j).get("remark"));
					rowData[colCount-3] = toString(balances.get(j).get("balance"));
					break;
				}
			}
			//设置人员消费信息
			for (int j=1; j<columns.size(); j++) {
				final String colText = columns.get(j).field;
				if ("余额".equals(colText) || "备注".equals(colText)) {
					continue;
				}
				for (int x=0; x<datas.size(); x++) {
					if (day.equals(datas.get(x).get("day")) && colText.equals(datas.get(x).get("person"))) {
						rowData[j] = toString(datas.get(x).get("expense"));
						break;
					}
				}
			}
			
			TableHelp.addRow(this.diagram, rowData);
		}
		//设置统计行
		rowData = new String[colCount];
		rowData[0] = "个人";
		BigDecimal totalExpanse = BigDecimal.valueOf(0);
		for (int i=1; i<columns.size(); i++) {
			if ("余额".equals(columns.get(i).field)) {
				break;
			}
			BigDecimal personExpanse = BigDecimal.valueOf(0);
			for (int j=1; j<=dayCountInMonth; j++) {
				String expense = this.diagram.getItem(j).getText(i);
				if (!"".equals(expense)) {
					personExpanse = personExpanse.add(new BigDecimal(expense));
				}
			}
			totalExpanse = totalExpanse.add(personExpanse);
			rowData[i] = personExpanse.toString();
		}
		TableHelp.addRow(this.diagram, rowData);
		rowData = new String[]{"总计",totalExpanse.toString()};
		TableHelp.addRow(this.diagram, rowData);
		this.diagram.setData(date);
		
		//设置图表的内容样式
		setDiagramStyle();
	}
	
	//设置图表的内容样式
	protected void setDiagramStyle() {
		//设置表头的样式，该表头是虚拟的表头，将第一行作为表头
		final int colCount = this.diagram.getColumnCount();
		final RGB gray = new RGB(235,235,235);
		final RGB green = new RGB(146,208,80);
		TableItem titleItem = this.diagram.getItem(0);
		titleItem.setFont(this.fontBold);
		titleItem.setBackground(ColorManager.getInstance().get(gray));
		for (int i=0; i<colCount; i++) {
			if ("余额".equals(this.diagram.getColumn(i).getText()) || "充值".equals(this.diagram.getColumn(i).getText())) {
				titleItem.setForeground(i, ColorManager.getInstance().get(ColorManager.RGB_RED));
			}
		}
		
		Calendar date = (Calendar)this.diagram.getData();
		TableItem[] items = this.diagram.getItems();
		for (int i=1; i<items.length; i++) {
			//设置首列字体加粗,背景置灰
			items[i].setFont(0,this.fontBold);
			items[i].setBackground(0,ColorManager.getInstance().get(gray));
			try {
				date.set(Calendar.DAY_OF_MONTH, Integer.valueOf(items[i].getText(0)));
				//周末的行背景设置为绿色
				if (date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
					items[i].setBackground(ColorManager.getInstance().get(green));
				}
				
			} catch (Exception e) {
				//合计行设置为红色加粗字体
				items[i].setFont(this.fontBold);
				items[i].setForeground(0,ColorManager.getInstance().get(ColorManager.RGB_RED));
				items[i].setBackground(ColorManager.getInstance().get(gray));
			}
		}
	}
	
	//结算按钮响应事件
	protected void onRechargeClick() {
		List<String> lstMonth = new ArrayList<String>();
		TableItem[] items = tablePayment.getItems();
        for (int i=0; i<items.length; i++) {
        	if (items[i].getData() == null) {
        		break;
        	} else {
        		Button check = (Button)((TableEditor)items[i].getData()).getEditor();
            	if (check.getSelection()) {
            		lstMonth.add(items[i].getText(1));
            	}
        	}	                	
        }
        if (lstMonth.size() < 1) {
        	AppFrame.messageBox("请选择要结算的月份");
        	return ;
        }
        
        StringBuilder str = new StringBuilder();
        str.append("确认结算以下月份的费用？");
        for (int i=0; i<lstMonth.size(); i++) {
        	str.append("\r");
        	str.append("   "+lstMonth.get(i));
        }
        if (!AppFrame.confirm(str.toString())) {
        	return ;
        }
        
        Connection conn = getConnection();
		if (conn == null) {
			AppFrame.messageBox("连接数据库失败");
			return ;
		}
        try {
        	Statement stat = conn.createStatement();
        	//每次最多也就结算三四个月份的费用，所以可以直接循环里执行sql
        	for (int i=0; i<lstMonth.size(); i++) {
            	str = new StringBuilder();
            	str.append("update t_canteen_MONTH set payment_date=date('now') where strftime('%Y-%m',RECORD_DATE,'localtime')='"+lstMonth.get(i)+"'");
            	if (stat.executeUpdate(str.toString()) < 1) {
                	str = new StringBuilder();
                	str.append("insert into t_canteen_MONTH(payment_date,RECORD_DATE)");
                	str.append(" values(date('now'),date('"+lstMonth.get(i)+"-01'))");
                	stat.executeUpdate(str.toString());
            	}
            }
        } catch (Exception e) {
			e.printStackTrace();
			AppFrame.messageBox("结算失败");
			return ;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
        
        this.showPaymentInfo();
	}
	
	//“清除”按钮响应事件，清除某天某人的消费记录
	protected void onClearExpenseClick() {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		final Calendar date = this.getSelDate();
		String person = cmbPerson.getText().trim();
		
		//验证消费人
		if (!validatePerson(person)) {
			AppFrame.messageBox("消费人不可为["+person+"]");
			return ;
		}
		if (!AppFrame.confirm("确认清除"+person+"于"+sdf.format(date.getTime())+"的消费记录？")) {
			return ;
		}
		
		Connection conn = getConnection();
		if (conn == null) {
			AppFrame.messageBox("连接数据库失败");
			return ;
		}
		
		try {
			Statement stat = conn.createStatement();
			String sql = "delete from T_CANTEEN_RECORD";
			sql += " where PERSON='"+person+"' and strftime('%Y-%m-%d',RECORD_DATE,'localtime')='"+sdf.format(date.getTime())+"'";
			stat.executeUpdate(sql);
		} catch (Exception e) {
			e.printStackTrace();
			AppFrame.messageBox("清除失败");
			return ;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
		
		this.drawDiagram(date);
	}
	
	//"记录"按钮点击响应事件
	protected void onSetClick() {
		/*
		 * 直接用Double做加减法运算时，经常会得出意外的结果，例如13.8-13.8有时会等于0.000000000018之类而不是等于0
		 * 所以只能用BigDecimal进行计算
		 */
		//昨日余额
		BigDecimal yestodayBalance = null;
		BigDecimal expense = null;
		String person = cmbPerson.getText().trim();
		String remark = txtRemark.getText();
		
		if (!"".equals(txtExpense.getText().trim())) {
			try {
				expense = new BigDecimal(txtExpense.getText().trim());
			} catch (Exception e) {
				AppFrame.messageBox("无效的单次消费输入");
				return ;
			}
		}
		if (!"".equals(txtBalance.getText().trim())) {
			try {
				yestodayBalance = new BigDecimal(txtBalance.getText().trim()).add(expense);
			} catch (Exception e) {
				AppFrame.messageBox("无效的早饭余额输入");
				return ;
			}
		}
		
		Connection conn = getConnection();
		if (conn == null) {
			AppFrame.messageBox("连接数据库失败");
			return ;
		}
		
		final Calendar date = getSelDate();
		try {			
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			final SimpleDateFormat sdfMonth = new SimpleDateFormat("yyyy-MM");
			final String strDate = sdf.format(date.getTime());			

			//输入验证
			if (!validate(conn,date
					,(yestodayBalance == null ? null : yestodayBalance.doubleValue())
					,(expense == null ? null : expense.doubleValue())
					,person)) {
				return ;
			}
			
			Statement stat = conn.createStatement();
			StringBuilder sql;
			
			//消费记录表 ： 先update,如果没记录则插入
			if ("充值".equals(person)) {
				//删除之前的充值，确保每月仅充值一次
				sql = new StringBuilder();
				sql.append("delete from T_CANTEEN_RECORD");
				sql.append(" where PERSON='"+person+"' and strftime('%Y-%m',RECORD_DATE,'localtime')='"+sdfMonth.format(date.getTime())+"'");
				stat.executeUpdate(sql.toString());
			}
			sql = new StringBuilder();
			sql.append("update T_CANTEEN_RECORD");
			sql.append(" set EXPENSE=(case when EXPENSE is null then "+expense+" else (EXPENSE+"+expense+") end)");
			sql.append(" where PERSON='"+person+"'");
			sql.append(" and strftime('%Y-%m-%d',RECORD_DATE,'localtime')='"+strDate+"'");
			if (stat.executeUpdate(sql.toString()) < 1) {
				sql = new StringBuilder();
				sql.append("insert into T_CANTEEN_RECORD(PERSON,RECORD_DATE,EXPENSE)");
				sql.append("values('"+person+"',date('"+strDate+"'),"+expense+")");
				stat.executeUpdate(sql.toString());
			}	
			
			//余额记录表-备注： 先update,如果没记录则插入
			if (!"".equals(remark)) {
				sql = new StringBuilder();
				sql.append("update T_CANTEEN_BALANCE set REMARK='"+remark+"'");
				sql.append(" where strftime('%Y-%m-%d',RECORD_DATE,'localtime')='"+strDate+"'");
				if (stat.executeUpdate(sql.toString()) < 1) {
					sql = new StringBuilder();
					sql.append("insert into T_CANTEEN_BALANCE(RECORD_DATE,REMARK)");
					sql.append("values(date('"+strDate+"'),'"+remark+"')");
					stat.executeUpdate(sql.toString());
				}
			}
			
			//余额记录表-昨日余额： 先update,如果没记录则插入
			if (yestodayBalance != null) {
				Calendar yestoday = Calendar.getInstance();
				yestoday.setTime(date.getTime());
				yestoday.add(Calendar.DAY_OF_MONTH, -1);
				
				//yestodayBalance == 0 =>清空昨日余额
				sql = new StringBuilder();
				sql.append("update T_CANTEEN_BALANCE set BALANCE="+(Double.valueOf(0).equals(yestodayBalance) ? null : yestodayBalance));
				sql.append(" where strftime('%Y-%m-%d',RECORD_DATE,'localtime')='"+sdf.format(yestoday.getTime())+"'");
				if (stat.executeUpdate(sql.toString()) < 1 && !Double.valueOf(0).equals(yestodayBalance)) {
					sql = new StringBuilder();
					sql.append("insert into T_CANTEEN_BALANCE(RECORD_DATE,BALANCE)");
					sql.append("values(date('"+sdf.format(yestoday.getTime())+"'),"+yestodayBalance+")");
					stat.executeUpdate(sql.toString());
				}
				
				//昨日的“未知”消费 
				BigDecimal expenseUnknown = null;
				if ((Double.valueOf(0).equals(yestodayBalance))) {
					//yestodayBalance == 0 =>清空昨日余额 同时清除昨日的“未知”消费
					expenseUnknown = BigDecimal.valueOf(0);
				} else {
					//获取上次余额以及之后的总消费
					sql = new StringBuilder();
					sql.append("select BALANCE,RECORD_DATE from T_CANTEEN_BALANCE where strftime('%Y-%m-%d',RECORD_DATE,'localtime')<'"+sdf.format(yestoday.getTime())+"'");
					sql.append(" and BALANCE is not null");
					sql.append(" order by RECORD_DATE desc limit 1");
					ResultSet rs = stat.executeQuery(sql.toString());
					if (rs.next()) {
						BigDecimal preBalance = new BigDecimal(rs.getObject(1).toString());
						Date preDate = convertToDate(rs.getObject(2));
						expenseUnknown = preBalance.subtract(yestodayBalance);
						
						//计算总消费
						sql = new StringBuilder();
						sql.append("select round(sum(case when PERSON='充值' then (0-EXPENSE)");
						sql.append(" when person='未知' and strftime('%Y-%m-%d',RECORD_DATE,'localtime')='"+sdf.format(yestoday.getTime())+"' then 0");
						sql.append(" else EXPENSE end),2) total_EXPENSE");
						sql.append(" from T_CANTEEN_RECORD");
						sql.append(" where '"+sdf.format(preDate.getTime())+"'<strftime('%Y-%m-%d',RECORD_DATE,'localtime')");
						sql.append(" and strftime('%Y-%m-%d',RECORD_DATE,'localtime')<='"+sdf.format(yestoday.getTime())+"'");
						rs = stat.executeQuery(sql.toString());
						if (rs.next()) {
							expenseUnknown = expenseUnknown.subtract(new BigDecimal(rs.getObject(1).toString()));
						}
					}
				}
				
				//更新昨日的“未知”消费
				if (expenseUnknown != null) {
					if (BigDecimal.valueOf(0).equals(expenseUnknown)) {
						sql = new StringBuilder();
						sql.append("delete from T_CANTEEN_RECORD");
						sql.append(" where PERSON='未知'");
						sql.append(" and strftime('%Y-%m-%d',RECORD_DATE,'localtime')='"+sdf.format(yestoday.getTime())+"'");
						stat.executeUpdate(sql.toString());
					} else {
						sql = new StringBuilder();
						sql.append("update T_CANTEEN_RECORD");
						sql.append(" set EXPENSE="+expenseUnknown);
						sql.append(" where PERSON='未知'");
						sql.append(" and strftime('%Y-%m-%d',RECORD_DATE,'localtime')='"+sdf.format(yestoday.getTime())+"'");
						if (stat.executeUpdate(sql.toString()) < 1) {
							sql = new StringBuilder();
							sql.append("insert into T_CANTEEN_RECORD(PERSON,RECORD_DATE,EXPENSE)");
							sql.append("values('未知',date('"+sdf.format(yestoday.getTime())+"'),"+expenseUnknown+")");
							stat.executeUpdate(sql.toString());
						}
					}
				}					
			}
		} catch (Exception e) {
			e.printStackTrace();
			AppFrame.messageBox("记录失败");
			return ;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
		
		this.drawDiagram(date);
	}
	
	protected static boolean validatePerson(String person) {
		final String[] reservNames = {"","未知","备注","余额","日期"};
		for (int i=0; i<reservNames.length; i++) {
			if (reservNames[i].equals(person)) {
				return false;
			}
		}
		return true;
	}
	
	//验证输入合法性
	protected static boolean validate(Connection conn,Calendar date,Double balance,Double expense,String person) {
		//验证消费人
		if (!validatePerson(person)) {
			AppFrame.messageBox("消费人不可为["+person+"]");
			return false;
		}
		
		//验证单次消费
		if (expense == null) {
			AppFrame.messageBox("请输入单次消费");
			return false;
		}
		if (expense > 100 && !"充值".equals(person)) {
			AppFrame.messageBox("单次消费不可超过100");
			return false;
		}
		if ("充值".equals(person) && expense != 700) {
			if (!AppFrame.confirm("充值金额不是700，是否录入错误？")) {
				return false;
			}
		}
		
		//balance == 0 则清空上一天的余额
		if (Double.valueOf(0).equals(balance)) {
			if (!AppFrame.confirm("确认清除昨日余额？")) {
				return false;
			}
			if (!Double.valueOf(0).equals(expense)) {
				AppFrame.messageBox("清除昨日余额时，单次消费必须输入0");
				return false;
			}
		}
		
		//验证余额
		if (balance != null && balance != 0) {
			if ("充值".equals(person)) {
				AppFrame.messageBox("录入早饭余额时不可选择[充值]作为消费人");
				return false;
			}
			if (expense > 20) {
				AppFrame.messageBox("录入早饭余额时,单次消费不可超过20");
				return false;
			}
			if (balance < 1000) {
				AppFrame.messageBox("余额不可低于1000");
				return false;
			}
			
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
			
			Calendar yestoday = Calendar.getInstance();
			yestoday.setTime(date.getTime());
			yestoday.add(Calendar.DAY_OF_MONTH, -1);			
			try {
				Statement stat = conn.createStatement();
				String sql = "select BALANCE from T_CANTEEN_BALANCE where strftime('%Y-%m-%d',RECORD_DATE,'localtime')='"+sdf.format(yestoday.getTime())+"'";
				ResultSet rs = stat.executeQuery(sql);
				if (rs.next() && rs.getObject(1) != null) {
					//验证昨日是否已有余额
					if (!AppFrame.confirm("余额已录入过，是否更新？")) {
						return false;
					}
				}
				
				Double recharge = 0.0;
				Double preBalance = null;
				Date preDate = null;
				//获取上次余额与当月充值
				sql = "select BALANCE,RECORD_DATE from T_CANTEEN_BALANCE where strftime('%Y-%m-%d',RECORD_DATE,'localtime')<'"+sdf.format(yestoday.getTime())+"'";
				sql += " and BALANCE is not null";
				sql += " order by RECORD_DATE desc limit 1";
				rs = stat.executeQuery(sql);
				if (rs.next()) {
					preBalance = Double.valueOf(rs.getObject(1).toString());
					preDate = convertToDate(rs.getObject(2));
					sql = "select EXPENSE from T_CANTEEN_RECORD where person='充值'";
					sql += " and strftime('%Y-%m-%d',RECORD_DATE,'localtime')>'"+sdf.format(preDate)+"'";
					sql += " limit 1";
					rs = stat.executeQuery(sql);
					if (rs.next()) {
						recharge = Double.valueOf(rs.getObject(1).toString());
					}
					if (balance >= (preBalance+recharge)) {
						AppFrame.messageBox("早饭余额不可高过上次余额与充值的和");
						return false;
					}
				}
			} catch (Exception e) {
				AppFrame.messageBox("记录失败");
				return false;
			}
		}
		
		return true;
	}
	
	protected static boolean createTable(Connection conn) {
		try {			
			Statement stat = conn.createStatement();
			String sql = "CREATE TABLE T_CANTEEN_RECORD(PERSON STRING(50) NOT NULL,RECORD_DATE DATE NOT NULL,EXPENSE NUMERIC(10,2) NOT NULL)";
			stat.addBatch(sql);
			sql = "CREATE TABLE T_CANTEEN_BALANCE(RECORD_DATE DATE NOT NULL UNIQUE,BALANCE NUMERIC(10,2),REMARK STRING(200))";
			stat.addBatch(sql);
			sql = "CREATE TABLE T_CANTEEN_MONTH(RECORD_DATE DATE NOT NULL UNIQUE,PAYMENT_DATE DATE)";
			stat.addBatch(sql);
			stat.executeBatch();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 加载某月份的数据
	 * @param date
	 * @return
	 */
	protected static List<Map<String,Object>> loadData(Calendar date) {
		Connection conn = getConnection();
		if (conn == null) {
			return null;
		}
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			String sql = "SELECT PERSON,RECORD_DATE,EXPENSE";
			sql += " FROM T_CANTEEN_RECORD WHERE strftime('%Y-%m',RECORD_DATE,'localtime')='"+sdf.format(date.getTime())+"'";
			sql += " order by PERSON";
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			
			Calendar tmp = Calendar.getInstance();
			List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
			while (rs.next()) {
				Map<String,Object> map = new HashMap<String,Object>();
				//sqlite的日期类型返回的是字符串
				Date d = convertToDate(rs.getObject(2));
				map.put("person", rs.getString(1));
				map.put("record_date", d);
				map.put("expense", rs.getObject(3));
				tmp.setTime(d);
				map.put("day", tmp.get(Calendar.DAY_OF_MONTH));
				data.add(map);
			}
			rs.close();
			return data;
		} catch (Exception e) {
			//查询失败，说明表尚未创建，创建之
			return createTable(conn) ? new ArrayList<Map<String,Object>>() : null;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 将字符串转换为时间
	 * @param date 格式 yyyy-MM-DD
	 * @return
	 * @throws ParseException 
	 */
	protected static Date convertToDate(Object date) throws ParseException {
		if (date == null) {
			return null;
		}
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.parse(date.toString());
	}
	
	//获取待结算信息
	protected static List<Map<String,Object>> loadPaymentInfo() {
		Connection conn = getConnection();
		if (conn == null) {
			return null;
		}
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("select t.month,t.total_expense");
			sql.append(" from (");
			sql.append("   select strftime('%Y-%m',RECORD_DATE,'localtime') month");
			sql.append("       , round(sum(case when PERSON='充值' then 0 else EXPENSE end),2) total_EXPENSE");
			sql.append("   from t_canteen_record");
			sql.append("   where EXPENSE is not null");
			sql.append("   group by strftime('%Y-%m',RECORD_DATE,'localtime')");
			sql.append(" ) t");
			sql.append(" where t.total_expense <> 0");
			sql.append(" and t.month not in(select strftime('%Y-%m',RECORD_DATE,'localtime') from t_canteen_MONTH where payment_Date is not null)");
			sql.append(" order by t.month");
			
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql.toString());
			List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
			while (rs.next()) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("month", rs.getString(1));
				map.put("total_expense", rs.getObject(2));
				data.add(map);
			}
			rs.close();
			return data;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
	}
	
	/**
	 * 加载月份的付款数据
	 * @param arrMonth
	 * @return
	 */
	protected static List<Map<String,Object>> loadMonthData(Calendar[] arrMonth) {
		if (arrMonth == null) {
			return null;
		}
		if (arrMonth.length < 1) {
			return new ArrayList<Map<String,Object>>();
		}
		Connection conn = getConnection();
		if (conn == null) {
			return null;
		}
		
		try {
			final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			StringBuilder sql = new StringBuilder();
			sql.append("SELECT RECORD_DATE,PAYMENT_DATE");
			sql.append(" FROM T_CANTEEN_MONTH");
			sql.append(" where strftime('%Y-%m',RECORD_DATE,'localtime') in(");
			for (int i=0; i<arrMonth.length; i++) {
				if (i > 0) {
					sql.append(",");
				}
				sql.append("'"+sdf.format(arrMonth[i].getTime())+"'");
			}
			sql.append(")");
			sql.append(" order by RECORD_DATE");
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql.toString());
			
			List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
			while (rs.next()) {
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("record_date", convertToDate(rs.getObject(1)));
				map.put("payment_date", convertToDate(rs.getObject(2)));
				data.add(map);
			}
			rs.close();
			return data;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
	}
	
	//加载余额信息
	protected static List<Map<String,Object>> loadBalance(Calendar date) {
		Connection conn = getConnection();
		if (conn == null) {
			return null;
		}
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
			String sql = "SELECT BALANCE,RECORD_DATE,REMARK";
			sql += " FROM T_CANTEEN_BALANCE WHERE strftime('%Y-%m',RECORD_DATE,'localtime')='"+sdf.format(date.getTime())+"'";
			sql += " order by RECORD_DATE";
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sql);
			
			Calendar tmp = Calendar.getInstance();
			List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
			while (rs.next()) {
				//sqlite的日期类型返回的是字符串
				Date d = convertToDate(rs.getObject(2));
				Map<String,Object> map = new HashMap<String,Object>();
				map.put("balance", rs.getObject(1));
				map.put("record_date", d);
				map.put("remark", rs.getObject(3));
				tmp.setTime(d);
				map.put("day", tmp.get(Calendar.DAY_OF_MONTH));
				data.add(map);
			}
			rs.close();
			return data;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				conn.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 获取配置数据库的链接
	 * @return
	 */
	protected static Connection getConnection() {		
		Connection conn = null;
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite://"+DATABASE_PATH);
		} catch (Exception e) {
			e.printStackTrace();
			conn = null;
		}
		return conn;
	}
	
	public Calendar getSelDate() {
		Calendar date = Calendar.getInstance();
		date.set(Calendar.YEAR, this.canlander.getYear());
		date.set(Calendar.MONTH, this.canlander.getMonth());
		date.set(Calendar.DAY_OF_MONTH, this.canlander.getDay());
		return date;
	}
	
	private static String toString(Object obj) {
		return obj == null ? "" : obj.toString().trim();
	}
}
