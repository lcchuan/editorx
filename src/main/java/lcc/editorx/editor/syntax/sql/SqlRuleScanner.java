package lcc.editorx.editor.syntax.sql;

import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

import lcc.editorx.editor.syntax.RuleBasedScannerE;
import lcc.editorx.editor.syntax.SyntaxManager;

/**
 * 基于规则的代码扫描器类- for SQL
 * @author lcc
 *
 */
public class SqlRuleScanner extends RuleBasedScannerE {

	@Override
	public String getLanguage() {
		return SyntaxManager.LANGUAGE_SQL;
	}
	
	//初始化注释的规则
	@Override
	protected void initCommontRules(List<IRule> rules) {
		rules.add(new MultiLineRule("/*", "*/", this.tkComment));
	    rules.add(new EndOfLineRule("--", this.tkComment));
	}

	@Override
	protected void initKeywords() {
		this.keywords = new String[]{"ALTER","AND","ALL","AS","BEGIN","BETWEEN","BREAK","BY","CASE"
				,"CLOSE","COLUMN","COMMENT","COMMIT","CONTINUE","CREATE","CURSOR","DECLARE","DEFAULT","DELETE","DESC","DISTINCT"
				,"DROP","ELSE","END","EXEC","EXECUTE","EXIT","EXISTS","FETCH","FROM","FOR"
				,"FUNCTION","GO","GRANT","GROUP","HAVING","IF","IMMEDIATE","INNER","IN","INSERT","INTO"
				,"IS","JOIN","KEY","LEFT","LIKE","LOOP","NOT","NULL","ON","OPEN","OR","ORDER"
				,"PRIMARY","PROC","PROCEDURE","RETURN","REVOKE","RIGHT","ROLLBACK","ROWNUM","SELECT","SET"
				,"TABLE","TABLESPACE","THEN","TOP","TRUNCATE","UNION","UPDATE","VALUES","VIEW"
				,"WHEN","WHERE","WHILE","WITH"};
	}

	@Override
	protected void initCompileWords() {
		this.compileWords = new String[]{"AVG","COUNT","LENGTH","LTRIM","MAX","MIN","REPLACE","RTRIM","SUM"};
	}
	
	//初始化关键字的规则
	@Override
	protected void intiKeywordRules(List<IRule> rules) {
		if (this.keywords == null || this.keywords.length < 1) {
			return ;
		}
		
	     WordRule ruleKeyWord = new WordRule(new IWordDetector(){
		    	 // 接口中的方法,字符是否是单词的开始
		    	 public boolean isWordStart(char c) {
		    		 for (int i=keywords.length-1; i>-1; i--) {
		    			 if (Character.toLowerCase(keywords[i].charAt(0)) == Character.toLowerCase(c)) {
		    				 return true;
		    			 }
		    		 }
		    		 return false;
		    	 }
	
		    	 // 接口中的方法,字符是否是单词中的一部分
		    	 public boolean isWordPart(char c) {
		    		 for (int i=keywords.length-1; i>-1; i--) {
		    			 if (keywords[i].toLowerCase().indexOf(Character.toLowerCase(c)) > -1) {
		    				 return true;
		    			 }
		    		 }
		    		 return false;
		    	 }
		     }
	  		, Token.UNDEFINED
	  		, true
	     );
	     
	     if (this.keywords != null) {
	    	 for (int i=0; i<keywords.length; i++) {
		    	 ruleKeyWord.addWord(this.keywords[i], this.tkKeyword);
		     }
		     rules.add(ruleKeyWord); 
	     }     
	}

	//初始化函数关键字的规则
	protected void intiCompileWordRules(List<IRule> rules) {
		if (this.compileWords == null || this.compileWords.length < 1) {
			return ;
		}
		
	    WordRule ruleKeyWord = new WordRule(new IWordDetector(){
		    	// 接口中的方法,字符是否是单词的开始
		    	 public boolean isWordStart(char c) {
		    		 for (int i=compileWords.length-1; i>-1; i--) {
		    			 if (Character.toLowerCase(compileWords[i].charAt(0)) == Character.toLowerCase(c)) {
		    				 return true;
		    			 }
		    		 }
		    		 return false;
		    	 }
	
		    	 // 接口中的方法,字符是否是单词中的一部分
		    	 public boolean isWordPart(char c) {
		    		 for (int i=compileWords.length-1; i>-1; i--) {
		    			 if (compileWords[i].toLowerCase().indexOf(Character.toLowerCase(c)) > -1) {
		    				 return true;
		    			 }
		    		 }
		    		 return false;
		    	 }
		    }
     		, Token.UNDEFINED
     		, true
	    );
	     
	    for (int i=0; i<compileWords.length; i++) {
	    	 ruleKeyWord.addWord(this.compileWords[i], this.tkKeyword1);
	    }
	    rules.add(ruleKeyWord); 	     
	}
}
