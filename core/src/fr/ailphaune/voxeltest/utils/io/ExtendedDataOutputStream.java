package fr.ailphaune.voxeltest.utils.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.saves.RegionPos;

public class ExtendedDataOutputStream extends DataOutputStream {

	public ExtendedDataOutputStream(OutputStream out) {
		super(out);
	}
	
	public void writeByte(byte b) throws IOException {
		super.writeByte(b & 0xFF);
	}

	public void writeBytes(byte[] bytes) throws IOException {
		writeBytes(bytes, 0, bytes.length);
	}
	
	public void writeBytes(byte[] bytes, int offset, int length) throws IOException {
		write(bytes, offset, length);
	}
	
	public void writeChars(char[] chars) throws IOException {
		writeChars(chars, 0, chars.length);
	}
	
	public void writeChars(char[] chars, int offset, int length) throws IOException {
		final int end = offset + length;
		if(offset >= chars.length || end > chars.length) throw new ArrayIndexOutOfBoundsException();
		for(int i = offset; i < end; i++) {
			writeChar(chars[i]);
		}
	}

	public void writeShorts(short[] shorts) throws IOException {
		writeShorts(shorts, 0, shorts.length);
	}
	
	public void writeShorts(short[] shorts, int offset, int length) throws IOException {
		final int end = offset + length;
		if(offset >= shorts.length || end > shorts.length) throw new ArrayIndexOutOfBoundsException();
		for(int i = offset; i < end; i++) {
			writeShort(shorts[i]);
		}
	}

	public void writeInts(int[] ints) throws IOException {
		writeInts(ints, 0, ints.length);
	}
	
	public void writeInts(int[] ints, int offset, int length) throws IOException {
		final int end = offset + length;
		if(offset >= ints.length || end > ints.length) throw new ArrayIndexOutOfBoundsException();
		for(int i = offset; i < end; i++) {
			writeInt(ints[i]);
		}
	}
	
	public void writeLongs(long[] longs) throws IOException {
		writeLongs(longs, 0, longs.length);
	}
	
	public void writeLongs(long[] longs, int offset, int length) throws IOException {
		final int end = offset + length;
		if(offset >= longs.length || end > longs.length) throw new ArrayIndexOutOfBoundsException();
		for(int i = offset; i < end; i++) {
			writeLong(longs[i]);
		}
	}

	public void writeChunkPos(ChunkPos pos) throws IOException {
		writeInt(pos.x);
		writeInt(pos.y);
		writeInt(pos.z);
	}
	
	public void writeVoxelPos(VoxelPos pos) throws IOException {
		writeInt(pos.x);
		writeInt(pos.y);
		writeInt(pos.z);
	}
	
	public void writeRegionPos(RegionPos pos) throws IOException {
		writeInt(pos.x);
		writeInt(pos.y);
		writeInt(pos.z);
	}
	
	public void writeVector2(Vector2 vec) throws IOException {
		writeFloat(vec.x);
		writeFloat(vec.y);
	}
	
	public void writeVector3(Vector3 vec) throws IOException {
		writeFloat(vec.x);
		writeFloat(vec.y);
		writeFloat(vec.z);
	}
}