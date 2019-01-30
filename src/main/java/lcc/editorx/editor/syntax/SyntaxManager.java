package lcc.editorx.editor.syntax;

import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

import lcc.editorx.editor.TextEditorE;
import lcc.editorx.editor.syntax.c.CRuleScanner;
import lcc.editorx.editor.syntax.java.JavaRuleScanner;
import lcc.editorx.editor.syntax.javascript.JSRuleScanner;
import lcc.editorx.editor.syntax.sql.SqlRuleScanner;
import lcc.editorx.editor.syntax.xml.XMLConfiguration;
import lcc.editorx.editor.syntax.xml.XMLPartitionScanner;
import lcc.utils.KeyValuePair;

/**
 * 对所有的语言进行管理
 * @author lcc
 *
 */
public class SyntaxManager {
	/**
	 * 语言标识，子类中需要重置该属性成员的值 
	 */
	public static final String LANGUAGE_NONE = "none";
	public static final String LANGUAGE_JAVA = "Java";
	public static final String LANGUAGE_C = "C++";
	public static final String LANGUAGE_JAVASCRIPT = "JavaScript";
	public static final String LANGUAGE_SQL = "Sql";
	public static final String LANGUAGE_XML = "Xml";
	
	private KeyValuePair[] SYNTAX_MAP = null;
	
	//单例模式
	private static SyntaxManager instance = null;
	public static SyntaxManager getInstance() {  
		if (instance == null) {  
			instance = new SyntaxManager();  
		}  
		return instance;
	}
	private SyntaxManager() {
		this.SYNTAX_MAP = new KeyValuePair[]{
				new KeyValuePair(LANGUAGE_JAVA,new String[]{"java"})
				, new KeyValuePair(LANGUAGE_C,new String[]{"h","cpp","cxx"})
				, new KeyValuePair(LANGUAGE_JAVASCRIPT,new String[]{"js"})
				, new KeyValuePair(LANGUAGE_SQL,new String[]{"sql"})
				, new KeyValuePair(LANGUAGE_XML,new String[]{"xml"})
			};
	}
	
	public void initEditorSyntax(TextEditorE editor,String language) {
		if (LANGUAGE_XML.equalsIgnoreCase(language)) {
			IDocumentPartitioner partitioner = new FastPartitioner(
					new XMLPartitionScanner(),
					new String[] {XMLPartitionScanner.XML_TAG,XMLPartitionScanner.XML_COMMENT });
			partitioner.connect(editor.getDocument());
			editor.getDocument().setDocumentPartitioner(partitioner);		
			editor.configure(new XMLConfiguration());
		} else if (LANGUAGE_JAVA.equalsIgnoreCase(language)) {
			editor.configure(new SourceViewerConfigurationE(new JavaRuleScanner()));
		} else if (LANGUAGE_C.equalsIgnoreCase(language)) {
			editor.configure(new SourceViewerConfigurationE(new CRuleScanner()));
		} else if (LANGUAGE_JAVASCRIPT.equalsIgnoreCase(language)) {
			editor.configure(new SourceViewerConfigurationE(new JSRuleScanner()));
		} else if (LANGUAGE_SQL.equalsIgnoreCase(language)) {
			editor.configure(new SourceViewerConfigurationE(new SqlRuleScanner()));
		} else {
			return;
		}
		
		editor.setLanguage(language);
	}
	
	public String getLanguageBuSuffix(String suffix) {
		for (int i=0; i<this.SYNTAX_MAP.length ; i++) {
			String[] suffixs = (String[])this.SYNTAX_MAP[i].value;
			for (int j=0; j<suffixs.length; j++) {
				if (suffixs[j].equalsIgnoreCase(suffix)) {
					return (String)this.SYNTAX_MAP[i].key;
				}
			}
		}
		return LANGUAGE_NONE;
	}
	
	public boolean isSupportedLanguage(String language) {
		for (int i=0; i<this.SYNTAX_MAP.length; i++) {
			if (this.SYNTAX_MAP[i].key.equals(language)) {
				return true;
			}
		}
		return false;
	}
	
	public String[] getSupportedLanguage() {
		String[] ls = new String[this.SYNTAX_MAP.length];
		for (int i=0; i<this.SYNTAX_MAP.length; i++) {
			ls[i] = (String)this.SYNTAX_MAP[i].key;
		}
		return ls;
	}
}
