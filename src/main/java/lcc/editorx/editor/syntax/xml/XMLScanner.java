package lcc.editorx.editor.syntax.xml;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.swt.SWT;

import lcc.editorx.frame.ColorManager;

public class XMLScanner extends RuleBasedScanner {

	public XMLScanner() {
		IToken procInstr =
			new Token(
				new TextAttribute(
						ColorManager.getInstance().get(ColorManager.RGB_KEYWORD),null,SWT.ITALIC));

		IRule[] rules = new IRule[2];
		//Add rule for processing instructions
		rules[0] = new SingleLineRule("<?", "?>", procInstr);
		// Add generic whitespace rule.
		rules[1] = new WhitespaceRule(new IWhitespaceDetector() {
			public boolean isWhitespace(char c) {
			    return Character.isWhitespace(c);
			}
	     });

		setRules(rules);
	}
}
