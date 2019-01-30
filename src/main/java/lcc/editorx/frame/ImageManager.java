package lcc.editorx.frame;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

public class ImageManager {
	//单例模式
	private static ImageManager instance = null;
	private ImageManager() {}
	public static ImageManager getInstance() {  
		if (instance == null) {  
			instance = new ImageManager();  
		}  
		return instance;
	}
	
	protected Map<String,Image> map = new HashMap<String,Image>();
	
	public Image get(String name) {
		Image image = (Image)map.get(name);
		if (image == null) {
			try {
				image = new Image(AppFrame.getInstance().getDisplay()
						, AppFrame.class.getResourceAsStream("/images/"+name));
				if ("editor.ico".equals(name)) {
					image = resizeImage(image, 32, 32);
				}
				map.put(name, image);
			} catch (Exception e) {
				image = null;
			}
		}
		return image;
	}

	public void dispose() {
		for (Map.Entry<String, Image> entry : map.entrySet()) {
			entry.getValue().dispose();
		}
	}
	
	public static Image resizeImage(Image src, int width,int height) {
		Image scaled = new Image(Display.getDefault(),width,height);
		GC gc = new GC(scaled);
		try {
			gc.setAdvanced(true); //打开高级绘图模式
			gc.setAntialias(SWT.ON);//设置消除锯齿
			gc.setInterpolation(SWT.HIGH);//设置插值
			gc.drawImage(src, 0, 0, src.getBounds().width,src.getBounds().height, 0, 0, width, height);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			gc.dispose();
		}
		return scaled;
	}
}
