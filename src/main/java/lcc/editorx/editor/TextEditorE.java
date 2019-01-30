package lcc.editorx.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import lcc.editorx.editor.syntax.SyntaxManager;
import lcc.editorx.frame.AppAction;
import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ColorManager;
import lcc.editorx.frame.config.TempContentRecord;
import lcc.utils.EncodingDetect;
import lcc.utils.StringHelp;

/**
 * 编辑器主类：提供代码着色、撤销恢复等
 * @author lcc
 *
 */
public class TextEditorE extends SourceViewer {
	protected TextEditorE me = null;
	
	//是否为mac平台
	public static final boolean IS_MAC = "cocoa".equals(SWT.getPlatform());
	//mac下，commond键为ctrl键
	public static final int KEY_CTRL = IS_MAC ? SWT.COMMAND : SWT.CTRL;
	
	public static final String[] charsets = {"GBK","UTF-8","UTF-16LE","UTF-16BE"};
	
	//编辑器背景色
	private static Color crBackgroud = ColorManager.getInstance().get(new RGB(255,255,255));
	//当前行的颜色
	private static Color crCurLine = ColorManager.getInstance().get(new RGB(232,242,254));
	
	//编辑器默认字体,使用时请调用getDefaultFont()函数
	private static Font defaultFont = null;
	
	//行号显示
	private LineNumberRulerColumn lineNumberRuler = null;
	
	private UndoManager undoManager;
	/**
	 * 是否需要撤销
	 * 该成员为临时变量，在replaceText、setText中被赋值，addVerifyListener中被使用
	 */
	private boolean needUndo = true;
	
	//内容是否已更改
	private boolean isModified = false;
	
	private String filepath = null;
	/**
	 * 文件字符编码,默认为GBK编码
	 * intel系列CPU采用的little endian方式存储数据
	 */
	private String encode = "GBK";
	//外部菜单选择的文件编码，该编码只有在保存文件时才会对文件的实际内容进行更改
	private String selectedEncode = "GBK";
	//是否携带BOM信息
	private boolean hasBOM = true;
	//语法高亮的语言类型
	private String language = SyntaxManager.LANGUAGE_NONE;
	
	public TextEditorE(Composite parent,String language) {
		super(parent, new CompositeRuler(), null, false, SWT.V_SCROLL | SWT.H_SCROLL | SWT.BORDER);
		
		this.me = this;
		
		//行号显示功能
		this.lineNumberRuler = new LineNumberRulerColumn();
		this.lineNumberRuler.setBackground(ColorManager.getInstance().get(new RGB(248,248,248)));
		this.lineNumberRuler.setForeground(ColorManager.getInstance().get(new RGB(160,160,160)));
		((CompositeRuler)this.getVerticalRuler()).addDecorator(0, this.lineNumberRuler);

		if (AppFrame.getInstance().getEditorBackground() != null) {
			this.getTextWidget().setBackgroundImage(AppFrame.getInstance().getEditorBackground());
		} else {
			this.getTextWidget().setBackground(TextEditorE.crBackgroud);
		}
		//设置编辑前当前行的颜色
		this.getTextWidget().addCaretListener(new CaretListener() {
			@Override
			public void caretMoved(CaretEvent e) {
				final int lineindex = getTextWidget().getLineAtOffset(e.caretOffset);
				final int linecount = getTextWidget().getLineCount();
				getTextWidget().setLineBackground(0, linecount, crBackgroud);
				getTextWidget().setLineBackground(lineindex, 1, crCurLine);
			}
			
		});
		
		this.setDocument(new Document());
		
		//设置默认字体
		try {
			this.getTextWidget().setFont(getDefaultFont());
		} catch (Exception e) {
			System.out.println("字体设置时遭遇异常");
		}
		
		this.getTextWidget().setMenu(EditorPopupMenu.createPopupMenu(this));
		
		//设置语法着色
		SyntaxManager.getInstance().initEditorSyntax(this, language);
		
		//撤销与恢复
		this.undoManager = new UndoManager(this);
		
		//添加键盘响应事件
		this.getTextWidget().addVerifyKeyListener(new VerifyKeyListener() {

			@Override
			public void verifyKey(VerifyEvent e) {
				//e.doit = false;
				onKeyDown(e);
			}}
		);
		
		//添加鼠标滚轮事件
		this.getTextWidget().addMouseWheelListener(new MouseWheelListener(){
			@Override
			public void mouseScrolled(MouseEvent e) {
				if (e.stateMask == KEY_CTRL) {
					if (e.count > 0) {
						zoomFont(true);
					} else if (e.count < 0) {
						zoomFont(false);
					}
				}
			}
		});
		
		/*
		 * 文本内容更改事件,TextViewerUndoManager在输入汉子时，会将拼音中的每个字母也算进去
		 */
		this.getTextWidget().addVerifyListener(new VerifyListener(){
			@Override
			public void verifyText(VerifyEvent e) {
				if (e.doit && needUndo) {
					undoManager.pushUndo(e.text);
				}
				if (e.doit) {
					isModified = undoManager.isModified();
					AppFrame.getInstance().setTabModifiedSign(isModified());
					
					//存储临时内容
					if (isModified && (me.filepath == null || "".equals(me.filepath))) {
						TempContentRecord.getInstance().addEditorForSave(me);
					}
				}
			}
		});
	}
	
