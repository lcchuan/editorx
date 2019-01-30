package lcc.utils.swt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * org.eclipse.swt.widgets.Table的帮助类
 * 提供数据加载功能
 * @author lcc
 *
 */
public class TableHelp {	
	public static class Column{
		//列对应数据表的字段名
		public String field = null;
		//列的标题
		public String title = null;
		//列宽
		public int width = 100;
		//是否为序号列
		public boolean isSerialCol = false;
		//该字段是否作为主键，如果设置为true,该字段的值将作为所在行的标识（getData()）
		public boolean isPrimaryKey = false;
		//列的align ,默认居中
		public int align = SWT.CENTER;
		
		public Column(String field,String title,int width,boolean isSerialCol,boolean isPrimaryKey) {
			this.field = field;
			this.title = title;
			this.width = width;
			this.isSerialCol = isSerialCol;
			this.isPrimaryKey = isPrimaryKey;
		}
		
		public Column(String field,String title,int width,int align) {
			this.field = field;
			this.title = title;
			this.width = width;
			this.align = align;
		}
	}
	
	public static void setColumnData(Table table,Column[] columns) {
		table.removeAll();
		TableColumn[] tableColumns = table.getColumns();
		for (int i=tableColumns.length-1; i>-1; i--) {
			tableColumns[i].dispose();
		}
		
 		for (int i=0; i<columns.length; i++) {
			TableColumn tableColumn = new TableColumn(table, columns[i].align);
			if (columns[i].isSerialCol) {//序号列
				tableColumn.setData("isSerialCol",true);
				tableColumn.setData("isPrimaryKey",false);
				tableColumn.setData("field","");
				tableColumn.setWidth(30);
			} else {
				tableColumn.setData("isSerialCol",false);
				tableColumn.setData("isPrimaryKey",columns[i].isPrimaryKey);
				tableColumn.setData("field",columns[i].field);
				tableColumn.setText(columns[i].title);
				tableColumn.setWidth(columns[i].width);
			}
		}
	}
	
	/*
	 * 设置列数据，并创建列
	 */
	public static void setColumnData(Table table,ArrayList<Column> columns) {
		setColumnData(table,columns.toArray(new Column[]{}));
	}
	
	//重置序号列数据
	public static void resetSerial(Table table) {
		final int colCount = table.getColumnCount();
		int serial = -1;
		
		for (int i=colCount-1; i>-1; i--) {
			TableColumn column = table.getColumn(i);
			if ((boolean)column.getData("isSerialCol")) {
				serial = i;
				break;
			}
		}
		if (serial < 0) {
			return ;
		}
		
		for (int i=table.getItemCount()-1; i>-1; i--) {
			table.getItem(i).setText(serial, String.valueOf(i+1));
		}
	}
	
	//加载数据
	public static boolean loadData(Table table,List<Map<String,Object>> data) {
		//移除table的所有数据
		table.removeAll();
		
		final int colCount = table.getColumnCount();
		String primaryKey = null;
		
		/*
		 * 1、验证列数据是否有效,如果存在无效的列，则直接返回false
		 * 2、搜索主键字段  isPrimaryKey
		 */
		for (int i=colCount-1; i>-1; i--) {
			TableColumn column = table.getColumn(i);
			if (column.getData("isSerialCol") == null || column.getData("field") == null) {
				return false;
			}
			if ((boolean)column.getData("isPrimaryKey")) {
				primaryKey = column.getData("field").toString();
			}
		}
		
		for (int i=0; i<data.size(); i++) {
			String[] rowData = new String[colCount];
			for (int j=0; j<colCount; j++) {
				TableColumn column = table.getColumn(j);
				if ((boolean)column.getData("isSerialCol")) {//序号列
					rowData[j] = String.valueOf(i+1);
				} else {
					Object value = data.get(i).get(column.getData("field").toString());
					rowData[j] = value == null ? null : value.toString();
				}
			}
			TableItem item = addRow(table,rowData);
			//设置行ID
			if (primaryKey != null) {
				item.setData(data.get(i).get(primaryKey));
			}
		}
		
		return true;
	}
	
	/**
	 * 向列表中新增一行，插入列表的最后
	 * @param table
	 * @param rowData
	 * @return 返回新插入的行
	 */
	public static TableItem addRow(Table table,String[] rowData) {
		TableItem item = new TableItem(table, SWT.NONE);
		item.setText(rowData);
		return item;
	}
	
	/**
	 * 向列表中加入空数据，有时，为了页面美观需要增加空行，以使列表及页面饱满
	 * @param table table
	 * @param rowCount 需要增加的空行数
	 * @param clearTable 是否需要清空列表
	 */
	public static void addNullData(Table table,int rowCount,boolean clearTable) {
		if (clearTable) {
			table.removeAll();
		}
		
		final int colCount = table.getColumnCount();
		String[] rowData = new String[colCount];
		for (int i=0; i<rowData.length; i++) {
			rowData[i] = "";
		}
		
		for (int i=0; i<rowCount; i++) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(rowData);
		}
	}
	
	/*
	 * 通过行ID获取行索引（从0开始）
	 * @param table table
	 * @param rowId 行ID,详情请参见TableHelp.loadData()中的"item.setData"部分
	 * @return 行索引（从0开始）；未搜索到，返回-1；
	 */
	public static int getRowIndex(Table table,String rowId) {
		for (int i=table.getItemCount()-1; i>-1; i--) {
			if (rowId.equals(table.getItem(i).getData())) {
				return i;
			}
		}
		return -1;
	}
}
