package fr.ailphaune.voxeltest.utils.io;

import java.io.ByteArrayOutputStream;

public class BetterByteArrayOutputStream extends ByteArrayOutputStream {

	public BetterByteArrayOutputStream() {
		super();
	}

	public BetterByteArrayOutputStream(int bufferSize) {
		super(bufferSize);
	}
	
	public void reset(int bufferSize) {
		reset();
		buf = new byte[bufferSize];
	}
	
	public void resize(int bufferSize) {
		byte[] newBuf = new byte[bufferSize];
		System.arraycopy(buf, 0, newBuf, 0, Math.min(size(), bufferSize));
		buf = newBuf;
	}
	
	public byte[] getBuffer() {
		return buf;
	}
	
	@Override
	public synchronized byte[] toByteArray() {
		if(buf.length == size()) return buf;
		return super.toByteArray();
	}
}