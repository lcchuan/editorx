package lcc.utils;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;

public class SystemInfo {
	public static class NetWorkInfo{
		public String macDescription;
		public String macAddress;
		public String ipv4;
		public String ipv6;
	}
	
	/**
	 * 获取网络信息
	 * @return 
	 */
	public static ArrayList<NetWorkInfo> getNetWorkInfo() {
		ArrayList<NetWorkInfo> macs = new ArrayList<NetWorkInfo>();
		try {
			// 返回所有网络接口的一个枚举实例
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				// 获得当前网络接口
				NetworkInterface network = e.nextElement();
				if (network == null || network.getHardwareAddress() == null) {
					continue;
				}
				
				byte[] address = network.getHardwareAddress();
				if (address.length < 1) {
					continue;
				}
				NetWorkInfo info = new NetWorkInfo();
				info.macDescription = network.getDisplayName();
				info.macAddress = getMacFromBytes(address);
				Enumeration<InetAddress> ips = network.getInetAddresses();
				while (ips.hasMoreElements()) {
					InetAddress ia = ips.nextElement();
					if (ia == null) {
						continue;
					} else if (ia.isLinkLocalAddress()) {
						continue;
					} else if (ia instanceof Inet4Address) {
						info.ipv4 = ((Inet4Address)ia).getHostAddress();
					} else if (ia instanceof Inet6Address) {
						info.ipv6 = ((Inet6Address)ia).getHostAddress();
					}
				}
				macs.add(info);
			}
			return macs;
		} catch (Exception e) {
			return null;
		}
	}
	
	private static String getMacFromBytes(byte[] bytes) {
		StringBuffer mac = new StringBuffer();
		byte currentByte;
        boolean first = false;
        for (byte b : bytes) {
            if(first) {
                mac.append("-");
            }
            currentByte = (byte)((b&240)>>4);
            mac.append(Integer.toHexString(currentByte));
            currentByte=(byte)(b&15);
            mac.append(Integer.toHexString(currentByte));
            first=true;
        }
        return mac.toString().toUpperCase();
	}
}
