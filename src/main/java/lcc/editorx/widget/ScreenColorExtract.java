package lcc.editorx.widget;

import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import lcc.editorx.frame.AppFrame;

/**
 * 屏幕取色
 * @author lcc
 *
 */
public class ScreenColorExtract{
	private Shell shell = null;
	private Robot robot = null;
	
	//鼠标事件
	private MouseListener mouseListener = null;
	
	//鼠标移动事件
	private MouseMoveListener mouseMoveListener = null;
	
	private DlgColor dlgColor = null;
	private DlgColor dlgColorPre = null;
	
	public ScreenColorExtract(DlgColor dlgColorPre1) {
		this.shell = new Shell(Display.getDefault(),SWT.MODELESS);
		this.dlgColor = new DlgColor(this.shell,true);
		this.dlgColorPre = dlgColorPre1;
		
		this.mouseListener = new MouseListener(){
			@Override
			public void mouseDoubleClick(MouseEvent arg0) {
			}

			@Override
			public void mouseDown(MouseEvent arg0) {
			}

			@Override
			public void mouseUp(MouseEvent arg0) {
				RGB rgb = dlgColor.getRGB();
				
				dlgColor.close();
				shell.dispose();
				
				if (dlgColorPre != null) {
					dlgColorPre.setColor(rgb);
					AppFrame.getInstance().getShell().setMinimized(false);
				}
			}
		};
		
		this.mouseMoveListener = new MouseMoveListener(){
			@Override
			public void mouseMove(MouseEvent e) {
				RGB rgb = getScreenRGB();
				if (rgb != null) {
					dlgColor.setColor(rgb);
				}
			}
		};
		
		//鼠标事件
		this.shell.addMouseListener(this.mouseListener);
		//鼠标移动事件
		this.shell.addMouseMoveListener(this.mouseMoveListener);
	}
	
	public void run() {
		Rectangle rect = this.shell.getDisplay().getBounds();
		Image screenImage = new Image(this.shell.getDisplay(), rect.width, rect.height);
		this.shell.setBounds(rect);
		
		//屏幕复制
		GC gc = new GC(this.shell.getDisplay());  
        gc.copyArea(screenImage, 0, 0);
        gc.dispose();
        
        Label canvas = new Label(this.shell, SWT.NONE);
        //canvas.setBackgroundImage(screenImage);
        canvas.setImage(screenImage);
        canvas.setBounds(rect);
        canvas.addMouseListener(this.mouseListener);
        canvas.addMouseMoveListener(this.mouseMoveListener);
        
        this.shell.open();
        
        this.dlgColor.open();
	}

	private RGB getScreenRGB() {
		try {
			if (this.robot == null) {
				this.robot = new Robot();
			}
			Point mousepoint = MouseInfo.getPointerInfo().getLocation();
			Color pixel = this.robot.getPixelColor(mousepoint.x,mousepoint.y);
			return new RGB(pixel.getRed(),pixel.getGreen(),pixel.getBlue());
		} catch (Exception e) {
			return null;
		}
	}
}
