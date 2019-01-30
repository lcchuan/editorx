package lcc.utils;

import java.util.ArrayList;

/**
 * 键值对，key-value or value-text 
 * 典型的应用场景：下拉框数据
 */
public class KeyValuePair {
	public Object key;
	public Object value;
	
	public KeyValuePair(Object key,Object value) {
		this.key = key;
		this.value = value;
	}
	
	public static Object getValue(ArrayList<KeyValuePair> data,Object key) {
		if (key == null || data == null) {
			return null;
		}
		for (int i=0; i<data.size(); i++) {
			if (key.equals(data.get(i).key)) {
				return data.get(i).value;
			}
		}
		return null;
	}
}
