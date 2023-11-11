package fr.ailphaune.voxeltest.utils;

public class ArrayUtils {

	@FunctionalInterface
	public static interface BooleanFunction<T> {
		public boolean apply(T value);
	}
	
	public static <T> boolean contains(T[] array, int start, int count, T value) {
		for(int i = 0; i < count; i++) {
			if(i + start >= array.length) break;
			if(array[i + start] == value) return true;
		}
		return false;
	}
	
	public static <T> boolean contains(T[] array, int start, int count, BooleanFunction<T> comparator) {
		for(int i = 0; i < count; i++) {
			if(i + start >= array.length) break;
			if(comparator.apply(array[i + start])) return true;
		}
		return false;
	}
	
	public static boolean contains(boolean[] array, int start, int count, boolean value) {
		for(int i = 0; i < count; i++) {
			if(i + start >= array.length) break;
			if(array[i + start] == value) return true;
		}
		return false;
	}
	
	public static boolean contains(byte[] array, int start, int count, byte value) {
		for(int i = 0; i < count; i++) {
			if(i + start >= array.length) break;
			if(array[i + start] == value) return true;
		}
		return false;
	}
	
	public static boolean contains(short[] array, int start, int count, short value) {
		for(int i = 0; i < count; i++) {
			if(i + start >= array.length) break;
			if(array[i + start] == value) return true;
		}
		return false;
	}
	
	public static boolean contains(char[] array, int start, int count, char value) {
		for(int i = 0; i < count; i++) {
			if(i + start >= array.length) break;
			if(array[i + start] == value) return true;
		}
		return false;
	}
	
	public static boolean contains(int[] array, int start, int count, int value) {
		for(int i = 0; i < count; i++) {
			if(i + start >= array.length) break;
			if(array[i + start] == value) return true;
		}
		return false;
	}
	
	public static boolean contains(long[] array, int start, int count, long value) {
		for(int i = 0; i < count; i++) {
			if(i + start >= array.length) break;
			if(array[i + start] == value) return true;
		}
		return false;
	}
	
	public static <T> boolean contains(T[] array, T value) {
		return contains(array, 0, array.length, value);
	}
	
	public static <T> boolean contains(T[] array, BooleanFunction<T> comparator) {
		return contains(array, 0, array.length, comparator);
	}
	
	public static boolean contains(boolean[] array, boolean value) {
		return contains(array, 0, array.length, value);
	}
	
	public static boolean contains(byte[] array, byte value) {
		return contains(array, 0, array.length, value);
	}
	
	public static boolean contains(short[] array, short value) {
		return contains(array, 0, array.length, value);
	}
	
	public static boolean contains(char[] array, char value) {
		return contains(array, 0, array.length, value);
	}
	
	public static boolean contains(int[] array, int value) {
		return contains(array, 0, array.length, value);
	}
	
	public static boolean contains(long[] array, long value) {
		return contains(array, 0, array.length, value);
	}
}