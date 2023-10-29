package fr.ailphaune.voxeltest.multiplayer.packet.c2s;

import java.io.IOException;

import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataInputStream;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataOutputStream;

public class PlayerMovePacket extends Packet<PlayerMovePacket.Data> {

	public PlayerMovePacket() {
		super("PlayerMovePacket");
	}

	@Override
	public Data readData(ExtendedDataInputStream stream) throws IOException {
		Data data = new Data();
		stream.readVector3(data.position);
		data.yaw = stream.readFloat();
		data.pitch = stream.readFloat();
		return data;
	}

	@Override
	public boolean writeUncheckedData(PacketData data, ExtendedDataOutputStream stream) throws IOException {
		if(data == null || !(data instanceof Data)) return false;
		Data d = (Data) data;
		if(d.position == null) return false;
		stream.writeVector3(d.position);
		stream.writeFloat(d.yaw);
		stream.writeFloat(d.pitch);
		return true;
	}

	public static class Data implements Packet.PacketData {

		public final Vector3 position = new Vector3();
		public float yaw, pitch;
		
		@Override
		public PlayerMovePacket getPacket() {
			return Packets.PLAYER_MOVE_PACKET;
		}
		
		public void from(Vector3 position) {
			this.position.set(position);
		}
	}
}