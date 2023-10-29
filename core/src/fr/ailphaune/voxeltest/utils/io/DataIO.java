package fr.ailphaune.voxeltest.utils.io;

import java.nio.ByteBuffer;

public class DataIO {

	private static byte[] bytes2 = new byte[2];
	private static byte[] bytes4 = new byte[4];
	private static byte[] bytes8 = new byte[8];

	private static ByteBuffer bb2 = ByteBuffer.wrap(bytes2);
	private static ByteBuffer bb4 = ByteBuffer.wrap(bytes4);
	private static ByteBuffer bb8 = ByteBuffer.wrap(bytes8);

	public static byte[] writeShort(short s) {
		bb2.putShort(0, s);
		return bytes2;
	}
	
	public static byte[] writeChar(char c) {
		bb2.putChar(0, c);
		return bytes2;
	}
	
	public static byte[] writeInt(int i) {
		bb4.putInt(0, i);
		return bytes4;
	}

	public static byte[] writeLong(long l) {
		bb8.putLong(0, l);
		return bytes8;
	}
	
	public static short readShort(byte[] bytes) {
		System.arraycopy(bytes, 0, bytes2, 0, 2);
		return bb2.getShort(0);
	}
	
	public static char readChar(byte[] bytes) {
		System.arraycopy(bytes, 0, bytes2, 0, 2);
		return bb2.getChar(0);
	}
	
	public static int readInt(byte[] bytes) {
		System.arraycopy(bytes, 0, bytes4, 0, 4);
		return bb2.getInt(0);
	}

	public static long readLong(byte[] bytes) {
		System.arraycopy(bytes, 0, bytes8, 0, 8);
		return bb8.getLong(0);
	}
}