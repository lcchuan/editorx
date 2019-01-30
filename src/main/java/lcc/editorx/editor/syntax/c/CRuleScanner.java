package lcc.editorx.editor.syntax.c;

import java.util.List;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.WordPatternRule;

import lcc.editorx.editor.syntax.RuleBasedScannerE;
import lcc.editorx.editor.syntax.SyntaxManager;

/**
 * 基于规则的代码扫描器类- for C C++
 * @author lcc
 *
 */
public class CRuleScanner extends RuleBasedScannerE {
	@Override
	public String getLanguage() {
		return SyntaxManager.LANGUAGE_C;
	}

	@Override
	protected void initKeywords() {
		this.keywords = new String[]{"asm","auto","bool","break","byte","case","catch","char","class","const"
				,"continue","default","delete","do","double","else","enum","explicit","export","extern","false","float"
				,"for","friend","goto","if","inline","int","long","mutable","namespace","new","operator","private"
				,"protected","public","register","return","short","signed","sizeof","static","struct","switch","template"
				,"this","throw","true","try","typedef","typeid","typename","union","unsigned","using","virtual","void","volatile","while"};
	}

	@Override
	protected void initCompileWords() {
		this.compileWords = new String[]{"define","elif","else","endif","error"
				,"if","ifdef","ifndef","include","import","line","pragma","undef"};
	}

	//初始化关键字的规则
	@Override
	protected void intiCompileWordRules(List<IRule> rules) {
		if (this.compileWords == null || this.compileWords.length < 1) {
			return ;
		}
		
		IWordDetector wd = new IWordDetector(){
			public boolean isWordStart(char c) {
	    		 for (int i=compileWords.length-1; i>-1; i--) {
	    			 if (compileWords[i].charAt(0) == c) {
	    				 return true;
	    			 }
	    		 }
	    		 return false;
	    	 }
	    	 public boolean isWordPart(char c) {
	    		 for (int i=compileWords.length-1; i>-1; i--) {
	    			 if (compileWords[i].indexOf(c) > -1) {
	    				 return true;
	    			 }
	    		 }
	    		 return false;
	    	 }
	     };
		
	     for (int i=0; i<compileWords.length; i++) {
	    	 rules.add(new WordPatternRule(wd,"#",this.compileWords[i],this.tkKeyword1)); 
	     }    
	}
}
