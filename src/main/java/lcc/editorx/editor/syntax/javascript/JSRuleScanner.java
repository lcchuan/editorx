package lcc.editorx.editor.syntax.javascript;

import lcc.editorx.editor.syntax.RuleBasedScannerE;
import lcc.editorx.editor.syntax.SyntaxManager;

/**
 * 基于规则的代码扫描器类- for javascript
 * @author lcc
 *
 */
public class JSRuleScanner extends RuleBasedScannerE {
	@Override
	public String getLanguage() {
		return SyntaxManager.LANGUAGE_JAVASCRIPT;
	}

	/**
	 * 初始化关键字
	 */
	@Override
	protected void initKeywords() {
		this.keywords = new String[]{"alert","Array","break","case","catch","char","confirm","const","continue","Date"
				,"default","delete","do","else","false","finally","for","function","if","in","instanceof"
				,"Math","new","null","Number","parseInt","return","switch","String","this","throw","true","try","typeof"
				,"var","void","while","with"};
	}
	
	/**
	 * 初始化关键字(编译相关)
	 */
	@Override
	protected void initCompileWords() {
		this.compileWords = new String[]{"attachEvent","body","debugger","document","window"};
	}

}
