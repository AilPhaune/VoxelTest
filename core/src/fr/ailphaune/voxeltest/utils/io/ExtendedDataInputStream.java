package fr.ailphaune.voxeltest.utils.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.VoxelPos;

public class ExtendedDataInputStream extends DataInputStream {

	public ExtendedDataInputStream(InputStream out) {
		super(out);
	}

	public void readBytes(byte[] bytes) throws IOException {
		readBytes(bytes, 0, bytes.length);
	}
	
	public void readBytes(byte[] bytes, int offset, int length) throws IOException {
		read(bytes, offset, length);
	}
	
	public void readChars(char[] chars) throws IOException {
		readChars(chars, 0, chars.length);
	}
	
	public void readChars(char[] chars, int offset, int length) throws IOException {
		final int end = offset + length;
		if(offset >= chars.length || end > chars.length) throw new ArrayIndexOutOfBoundsException();
		for(int i = offset; i < end; i++) {
			chars[i] = readChar();
		}
	}

	public void readShorts(short[] shorts) throws IOException {
		readShorts(shorts, 0, shorts.length);
	}
	
	public void readShorts(short[] shorts, int offset, int length) throws IOException {
		final int end = offset + length;
		if(offset >= shorts.length || end > shorts.length) throw new ArrayIndexOutOfBoundsException();
		for(int i = offset; i < end; i++) {
			shorts[i] = readShort();
		}
	}

	public void readInts(int[] ints) throws IOException {
		readInts(ints, 0, ints.length);
	}
	
	public void readInts(int[] ints, int offset, int length) throws IOException {
		final int end = offset + length;
		if(offset >= ints.length || end > ints.length) throw new ArrayIndexOutOfBoundsException();
		for(int i = offset; i < end; i++) {
			ints[i] = readInt();
		}
	}
	
	public void readLongs(long[] longs) throws IOException {
		readLongs(longs, 0, longs.length);
	}
	
	public void readLongs(long[] longs, int offset, int length) throws IOException {
		final int end = offset + length;
		if(offset >= longs.length || end > longs.length) throw new ArrayIndexOutOfBoundsException();
		for(int i = offset; i < end; i++) {
			longs[i] = readLong();
		}
	}

	public ChunkPos readChunkPos(ChunkPos out) throws IOException {
		return out.set(readInt(), readInt(), readInt());
	}
	
	public VoxelPos readVoxelPos(VoxelPos out) throws IOException {
		return out.set(readInt(), readInt(), readInt());
	}
	
	public Vector2 readVector2(Vector2 out) throws IOException {
		return out.set(readFloat(), readFloat());
	}
	
	public Vector3 readVector3(Vector3 out) throws IOException {
		return out.set(readFloat(), readFloat(), readFloat());
	}
}