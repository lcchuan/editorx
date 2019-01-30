package lcc.editorx.editor.syntax.xml;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWhitespaceDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

import lcc.editorx.frame.ColorManager;

public class XMLTagScanner extends RuleBasedScanner {

	public XMLTagScanner() {
		IToken string =
			new Token(
				new TextAttribute(ColorManager.getInstance().get(ColorManager.RGB_STRING)));

		IRule[] rules = new IRule[3];

		// Add rule for double quotes
		rules[0] = new SingleLineRule("\"", "\"", string, '\\');
		// Add a rule for single quotes
		rules[1] = new SingleLineRule("'", "'", string, '\\');
		// Add generic whitespace rule.
		rules[2] = new WhitespaceRule(new IWhitespaceDetector() {
			public boolean isWhitespace(char c) {
			    return Character.isWhitespace(c);
			}
	     });

		setRules(rules);
	}
}
