package fr.ailphaune.voxeltest.multiplayer.packet.s2c;

import java.io.IOException;
import java.util.UUID;

import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataInputStream;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataOutputStream;

public class JoinWorldPacket extends Packet<JoinWorldPacket.Data> {

	public JoinWorldPacket() {
		super("JoinWorldPacket");
	}
	
	@Override
	public Data readData(ExtendedDataInputStream stream) throws IOException {
		Data data = new Data();
		data.uuid = new UUID(stream.readLong(), stream.readLong());
		data.hp = stream.readLong();
		stream.readVector3(data.position);
		data.yaw = stream.readFloat();
		data.pitch = stream.readFloat();
		return data;
	}

	@Override
	public boolean writeUncheckedData(PacketData data, ExtendedDataOutputStream stream) throws IOException {
		if(data == null || !(data instanceof Data)) return false;
		Data d = (Data) data;
		if(d.position == null || d.uuid == null) return false;
		stream.writeLong(d.uuid.getMostSignificantBits());
		stream.writeLong(d.uuid.getLeastSignificantBits());
		stream.writeLong(d.hp);
		stream.writeVector3(d.position);
		stream.writeFloat(d.yaw);
		stream.writeFloat(d.pitch);
		return true;
	}

	public static class Data implements Packet.PacketData {

		public UUID uuid;
		public long hp;
		public final Vector3 position = new Vector3();
		public float yaw, pitch;
		
		@Override
		public JoinWorldPacket getPacket() {
			return Packets.JOIN_WORLD;
		}
	}
}