	public static Font getDefaultFont() {
		if (defaultFont == null) {
			try {
				defaultFont = new Font(AppFrame.getInstance().getDisplay(),"宋体",12,SWT.NORMAL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return defaultFont;
	}
	
	/**
	 * 缩放字体
	 * @param zoomIn true-放大；false-缩小
	 */
	public void zoomFont(boolean zoomIn) {
		try {
			Font font = this.getTextWidget().getFont();
			FontData[] fontData = font.getFontData();
			if (zoomIn) {
				++fontData[0].height;
			} else {
				--fontData[0].height;
			}
			Font newFont = new Font(AppFrame.getInstance().getDisplay(),fontData);
			this.getTextWidget().setFont(newFont);
		} catch (Exception e1) {
			System.out.println("字体设置时遭遇异常");
		}
	}
	
	/**
	 * 为了覆盖父类的撤销类，采用自定义的撤销类
	 */
	public void setUndoManager(IUndoManager undoManager)
	{
		fUndoManager = null;
	}
	
	/**
	 * 复制当前选中的内容
	 * this.getTextWidget().copy()会将格式一起复制，所以重写
	 */
	public void copy() {
		String selected = this.getTextWidget().getSelectionText();
		if ("".equals(selected)) {
			return ;
		}
		
		Clipboard clipboard = new Clipboard(Display.getCurrent());
		clipboard.setContents(new Object[]{selected}, new Transfer[]{TextTransfer.getInstance()});
		clipboard.dispose();
	}
	
	/**
	 * 剪切当前选中的内容
	 * this.getTextWidget().cut()会将格式一起复制，所以重写
	 */
	public void cut() {
		Point range = this.getTextWidget().getSelection();
		if (range.y-range.x < 1) {
			return ;
		}
		copy();		
		this.getTextWidget().replaceTextRange(range.x, range.y-range.x, "");
	}
	
	/**
	 * 复制另一个编辑器的全部内容，但不包括代码着色信息
	 * @param editor
	 */
	public void copy(TextEditorE editor) {
		this.setText(editor.getText());
		this.encode = editor.getEncode();
		this.selectedEncode = editor.getSelectedEncode();
		this.hasBOM = editor.hasBOM;
		this.undoManager = editor.undoManager;
		this.isModified = editor.isModified;
		this.needUndo = editor.needUndo;
		this.filepath = editor.filepath;
		
		Point range = editor.getSelectedRange();
		this.setSelectedRange(range.x, range.y);
		this.setTopIndex(editor.getTopIndex());
	}
	
	/**
	 * 判断encode是否为本编辑器所支持的字符编码
	 * @param encode
	 * @return
	 */
	public static boolean isSupportedCharset(String encode) {
		final String[] availableCharsets = getSupportedCharset();
		for (int i=0; i<availableCharsets.length; i++) {
			if (availableCharsets[i].equalsIgnoreCase(encode)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取本编辑器支持的编码
	 * @return
	 */
	public static String[] getSupportedCharset() {
		return charsets;
	}
	
	/**
	 * 判断内容是否已更改
	 * @return
	 */
	public boolean isModified() {
		return this.isModified;
	}
	
	public String getEncode() {
		return this.encode;
	}
	
	public String getSelectedEncode() {
		return this.selectedEncode;
	}
	
	public String setSelectedEncode(String encode) {
		if (TextEditorE.isSupportedCharset(encode)) {
			this.selectedEncode = encode;
		}
		return this.selectedEncode;
	}
	
	public boolean hasBom() {
		return this.hasBOM;
	}
	
	public void setHasBOM(boolean hasBOM) {
		this.hasBOM = hasBOM;
	}
	
	public void setText(String text) {
		this.needUndo = false;
		this.getDocument().set(text);
		this.needUndo = true;
		
		//重置撤销消息以及修改状态
		this.undoManager.clear();
		this.isModified = false;
	}
	
	public String getText() {
		return this.getDocument().get();
	}
	
	public byte[] getTextBytes() {
		return this.getTextBytes(this.encode);
	}
	
	public byte[] getTextBytes(String encode) {
		try {
			return this.getDocument().get().getBytes(encode);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String getFilepath() {
		return this.filepath;
	}
	
	public void undo() {
		this.undoManager.undo();
	}
	
	public void redo() {
		this.undoManager.redo();
	}
	
	/**
	 * 获取文件名称
	 * @return
	 */
	public String getFileName() {
		if (this.filepath == null || "".equals(this.filepath)) {
			return null;
		}
		
		int index = this.filepath.lastIndexOf('\\');
		int temp = this.filepath.lastIndexOf('/');
		if (index < temp) {
			index = temp;
		}
		if (index > -1) {
			return this.filepath.substring(index+1);
		} else {
			return null;
		}
	}
	
	public void focus() {
		this.getTextWidget().setFocus();
	}
	
	/**
	 * 替换文本或插入文本
	 * @param text  新的文本呢内容
	 * @param start 替换的起始位置
	 * @param length 被替换本文的长度
	 * @param needUndo   是否加入撤销消息
	 */
	public void replaceText(String text,int start,int length,boolean needUndo) {
		this.needUndo = needUndo;
		this.getTextWidget().replaceTextRange(start, length, text);
		this.needUndo = true;
	}
	
	public void replaceText(String text,int start,int length) {
		replaceText(text,start, length,true);
	}
	
	/**
	 * 替换当前所选
	 * @param text
	 */
	public void replaceText(String text) {
		Point range = this.getSelectedRange();
		this.replaceText(text,range.x, range.y);
	}
	
	/**
	 * 清除空行
	 */
	public void clearNullLine() {
		boolean hasNullLine = false;
		StringBuffer newText = new StringBuffer();
		boolean firstLine = true;
		final int length = this.getTextWidget().getLineCount();
		for (int i=0; i<length; i++) {
			String line = this.getTextWidget().getLine(i);
			
			int j = line.length()-1;
			for (;j>-1; j--) {
				if (!Character.isWhitespace(line.charAt(j))) {
					break;
				}
			}
			if (j > -1) {
				if (firstLine) {
					firstLine = false;
				} else {
					newText.append("\r\n");
				}
				newText.append(line);
			} else {
				hasNullLine = true;
			}
		}
		if (hasNullLine) {
			this.setText(newText.toString());
		}
	}
	
	/**
	 * 清除行号（从网站上拷贝代码时，经常会在每行的开头携带行号）
	 */
	public void clearRowNumber() {
		StyledText textWidget = this.getTextWidget();
		final Point sel = textWidget.getSelection();
		final int line_count = textWidget.getLineCount();
		int startLine = textWidget.getLineAtOffset(sel.x);
		int endLine;
		if (sel.x != sel.y) {
			endLine = textWidget.getLineAtOffset(sel.y);
		} else {
			endLine = startLine;
		}
		
		//解析行中是否携带行号，如果携带则删除，将删除后的行文本添加至des中
		StringBuilder des = new StringBuilder();
		boolean exists = false;
		for (int i=startLine; i<=endLine; i++) {
			final String line = this.getTextWidget().getLine(i);
			int index = 0;
			boolean match = false;
			for (; index<line.length(); index++) {
				char c = line.charAt(index);
				if (Character.isWhitespace(c)) {
					if (!match) {
						continue;
					} else {
						break;
					}
				} else if (c < '0' || '9' < c) {
					break;
				} else {
					match = true;
				}
			}
			if (index<line.length()) {
				exists = true;
				des.append(line.substring(index));
			} else {
				des.append(line);
			}
			if (i+1 < line_count) {
				//添加换行符
				des.append(System.getProperty("line.separator"));
			}
		}
		
		//替换删除行号后的行文本
		if (exists) {
			final int start = textWidget.getOffsetAtLine(startLine);
			int end =  endLine+1 == line_count ? textWidget.getCharCount() : textWidget.getOffsetAtLine(endLine+1);
			textWidget.setSelection(start, end);
			this.replaceText(des.toString());
			end =  endLine+1 == line_count ? textWidget.getCharCount() : textWidget.getOffsetAtLine(endLine+1);
			textWidget.setSelection(start, end);
		}
	}
	
	/**
	 * 处理选中行行头的tab符号 ，如果没有选中行，则处理当前选中位置(非行头)
	 * @param add true->添加4个空格；false-删除4个空格
	 */
	public void dealLineStartTab(boolean add) {
		StyledText textWidget = this.getTextWidget();
		final Point sel = textWidget.getSelection();
		int startLine = 0;
		int endLine = 0;
		if (sel.x != sel.y) {
			startLine = textWidget.getLineAtOffset(sel.x);
			endLine = textWidget.getLineAtOffset(sel.y);
		}
		
		if (sel.x == sel.y || startLine == endLine) { //处理当前选中位置(非行头)
			if (add) { //添加4个空格
				this.replaceText("    ");
				textWidget.setSelection(sel.x+4);
			} else if (0 < sel.x){
				int index = sel.x-1;
				if ("\t".equals(textWidget.getText(index, index))) {//删除tab
					textWidget.setSelection(index, sel.x);
					this.replaceText("");
				} else if (" ".equals(textWidget.getText(index, index))){//删除最多4个空格
					index--;
					while (0 < index && " ".equals(textWidget.getText(index, index))) {
						index--;
						if (sel.x-index == 4) {
							break;
						}
					}
					textWidget.setSelection(index, sel.x);
					this.replaceText("");
				}
			}
		} else { //处理所有选中行的行头tab
			StringBuilder str = new StringBuilder();
			final int lineCount = textWidget.getLineCount();
			final int charCount = textWidget.getCharCount();
			final int start = textWidget.getOffsetAtLine(startLine);
			final int end =  endLine+1 == lineCount ? charCount : textWidget.getOffsetAtLine(endLine+1);
			for (int i=startLine; i<=endLine; i++) {
				final int nextLineStart = i+1 == lineCount ? charCount : textWidget.getOffsetAtLine(i+1);
				int index = textWidget.getOffsetAtLine(i);
				if (add) {
					str.append("    ");
					str.append(textWidget.getText(index,nextLineStart-1));
				} else {
					if ("\t".equals(textWidget.getText(index, index))) {
						++index;
					} else if (" ".equals(textWidget.getText(index, index))) {
						int tmp = index+1;
						while (" ".equals(textWidget.getText(tmp, tmp))) {
							tmp++;
							if (tmp-index == 4) {
								break;
							}
						}
						index = tmp;
					}
					str.append(textWidget.getText(index,nextLineStart-1));
				}
			}
			textWidget.setSelection(start, end);
			this.replaceText(str.toString());
			if (endLine+1 == lineCount) {
				textWidget.setSelection(start, start+str.length());
			} else {
				textWidget.setSelection(start, start+str.length()-System.getProperty("line.separator").length());
			}			
		}
	}
	
	/**
	 * 回到当前行
	 */
	public void scrollToCurLine() {
		int offset = this.getTextWidget().getCaretOffset();
		int lineindex = this.getTextWidget().getLineAtOffset(offset);
		if (lineindex > 2) {
			lineindex -= 2;
		}
		this.getTextWidget().setTopIndex(lineindex);
	}
	
	/**
	 * 设置文本自动换行
	 * @param warp
	 */
	public boolean wrap(boolean wrap) {
		if (wrap && !SyntaxManager.LANGUAGE_NONE.equalsIgnoreCase(this.language)) {
			//代码着色时，不允许文本自动换行
			wrap = false;
		}
		this.getTextWidget().setWordWrap(wrap);
		return wrap;
	}
	
	public boolean isWrap() {
		return this.getTextWidget().getWordWrap();
	}
	
	/**
	 * 将当前选中的文本字符大小写转换
	 * @param upperCase
	 */
	public void changeCase(boolean upperCase) {
		Point range = this.getTextWidget().getSelection();
		if (range.y-range.x < 1) {
			return ;
		}
		String selected = this.getTextWidget().getSelectionText();
		String str = upperCase ? selected.toUpperCase() : selected.toLowerCase();
		if (!selected.equals(str)) {
			this.getTextWidget().replaceTextRange(range.x, range.y-range.x, str);
			this.getTextWidget().setSelection(range);
		}
	}
	
	/**
	 * 打开文件
	 * @param path
	 */
	public boolean openFile(String filepath) {
		this.filepath = filepath == null ? null : filepath.trim();
		this.getDocument().set(null);
		
		//设置语法着色
		try {
			//获取文件后缀名
			String suffix;
			int index = filepath.lastIndexOf('.');
			if (index < 0 || filepath.length()-1 <= index) {
				suffix = "";
			} else {
				suffix = filepath.substring(index+1).toLowerCase();
			}
			
			String language = SyntaxManager.getInstance().getLanguageBuSuffix(suffix);
			SyntaxManager.getInstance().initEditorSyntax(this, language);
		} catch (Exception e) {
			AppFrame.messageBox("设置语法着色时遭遇异常");
		}
		
		//加载文件内容
		InputStream in = null;
		try {
			File file = new File(this.filepath);
			if (!file.exists()) {
				AppFrame.messageBox("无效的文件路径【"+this.filepath+"】");
				return false;
			}
			if (!file.canRead()) {
				AppFrame.messageBox("您无权读取文件【"+this.filepath+"】");
				return false;
			}
			
			in = new FileInputStream(file);
			final int size = in.available();
			if (size > 0) {
				byte[] content = new byte[size];
				in.read(content);
				
				this.encode = EncodingDetect.getEncodeByBOM(content);
				if (this.encode != null) {
					this.hasBOM = true;
				} else {
					this.hasBOM = false;
					this.encode = EncodingDetect.getEncode(content);
				}
				if ("ASCII".equalsIgnoreCase(this.encode) || "ISO8859_1".equalsIgnoreCase(this.encode) || "GB2312".equalsIgnoreCase(this.encode)) {
					/*
					 * GB2312是中国规定的汉字编码，也可以说是简体中文的字符集编码;GBK 是 GB2312的扩展 ,除了兼容GB2312外，它还能显示繁体中文，还有日文的假名
					 * 总体说来，GBK包括所有的汉字，包括简体和繁体。而gb2312则只包括简体汉字。
					 */
					this.encode = "GBK";
				}
				this.getDocument().set(new String(content,this.encode));
			} else {
				this.encode = "GBK";
				this.hasBOM = false;
			}			
		} catch (Exception e) {
			this.filepath = null;
			AppFrame.messageBox("打开文件时遭遇异常");
			return false;
		} finally {
			this.selectedEncode = this.encode;
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e) {				
			}
		}
		
		return true;
	}
	
	public boolean saveFile(String filepath) {
		if (!this.encode.equalsIgnoreCase(this.selectedEncode)) {
			return this.saveFile(filepath, this.selectedEncode);
		} else {
			return this.saveFile(filepath, this.encode);
		}
	}
	
	/**
	 * 保存文件
	 */
	public boolean saveFile(String filepath,String encode) {
		FileOutputStream out = null;		
		try {
			File file = new File(filepath);
		    if (!file.exists()) {
		    	// if file doesnt exists, then create it
		    	file.createNewFile();
		    }
			if (!file.canWrite()) {
				AppFrame.messageBox("当前文件为只读，不可保存，如需保存，请选择“另存为”");
				return false;
			}
			
			//获取文件的字节内容
		    byte[] content = this.getTextBytes(encode);
		    if (content == null) {
		    	AppFrame.messageBox("获取文件的字节内容失败");
		    	return false;
		    }
		    
		    //保存文件内容
		    out = new FileOutputStream(file);
		    if (this.hasBOM) {
		    	//加入BOM信息
		    	byte[] bom = EncodingDetect.getBOMBytes(encode);
		    	if (bom != null) {
		    		out.write(bom);
		    	}
		    }
		    out.write(content);
		    out.flush();
		    out.close();
		    out = null;
		} catch (Exception e) {
			e.printStackTrace();
			AppFrame.messageBox("保存时遭遇异常");
			return false;
		} finally {
			try {
				if (out != null) {
					out.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		this.encode = encode;
		this.filepath = filepath;
		this.isModified = false;
		return true;
	}
	
	public String getLanguage() {
		return this.language;
	}
	
	/**
	 * 该函数仅供SyntaxManager.initEditorSyntax调用，其余调用无效
	 * @param language
	 */
	public void setLanguage(String language) {
		this.language = language;
	}
	
	/**
	 * 文本替换
	 * @param oldText
	 * @param newText
	 * @param caseSensitive 是否区分大小写
	 * @param wholeWord 是否查找整个字
	 * @return 替换的个数
	 */
	public int replaceAll(String oldText,String newText,boolean caseSensitive,boolean wholeWord) {
		if (oldText == null || "".equals(oldText)) {
			return 0;
		}
		
		final String src = this.getText();
		final int oldTextLength = oldText.length();
		final int newTextLength = newText.length();
		int replacedCount = 0;
		
		int start = 0;
		while (true) {
			start = StringHelp.indexOfString(this.getText(), oldText, caseSensitive,wholeWord, start);
			if (start > -1) {
				this.replaceText(newText, start, oldTextLength, false);
				start += newTextLength;
				replacedCount++;
			} else {
				break;
			}
		}
		
		if (replacedCount > 0) {
			this.undoManager.pushUndo(this.getTextWidget().getText(),src,0,src.length(),UndoManager.TYPE_REPLACE);
		}
		
		return replacedCount;
	}
	
	/**
	 * 查找文本,如果找到，则在编辑器中选择该段文字
	 * @param text  待查找的文本
	 * @param regex 是否为正则表达式
	 * @param caseSensitive 是否区分大小写
	 * @param wholeWord 是否查找整个字
	 * @param up 是否向上查找
	 * @param start 开始搜索的位置，如果小于0，则从当前光标位置开始搜索
	 * @return 返回搜索到的起始位置
	 */
	public int findText(String text,boolean caseSensitive,boolean wholeWord,boolean up,int start) {
		if (text == null || "".equals(text)) {
			return -1;
		}
		
		final String src = this.getText();
		final int charCount = src.length();
		int index = -1;
		
		//如果小于0，则从当前光标位置开始搜索
		if (start < 0) {
			Point range = this.getSelectedRange();
			if (up) {
				start = range.x;
			} else {
				start = range.x+range.y;
			}
		}
		
		if (up) {
			index = StringHelp.lastIndexOfString(src, text, caseSensitive,wholeWord, start-text.length());
			if (index < 0) {
				//循环搜索
				index = StringHelp.lastIndexOfString(src, text, caseSensitive,wholeWord, charCount-text.length());
			}
		} else {
			index = StringHelp.indexOfString(src, text, caseSensitive,wholeWord, start);
			if (index < 0) {
				//循环搜索
				index = StringHelp.indexOfString(src, text, caseSensitive,wholeWord, 0);
			}
		}	
		
		if (index > -1) {
			this.getTextWidget().setSelection(index, index+text.length());
		}
		
		return index;
	}
	
	/**
	 * 键盘点击事件
	 * @param arg0
	 */
	private void onKeyDown(VerifyEvent e){
		e.doit = false;
		if (e.stateMask == KEY_CTRL && (e.keyCode == 'c' || e.keyCode == 'C')) {
			//复制操作
			this.copy();
		} else if (e.stateMask == KEY_CTRL && (e.keyCode == 'x' || e.keyCode == 'X')) {
			//剪切操作
			this.cut();
		} else if (e.stateMask == KEY_CTRL && (e.keyCode == 'z' || e.keyCode == 'Z')) {
			//撤销操作
			this.undo();
		} else if (e.stateMask == KEY_CTRL && (e.keyCode == 'y' || e.keyCode == 'Y')) {
			//恢复操作
			this.redo();
		} else if (e.stateMask == KEY_CTRL && (e.keyCode == 'a' || e.keyCode == 'A')) {
			//全选
			this.getTextWidget().selectAll();
		} else if (e.stateMask == KEY_CTRL && (e.keyCode == 'f' || e.keyCode == 'F')) {
			//查找/替换
			DlgFind dlg = DlgFind.getInstance(this);
			dlg.open();
		} else if (e.stateMask == (KEY_CTRL|SWT.SHIFT) && (e.keyCode == 'z' || e.keyCode == 'Z')) {
			//字符小写
			changeCase(false);
		} else if (e.stateMask == (KEY_CTRL|SWT.SHIFT) && (e.keyCode == 'a' || e.keyCode == 'A')) {
			//字符大写
			changeCase(true);
		} else if (e.stateMask == KEY_CTRL && (e.keyCode == 's' || e.keyCode == 'S')) {
			//保存
			AppAction.onSave(this, false);
		} else if (e.keyCode == SWT.F3) {
			//向下搜索
			String text = this.getTextWidget().getSelectionText();
			if (!"".equals(text)) {
				this.findText(text, true, true, false, -1);
			}
		} else if (e.keyCode == SWT.F4) {
			//向上搜索
			String text = this.getTextWidget().getSelectionText();
			if (!"".equals(text)) {
				this.findText(text, true, true, true, -1);
			}
		} else if (e.stateMask == SWT.SHIFT && e.keyCode == SWT.TAB) {
			//删除行头的tab符
			this.dealLineStartTab(false);
		} else if (e.keyCode == SWT.TAB) {
			//行头的添加tab符
			this.dealLineStartTab(true);
		} else {
			e.doit = true;
		}
	}
}
