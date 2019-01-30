package lcc.editorx.editor;

import java.util.Stack;

import org.eclipse.swt.graphics.Point;

/**
 * 撤销管理，TextViewerUndoManager在输入汉子时，会将拼音中的每个字母也算进去
 * @author lcc
 *
 */
public class UndoManager {
	public static final byte TYPE_DELETE = 1;
	public static final byte TYPE_INSERT = 2;
	public static final byte TYPE_REPLACE = 3;
	
	public class Data{
		public String newText;
		public String oldText;
		public int start;
		public int end;
		public byte type;
	}
	
	//默认的最大缓存为5M个字符
	private long maxBuffersize = 1024*1024*5;
	
	private TextEditorE editor = null;
	//撤销信息
	private Stack<Data> undo = null;
	//恢复信息
	private Stack<Data> redo = null;
	
	/*
	 * 是否可以撤销至初始状态，如果某次增加了超过maxBuffersize的内容
	 * 则undo消息会被删除，则表明不可能在恢复到初始状态了
	 */
	private boolean canToInit = true;
	
	public UndoManager(TextEditorE editor) {
		this.editor = editor;
		clear();
	}
	
	public void clear() {
		this.undo = new Stack<Data>();
		this.redo = new Stack<Data>();
	}
	
	public boolean isModified() {
		return canUndo() || !canToInit;
	}
	
	/**
	 * 判断是否有可撤销的信息
	 * @return
	 */
	public boolean canUndo() {
		return this.undo.size() > 0;
	}
	
	/**
	 * 添加撤销信息
	 * @param text
	 * @return
	 */
	public boolean pushUndo(String text) {
		Data data = new Data();
		Point range = this.editor.getTextWidget().getSelectionRange();
		
		if (text == null || "".equals(text)) {
			data.type = UndoManager.TYPE_DELETE;
		} else {
			if (range.y > 0) {
				data.type = UndoManager.TYPE_REPLACE;
			} else {
				data.type = UndoManager.TYPE_INSERT;
			}
		}
		
		data.newText = text == null ? "" : text;
		if (range.y < 1 && UndoManager.TYPE_DELETE == data.type) {
			data.start = range.x-1;
			data.end = range.x;
			data.oldText = this.editor.getTextWidget().getTextRange(data.start, 1);
		} else {
			data.oldText = this.editor.getTextWidget().getSelectionText();
			data.start = range.x;
			data.end = range.x+range.y;
		}
		
		pushUndo(data);
		
		return true;
	}
	
	public void pushUndo(String newText,String oldText,int start,int end,byte type) {
		Data data = new Data();
		data.newText = newText;
		data.oldText = oldText;
		data.start = start;
		data.end = end;
		data.type = type;
		pushUndo(data);
	}
	
	public void pushUndo(Data data) {
		//清空恢复消息
		this.redo = new Stack<Data>();		
		this.undo.push(data);
		
		//如果内存大小超限，则删除第一个信息
		while (this.getBuffersize() > this.maxBuffersize) {
			this.canToInit = false;
			this.undo.remove(0);
		}
	}
	
	public void undo() {
		if (!this.canUndo()) {
			return;
		}
		
		Data data = this.undo.pop();
		switch (data.type) {
		case TYPE_DELETE:
			this.editor.replaceText(data.oldText,data.start, 0, false);
			this.editor.getTextWidget().setSelection(data.start+data.oldText.length());
			break;
		case TYPE_INSERT:
			this.editor.replaceText("", data.start, data.newText.length(), false);
			break;
		case TYPE_REPLACE:
			this.editor.replaceText(data.oldText,data.start, data.newText.length(), false);
			break;
		default:
			System.out.println("2017010321091 无效的操作类型："+data.type);
			return ;
		}
		
		this.redo.push(data);
	}
	
	public void redo() {
		if (this.redo.size() < 1) {
			return ;
		}
		
		Data data = this.redo.pop();
		switch (data.type) {
		case TYPE_DELETE:
			this.editor.replaceText("", data.start, data.end-data.start, false);
			break;
		case TYPE_INSERT:
			this.editor.replaceText(data.newText, data.start, 0, false);
			this.editor.getTextWidget().setSelection(data.start+data.newText.length());
			break;
		case TYPE_REPLACE:
			this.editor.replaceText(data.newText, data.start, data.end-data.start, false);
			break;
		default:
			System.out.println("2017010321091 无效的操作类型："+data.type);
			return ;
		}
		
		this.undo.push(data);
	}
	
	/**
	 * 获取缓存大小
	 * @return
	 */
	private long getBuffersize() {
		long size = 0;
		Data data;
		for (int i=this.undo.size()-1; i>-1; i--) {
			data = this.undo.get(i);
			if (data.newText != null) {
				size += data.newText.length();
			}
			if (data.oldText != null) {
				size += data.oldText.length();
			}
		}
		for (int i=this.redo.size()-1; i>-1; i--) {
			data = this.redo.get(i);
			if (data.newText != null) {
				size += data.newText.length();
			}
			if (data.oldText != null) {
				size += data.oldText.length();
			}
		}
		return size;
	}
}
