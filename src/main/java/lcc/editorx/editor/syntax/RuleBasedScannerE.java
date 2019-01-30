package lcc.editorx.editor.syntax;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.SWT;

import lcc.editorx.frame.ColorManager;

/**
 * 基于规则的代码扫描器类- 基类
 * @author lcc
 *
 */
public abstract class RuleBasedScannerE extends RuleBasedScanner {	
	//设置关键字等的颜色、字体等
	protected Token tkKeyword = null;//关键字
	protected Token tkKeyword1 = null;//关键字
	protected Token tkString = null;//字符串
	protected Token tkComment = null;//注释
	
	protected String[] keywords = null;
	protected String[] compileWords = null;
	
	public RuleBasedScannerE(){	   
	   this.tkKeyword = new Token(new TextAttribute(ColorManager.getInstance().get(ColorManager.RGB_KEYWORD)));
	   this.tkKeyword1 = new Token(new TextAttribute(ColorManager.getInstance().get(ColorManager.RGB_KEYWORD1)));
	   this.tkString = new Token(new TextAttribute(ColorManager.getInstance().get(ColorManager.RGB_STRING)));
	   this.tkComment = new Token(new TextAttribute(ColorManager.getInstance().get(ColorManager.RGB_COMMENT),null,SWT.ITALIC));
	   
	   initKeywords();
	   initCompileWords();
	   
	   //设置代码的规则
	   initRules();
	}
	
	/**
	 * 比较两个规则对象是否相等
	 * @param ruleScanner1
	 * @param ruleScanner2
	 * @return
	 */
	public static boolean equals(RuleBasedScannerE ruleScanner1,RuleBasedScannerE ruleScanner2) {
		if (ruleScanner1 == null) {
			return ruleScanner2 == null;
		}
		return ruleScanner2 == null ? false : ruleScanner1.getLanguage().equals(ruleScanner2.getLanguage());
	}
	
	/**
	 * 比较两个规则对象是否相等
	 * @param ruleScanner
	 * @return
	 */
	public boolean equals(RuleBasedScannerE ruleScanner) {
		return ruleScanner == null ? false : this.getLanguage().equals(ruleScanner.getLanguage());
	}
	
	/**
	 * 获取语法着色的语言类型
	 */
	public abstract String getLanguage();
	
	//初始化关键字
	protected abstract void initKeywords();
	
	//初始化关键字(编译相关)
	protected abstract void initCompileWords();
	
	//初始化字符串的规则
	protected void initStringRules(List<IRule> rules) {
		rules.add(new SingleLineRule("\"", "\"",this.tkString, '\\'));
	    rules.add(new SingleLineRule("'", "'", this.tkString, '\\'));
	}
	
	//初始化注释的规则
	protected void initCommontRules(List<IRule> rules) {
		rules.add(new MultiLineRule("/*", "*/", this.tkComment));
	    rules.add(new EndOfLineRule("//", this.tkComment));
	}
	
	//初始化关键字的规则
	protected void intiKeywordRules(List<IRule> rules) {
		if (this.keywords == null || this.keywords.length < 1) {
			return ;
		}
		
	     WordRule ruleKeyWord = new WordRule(new IWordDetector(){
	    	 // 接口中的方法,字符是否是单词的开始
	    	 public boolean isWordStart(char c) {
	    		 for (int i=keywords.length-1; i>-1; i--) {
	    			 if (keywords[i].charAt(0) == c) {
	    				 return true;
	    			 }
	    		 }
	    		 return false;
	    	 }

	    	 // 接口中的方法,字符是否是单词中的一部分
	    	 public boolean isWordPart(char c) {
	    		 for (int i=keywords.length-1; i>-1; i--) {
	    			 if (keywords[i].indexOf(c) > -1) {
	    				 return true;
	    			 }
	    		 }
	    		 return false;
	    	 }
	     });
	     
	     for (int i=0; i<keywords.length; i++) {
	    	 ruleKeyWord.addWord(this.keywords[i], this.tkKeyword);
	     }
	     rules.add(ruleKeyWord);    
	}
	
	//初始化编译关键字的规则
	protected void intiCompileWordRules(List<IRule> rules) {
		if (this.compileWords == null || this.compileWords.length < 1) {
			return ;
		}
		
	    WordRule ruleKeyWord = new WordRule(new IWordDetector(){
	    	// 接口中的方法,字符是否是单词的开始
	    	 public boolean isWordStart(char c) {
	    		 for (int i=compileWords.length-1; i>-1; i--) {
	    			 if (compileWords[i].charAt(0) == c) {
	    				 return true;
	    			 }
	    		 }
	    		 return false;
	    	 }

	    	 // 接口中的方法,字符是否是单词中的一部分
	    	 public boolean isWordPart(char c) {
	    		 for (int i=compileWords.length-1; i>-1; i--) {
	    			 if (compileWords[i].indexOf(c) > -1) {
	    				 return true;
	    			 }
	    		 }
	    		 return false;
	    	 }
	    });
	     
	    for (int i=0; i<compileWords.length; i++) {
	    	 ruleKeyWord.addWord(this.compileWords[i], this.tkKeyword1);
	    }
	    rules.add(ruleKeyWord); 	     
	}
	
	//初始化规则
	protected void initRules() {
	     //用一个List集合对象保存所有的规则
	     List<IRule> rules = new ArrayList<IRule>();
	     
	     //初始化字符串的规则
	     initStringRules(rules);
	     
	     //初始化注释的规则
	     initCommontRules(rules);
	     
	     //空格的规则
	     rules.add(new WhitespaceRule(new IWhitespaceDetector() {
             public boolean isWhitespace(char c) {
                 return Character.isWhitespace(c);
             }
	     }));
	     
	     //初始化空格的规则
	     rules.add(new WhitespaceRule(new IWhitespaceDetector() {
			public boolean isWhitespace(char c) {
			    return Character.isWhitespace(c);
			}
	     }));
	     
	     //初始化关键字的规则
	     intiKeywordRules(rules);	     
	     intiCompileWordRules(rules);
	     
	     //调用父类中的方法，设置规则
	     setRules(rules.toArray(new IRule[rules.size()]));
	}
}
