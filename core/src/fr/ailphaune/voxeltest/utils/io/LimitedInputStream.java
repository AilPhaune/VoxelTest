package fr.ailphaune.voxeltest.utils.io;

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream {

	protected InputStream stream;
	protected int totalRead;
	protected final int maxRead;

	public LimitedInputStream(InputStream stream, int maxRead) {
		this.stream = stream;
		this.maxRead = maxRead;
	}

	@Override
	public int read() throws IOException {
		totalRead++;
		if (totalRead > maxRead) {
			return -1;
		}
		return stream.read();
	}

	// Consumes the remaining bytes
	public void consume() throws IOException {
		while(read() >= 0);
	}
	
	@Override
	public void close() throws IOException {
		consume();
		stream.close();
	}
	
	@Override
	public int available() throws IOException {
		return Math.min(stream.available(), maxRead - totalRead);
	}
}