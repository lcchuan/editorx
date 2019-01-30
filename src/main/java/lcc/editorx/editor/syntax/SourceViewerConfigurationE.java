package lcc.editorx.editor.syntax;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

/**
 * 代码着色配置类
 * @author lcc
 *
 */
public class SourceViewerConfigurationE extends SourceViewerConfiguration {
	//扫描规则
	protected RuleBasedScannerE ruleBasedScanner;
	
	protected SourceViewerConfigurationE() {}
	
	public SourceViewerConfigurationE(RuleBasedScannerE ruleBasedScanner) {
		super();
		this.ruleBasedScanner = ruleBasedScanner;
	}
	
	//覆盖父类中的方法，主要提供代码着色功能
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
	    PresentationReconciler reconciler = new PresentationReconciler();
	    
	    DefaultDamagerRepairer dr = new DefaultDamagerRepairer(this.ruleBasedScanner);
	    reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
	    reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
	    return reconciler;
	}
}
