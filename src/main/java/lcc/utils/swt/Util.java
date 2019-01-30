package lcc.utils.swt;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Util {
	private static double zoom = 0;
	
	//信息提示框
	public static void messageBox(Shell shell,String text) {
		MessageBox msgBox = new MessageBox(shell,SWT.OK|SWT.ICON_INFORMATION);
		msgBox.setText("提示");
		msgBox.setMessage(text);
		msgBox.open();
	}
	
	//确认提示框
	public static boolean confirm(Shell shell,String text) {
		MessageBox msgBox = new MessageBox(shell,SWT.OK|SWT.CANCEL|SWT.ICON_QUESTION);
		msgBox.setText("确认");
		msgBox.setMessage(text);
		return msgBox.open() == SWT.OK;
	}
	
	public static int zoomForHignGDI(int length) {
		if (zoom == 0) {
			String osname = System.getProperty("os.name");
			String osversion = System.getProperty("os.version");
			if ("Windows 8.1".equals(osname) && "6.3".equals(osversion)) {
				//解决win10的缩放问题,java中用的是旧版本的windowsApi,所以获取的版本最高只能为win8,即使是win10返回的也是win8
				//100%的缩放比例时，dpi应该为96
				Point dpi = Display.getDefault().getDPI();
				zoom = (double)dpi.x/(double)96;
			} else {
				zoom = 1;
			}
		}
		
		return (1==zoom ? length : (int)Math.round(length*zoom));
	}
	
	public static Point zoomForHignGDI(Point pt) {
		return zoomForHignGDI(pt.x,pt.y);
	}
	
	public static Point zoomForHignGDI(int x,int y) {
		return new Point(zoomForHignGDI(x),zoomForHignGDI(y));
	}
	
	public static org.eclipse.swt.graphics.Rectangle zoomForHignGDI(int x,int y,int width,int height) {
		return new org.eclipse.swt.graphics.Rectangle(
				zoomForHignGDI(x),zoomForHignGDI(y),zoomForHignGDI(width),zoomForHignGDI(height));
	}
}
