package fr.ailphaune.voxeltest.utils.io;

import java.io.InputStream;
import java.nio.ByteBuffer;

public class ByteBufferInputStream extends InputStream {

	private ByteBuffer bb;
	private boolean closed = false;
	
	public ByteBufferInputStream(ByteBuffer bb) {
		this.bb = bb;
	}
	
	@Override
	public int read() {
		if(closed || bb.remaining() <= 0) return -1;
		return Byte.toUnsignedInt(bb.get());
	}
	
	@Override
	public void close() {
		closed = true;
	}
}