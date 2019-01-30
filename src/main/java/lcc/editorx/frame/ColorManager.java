package lcc.editorx.frame;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * 颜色管理类，保证相同的颜色对象，不会被重复创建
 * @author lcc
 *
 */
public class ColorManager {
	public static RGB RGB_STRING = new RGB(128,128,128);
	public static RGB RGB_COMMENT = new RGB(0,128,128);
	public static RGB RGB_KEYWORD = new RGB(58,144,255);
	public static RGB RGB_KEYWORD1 = new RGB(149,0,85);
	public static RGB RGB_BLACK = new RGB(0,0,0);
	public static RGB RGB_RED = new RGB(255,0,0);
	
	//单例模式
	private static ColorManager instance = null;
	private ColorManager() {}
	public static ColorManager getInstance() {  
		if (instance == null) {  
			instance = new ColorManager();  
		}  
		return instance;
	}

	protected Map<String,Color> fColorTable = new HashMap<String,Color>();
	
	public Color get(RGB rgb) {
		Color color = fColorTable.get(rgb.toString());
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb.toString(), color);
		}
		return color;
	}

	public void dispose() {
		for (Map.Entry<String,Color> entry : fColorTable.entrySet()) {
			entry.getValue().dispose();
		}
	}
}
