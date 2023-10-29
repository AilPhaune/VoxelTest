package fr.ailphaune.voxeltest.multiplayer.packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import fr.ailphaune.voxeltest.utils.io.ExtendedDataInputStream;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataOutputStream;

public class DefaultPacket<T extends Serializable> extends Packet<DefaultPacketData<T>> {
	
	public DefaultPacket(String name) {
		super(name);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public DefaultPacketData<T> readData(ExtendedDataInputStream in) throws IOException {
		ObjectInputStream ois = new ObjectInputStream(in);
		try {
			return new DefaultPacketData<T>(this, (T) ois.readObject());
		} catch (Throwable e) {
			return null;
		} finally {
			ois.close();
		}
	}

	@Override
	public boolean writeUncheckedData(PacketData data, ExtendedDataOutputStream out) throws IOException {
		if(data == null || !(data instanceof DefaultPacketData)) return false;
		DefaultPacketData<?> d = (DefaultPacketData<?>) data;
		if(d.getData() == null) return false;
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(d.getData());
		oos.flush();
		return true;
	}
	
}