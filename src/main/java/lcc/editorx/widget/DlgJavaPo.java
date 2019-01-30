package lcc.editorx.widget;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ImageManager;
import lcc.utils.JdbcHelp.FIELD;
import lcc.utils.swt.TableHelp;

public class DlgJavaPo extends Dialog {
	private Table _tableMember = null;

	public DlgJavaPo() {
		super(AppFrame.getInstance().getShell());
	}

	/**
	 * 设置标题栏名称与图标
	 */
	@Override  
	protected void configureShell(Shell newShell) { 
	    super.configureShell(newShell);  
	    newShell.setText("生成Java Po类");
	    if (ImageManager.getInstance().get("EditorX.ico") != null) {
	    	newShell.setImage(ImageManager.getInstance().get("EditorX.ico"));
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
	    return lcc.utils.swt.Util.zoomForHignGDI(800,500);  
	}
	
	/**
	 * 重写父类方法的意图时，将“ok”，“cancel”改为“确认”、“取消”
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		Button btn = createButton(parent, 2, "生成Po类", false);
		btn.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				final int count =  _tableMember.getItemCount();
				if (count < 1) {
					AppFrame.messageBox("请先添加类成员");
					return ;
				}
				
				StringBuilder strb_member = new StringBuilder();
				StringBuilder strb_func = new StringBuilder();
				
				for (int i=0; i<count; i++) {
					String data_type = _tableMember.getItem(i).getText(0).trim();
					String member_name = _tableMember.getItem(i).getText(1).trim();
					String member_remark = _tableMember.getItem(i).getText(2).trim();
					String funcName = member_name;
					
					if ("string".equals(data_type)) {
						data_type = "String";
					} else if ("date".equals(data_type)) {
						data_type = "Date";
					}
					
					if ("".equals(data_type) && "".equals(member_name)) {
						continue;
					}
					
					if ("".equals(data_type)) {
						AppFrame.messageBox("成员变量类型不可为空(第"+(i+1)+"行)");
						return ;
					}
					if ("".equals(member_name)) {
						AppFrame.messageBox("成员变量名称不可为空(第"+(i+1)+"行)");
						return ;
					}
					
					//按驼峰法命名函数名					
					funcName = getVariableName(funcName,true);
					if ("".equals(funcName)) {
						AppFrame.messageBox("无效的成员变量名(第"+(i+1)+"行)");
						return ;
					}					
					funcName = Character.toUpperCase(funcName.charAt(0))+funcName.substring(1);
					
					//添加成员变量
					if (!"".equals(member_remark)) {
						strb_member.append("\r\n    //"+member_remark);
					}
					strb_member.append("\r\n    private "+data_type+" "+member_name+";");
					
					//添加成员函数
					strb_func.append("\r\n\r\n    public "+data_type+" get"+funcName+"(){");
					strb_func.append("\r\n        return this."+member_name+";");
					strb_func.append("\r\n    }");
					strb_func.append("\r\n    public void set"+funcName+"("+data_type+" "+member_name+"){");
					strb_func.append("\r\n        this."+member_name+" = "+member_name+";");
					strb_func.append("\r\n    }");
				}
				
				AppFrame.getInstance().createEditor().setText("public class PO{"+strb_member.toString()+strb_func.toString()+"\r\n}");
			}
		});
		
	    createButton(parent, 0, "退出",true);
	}
	
	/**
	 * 创建对话框内容
	 */
	@Override
	protected Control createDialogArea(Composite parent) {				
		Composite composite = new Composite(parent, 0);
		
		//“新增成员”按钮
		Button btn = new Button(composite, SWT.NONE);
		btn.setText("新增成员");
		btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(10,5,100,20));
		btn.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				addMemberRow();
			}
		});
		
		//“删除成员”按钮
		btn = new Button(composite, SWT.NONE);
		btn.setText("删除成员");
		btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(115,5,100,20));
		btn.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int index = _tableMember.getSelectionIndex();
				if (index < 0) {
					AppFrame.messageBox("请选择要删除的成员");
					return;
				}
				_tableMember.remove(index);
				if (_tableMember.getItemCount() <= index) {
					index--;
				}
				if (-1 < index && index < _tableMember.getItemCount()) {
					_tableMember.select(index);
				}
			}
		});
		
		//“自动生成成员”按钮
		btn = new Button(composite, SWT.NONE);
		btn.setText("自动生成成员");
		btn.setBounds(lcc.utils.swt.Util.zoomForHignGDI(220,5,100,20));
		btn.addSelectionListener(new SelectionListener(){	
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				DlgDatabaseLogin dlg = new DlgDatabaseLogin(true);
				if (dlg.open() == SWT.OK) {
					_tableMember.removeAll();
					List<FIELD> fields = dlg.getFields();
					for (int i=0; i<fields.size(); i++) {
						FIELD field = fields.get(i);
						TableHelp.addRow(_tableMember
								, new String[]{getJavaClass(field.type),getVariableName(field.name,false),field.comment}
						);
					}
				}
			}
		});
		
		this._tableMember = new Table(composite, SWT.SINGLE|SWT.FULL_SELECTION|SWT.BORDER|SWT.V_SCROLL);
		this._tableMember.setHeaderVisible(true);// 设置显示表头
		this._tableMember.setLinesVisible(true);// 设置显示表格线
		this._tableMember.setBounds(lcc.utils.swt.Util.zoomForHignGDI(5,30,786,400));
		//加载列信息
		ArrayList<TableHelp.Column> columns = new ArrayList<TableHelp.Column>();
		columns.add(new TableHelp.Column("data_type","成员数据类型",lcc.utils.swt.Util.zoomForHignGDI(180),SWT.CENTER));
		columns.add(new TableHelp.Column("member_name","成员变量名称",lcc.utils.swt.Util.zoomForHignGDI(180),SWT.CENTER));
		columns.add(new TableHelp.Column("member_remark","成员变量备注",lcc.utils.swt.Util.zoomForHignGDI(400),SWT.LEFT));
		TableHelp.setColumnData(this._tableMember, columns);
		//设置表格的可编辑属性
		this._tableMember.addListener(SWT.MouseDoubleClick, new Listener() {
			int editColumnIndex = -1;
			
			@Override
			public void handleEvent(Event paramEvent) {
				final Point point = new Point(paramEvent.x, paramEvent.y);
			    final TableItem tableItem = _tableMember.getItem(point);
			    if (tableItem == null) {
			    	addMemberRow();
			    } else {
			    	final int colCount = _tableMember.getColumnCount();
				    for (int i=0; i<colCount; i++) {
				    	final Rectangle rect = tableItem.getBounds(i);
				    	if (rect.contains(point)) {
				    		editColumnIndex = i;
				    	    final TableEditor editor = new TableEditor(_tableMember);
				    	    final Control oldEditor = editor.getEditor();
				    	    if (oldEditor != null) {
				    	        oldEditor.dispose();
				    	    }
				    	    final Text text = new Text(_tableMember, SWT.NONE);
				    	    text.computeSize(SWT.DEFAULT, _tableMember.getItemHeight());
				    	    editor.grabHorizontal = true;
				    	    editor.minimumHeight = text.getSize().y;
				    	    editor.minimumWidth = text.getSize().x;
				    	    editor.setEditor(text, tableItem, editColumnIndex);
				    	    text.setText(tableItem.getText(editColumnIndex));
				    	    text.selectAll();
				    	    text.forceFocus();
				    	    text.addModifyListener(new ModifyListener() {
								@Override
								public void modifyText(ModifyEvent paramModifyEvent) {
									editor.getItem().setText(editColumnIndex, text.getText());
								}
				    	    });
				    	    text.addListener(SWT.FocusOut, new Listener() {

								@Override
								public void handleEvent(Event paramEvent) {
									editor.getItem().setText(editColumnIndex, text.getText());
									text.dispose();
									editor.dispose();
								}
				    	    	
				    	    });
				    	    
				    	    break;
				    	}
				    }
			    }
			}
			
		});
		
	    applyDialogFont(composite);
	    
	    return composite;
    }
	
	private String getJavaClass(String fieldType) {
		if (fieldType == null || "".equals(fieldType)) {
			return "String";
		}
		fieldType = fieldType.trim().toLowerCase();
		if (fieldType.contains("char")) {
			return "String";
		} else if (fieldType.contains("date") || fieldType.contains("time")) {
			return "Date";
		} else {
			return fieldType;
		}
	}
	
	/**
	 * 通过字段名称生成类的属性名称
	 * @param fieldName 字段名称
	 * @param camelCase true-按驼峰命名法生成属性名；false-将fieldName全小写
	 * @return
	 */
	private String getVariableName(String fieldName,boolean camelCase) {
		if (fieldName == null || "".equals(fieldName)) {
			return "";
		}
		fieldName = fieldName.trim();
		if (camelCase) {//按驼峰法命名函数名
			final int length = fieldName.length();
			int index = fieldName.indexOf('_');
			while (index > -1) {
				if (length <= index+1) {
					break;
				}
				fieldName = fieldName.substring(0,index)+'_'+Character.toUpperCase(fieldName.charAt(index+1))+fieldName.substring(index+2);
				index = fieldName.indexOf('_',index+1);
			}
			fieldName = fieldName.replace("_", "");
			return fieldName;
		} else {
			return fieldName.toLowerCase();
		}
	}
	
	private void addMemberRow() {
		//默认成员的数据类型为"String"
		TableHelp.addRow(this._tableMember, new String[]{"String","",""});
		final int index = this._tableMember.getItemCount()-1;		
		this._tableMember.getItem(index).setText(0, "String");
		this._tableMember.select(index);
	}
}
