package lcc.editorx.editor.syntax.java;

import lcc.editorx.editor.syntax.RuleBasedScannerE;
import lcc.editorx.editor.syntax.SyntaxManager;

/**
 * 基于规则的代码扫描器类- for java
 * @author lcc
 *
 */
public class JavaRuleScanner extends RuleBasedScannerE {
	/**
	 * 获取语法着色的语言类型
	 */
	@Override
	public String getLanguage() {
		return SyntaxManager.LANGUAGE_JAVA;
	}
	
	//初始化关键字
	@Override
	protected void initKeywords() {
		this.keywords = new String[]{"abstract","boolean","break","byte","case","catch","char","class","const"
				,"continue","default","do","double","else","enum","Exception","extends","false","float","final","finally"
				,"for","goto","if","implements","instanceof","int","interface","long","native","new","null","private"
				,"protected","public","return","short","static","strictfp","String","switch","synchronized","super"
				,"this","throw","throws","transient","true","try","void","volatile","while"};
	}
	
	//初始化关键字(编译相关)
	@Override
	protected void initCompileWords() {
		this.compileWords = new String[]{"import","package"};
	}
}
