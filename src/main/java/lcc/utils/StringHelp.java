package lcc.utils;

public class StringHelp {
	/**
	 * 将Object转为String,如果obj为null则返回空字符串
	 * @param obj
	 * @return
	 */
	public static String toString(Object obj) {
		return obj == null ? "" : obj.toString();
	}
	
	/**
	 * 判断是否为空字符串
	 * @param obj
	 * @return
	 */
	public static boolean isNullString(Object obj) {
		return obj == null || "".equals(obj.toString());
	}
	
	/**
	 * 复制String.indexOf算法，主要目的是增加了 “区分大小写”搜索功能
	 * @param   source        the characters being searched.
	 * @param   target        the characters being searched for.
	 * @param   caseSensitive 搜索时是否区分大小写
	 * @param   wholeWord     搜索时是否整字匹配
	 * @param   fromIndex     the index to begin searching from.
	 * @return
	 */
	public static int indexOfString(String source,String target,boolean caseSensitive,boolean wholeWord,int fromIndex) {
		if (target == null || source == null) {
			return -1;
		}
		
		final int sourceCount = source.length();
		final int targetCount = target.length();
		
		if (fromIndex >= sourceCount) {
            return (targetCount == 0 ? sourceCount : -1);
        }
		if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (targetCount == 0) {
            return fromIndex;
        }
        
        char first = target.charAt(0);
        int max = sourceCount - targetCount;
        for (int i = fromIndex; i <= max; i++) {
            /* Look for first character. */
            if (!firstCharEquals(source,i,first,caseSensitive,wholeWord)) {
                while (++i <= max && !firstCharEquals(source,i,first,caseSensitive,wholeWord));
            }

            /* Found first character, now look at the rest of v2 */
            if (i <= max) {
                int j = i + 1;
                int end = j + targetCount - 1;
                
                if (lastCharEquals(source,end-1,target.charAt(targetCount - 1),caseSensitive,wholeWord)) {
                	for (int k = 1; j < end && charEquals(source.charAt(j),target.charAt(k),caseSensitive); j++, k++);

                    if (j == end) {
                        /* Found whole string. */
                        return i;
                    }
                }
            }
        }
		
		return -1;
	}
	
	/**
	 * 复制String.lastIndexOf算法，主要目的是增加了 “区分大小写”搜索功能
	 * @param   source        the characters being searched.
	 * @param   target        the characters being searched for.
	 * @param   caseSensitive 搜索时是否区分大小写
	 * @param   wholeWord     搜索时是否整字匹配
	 * @param   fromIndex     the index to begin searching from.
	 * @return
	 */
	public static int lastIndexOfString(String source,String target,boolean caseSensitive,boolean wholeWord,int fromIndex) {
		if (target == null || source == null) {
			return -1;
		}
		final int sourceCount = source.length();
		final int targetCount = target.length();
		
		int rightIndex = sourceCount - targetCount;
        if (fromIndex < 0) {
            return -1;
        }
        if (fromIndex > rightIndex) {
            fromIndex = rightIndex;
        }
        /* Empty string always matches. */
        if (targetCount == 0) {
            return fromIndex;
        }

        int strLastIndex = targetCount - 1;
        char strLastChar = target.charAt(strLastIndex);
        char strFirstChar = target.charAt(0);
        int min = targetCount - 1;
        int i = min + fromIndex;
        
        startSearchForLastChar:
        while (true) {
            while (i >= min && !lastCharEquals(source,i,strLastChar,caseSensitive,wholeWord)) {
                i--;
            }
            if (i < min) {
                return -1;
            }
            int j = i - 1;
            int start = j - (targetCount - 1);
            int k = strLastIndex - 1;
            
            if (!firstCharEquals(source,start+1,strFirstChar,caseSensitive,wholeWord)) {
            	i--;
                continue startSearchForLastChar;
            } 
            
            while (j > start) {
                if (!charEquals(source.charAt(j--),target.charAt(k--),caseSensitive)) {
                    i--;
                    continue startSearchForLastChar;
                }
            }
            return start + 1;
        }
	}
	
	private static boolean firstCharEquals(String source,int sourceIndex,char c2,boolean caseSensitive,boolean wholeWord) {
		final int sourceLastIndex = source.length()-1;
		if (sourceIndex < 0 || sourceLastIndex < sourceIndex) {
			return false;
		}
		
		char c = source.charAt(sourceIndex);
		if (c != c2) {
			return false;
		}
		if (wholeWord && sourceIndex > 0) {
			c = source.charAt(sourceIndex-1);
			return !(Character.isDigit(c) || Character.isLetter(c));
		} else {
			return true;
		}
	}
	
	private static boolean lastCharEquals(String source,int sourceIndex,char c2,boolean caseSensitive,boolean wholeWord) {
		final int sourceLastIndex = source.length()-1;
		if (sourceIndex < 0 || sourceLastIndex < sourceIndex) {
			return false;
		}
		char c = source.charAt(sourceIndex);
		if (c != c2) {
			return false;
		}
		if (wholeWord && sourceIndex < sourceLastIndex) {
			c = source.charAt(sourceIndex+1);
			return !(Character.isDigit(c) || Character.isLetter(c));
		} else {
			return true;
		}
	}
	
	/**
	 * 比较两个char是否相等
	 * @param c1
	 * @param c2
	 * @param caseSensitive 是否区分大小写
	 * @return
	 */
	public static boolean charEquals(char c1,char c2,boolean caseSensitive) {
		if (c1 == c2) {
			return true;
		}
		return caseSensitive ? false : (Character.toLowerCase(c1) == Character.toLowerCase(c2));
	}
}
