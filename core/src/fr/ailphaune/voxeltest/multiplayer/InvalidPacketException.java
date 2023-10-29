package fr.ailphaune.voxeltest.multiplayer;

import java.io.IOException;

public class InvalidPacketException extends IOException {

	private static final long serialVersionUID = 2841313146333975630L;

	public InvalidPacketException() {
        super();
    }

	public InvalidPacketException(String message) {
        super(message);
    }

	public InvalidPacketException(String message, Throwable cause) {
        super(message, cause);
    }

	public InvalidPacketException(Throwable cause) {
        super(cause);
    }
}