package fr.ailphaune.voxeltest.utils;

public class DataTypeUtils {
	
	private static final char[] HEX = "0123456789ABCDEF".toCharArray();
	
	public static String bytesToHex(byte[] bytes, int offset, int length) {
		char[] hex = new char[bytes.length * 2];
		int hI = 0, v;
		for(int i = 0; i < length; i++) {
			v = bytes[i+offset] & 0xFF;
			hex[hI++] = HEX[v >>> 4];
			hex[hI++] = HEX[v & 0xF];
		}
		return new String(hex);
	}
	
}