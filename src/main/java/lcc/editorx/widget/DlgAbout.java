package lcc.editorx.widget;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import lcc.editorx.frame.AppFrame;
import lcc.editorx.frame.ImageManager;

/**
 * 关于对话框
 * @author lcc
 *
 */
public class DlgAbout extends Dialog{
	private Label imgContainer = null;
	private Image img = null;
	
	public DlgAbout() {
		super(AppFrame.getInstance().getShell());
	}
	
	/**
	 * 设置标题栏名称
	 */
	@Override  
	protected void configureShell(Shell newShell) { 
	    super.configureShell(newShell);  
	    newShell.setText("About EditorX");  
	} 
	
	/**
	 * 设置窗体大小
	 */
	@Override  
	protected Point getInitialSize() {
	    return lcc.utils.swt.Util.zoomForHignGDI(350,180);  
	} 
	
	/**
	 * 重写对话框按钮
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
	    createButton(parent, 0, "ok",true);
	}
	
	/**
	 * 创建对话框内容
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, 0);
		
		this.imgContainer = new Label(composite, SWT.NONE);		
		this.img = ImageManager.getInstance().get("about.bmp");
		imgContainer.addPaintListener(new PaintListener(){
			@Override
			public void paintControl(PaintEvent e) {
				if (img == null) {
					return ;
				}
				Point size = imgContainer.getSize();
				Rectangle imgSize = img.getBounds();
				e.gc.drawImage(img, 0, 0, imgSize.width, imgSize.height, 0, 0,size.x,size.y);
			}
		});
		this.imgContainer.setBounds(lcc.utils.swt.Util.zoomForHignGDI(45,25,66,61));
		
		StringBuffer str = new StringBuffer();
		str.append("EditorX v4.2.3.1");
		str.append("\r\nAuthor  李长川");
		str.append("\r\nE-Mail  lcchuan@163.com");
		
		Label label = new Label(composite, SWT.NONE);
		label.setText(str.toString());
		/*try {
			label.setFont(new Font(AppFrame.getInstance().getDisplay(),"宋体",14,SWT.NORMAL));
		} catch (Exception e) {
		}*/
		label.setBounds(lcc.utils.swt.Util.zoomForHignGDI(140,30,200,65));
		
	    applyDialogFont(composite);
	    return composite;
    }
}
