package fr.ailphaune.voxeltest.multiplayer.packet.s2c;

import java.io.IOException;

import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataInputStream;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataOutputStream;

public class VoxelUpdatesPacket extends Packet<VoxelUpdatesPacket.Data> {
	
	public VoxelUpdatesPacket() {
		super("VoxelUpdatesPacket");
	}
	
	@Override
	public Data readData(ExtendedDataInputStream stream) throws IOException {
		Data data = new Data();
		int len = stream.readInt();
		data.updates = new Update[len];
		for(int i = 0; i < len; i++) {
			Update u = (data.updates[i] = new Update());
			u.x = stream.readInt();
			u.y = stream.readInt();
			u.z = stream.readInt();
			u.voxel = stream.readByte();
			u.state = stream.readShort();
		}
		return data;
	}

	@Override
	public boolean writeUncheckedData(PacketData data, ExtendedDataOutputStream stream) throws IOException {
		if(data == null || !(data instanceof Data)) return false;
		Data d = (Data) data;
		if(d.updates == null) return false;
		stream.writeInt(d.updates.length);
		for(Update u : d.updates) {
			stream.writeInt(u.x);
			stream.writeInt(u.y);
			stream.writeInt(u.z);
			stream.writeByte(u.voxel);
			stream.writeShort(u.state);
		}
		return true;
	}

	public static class Update {
		
		public int x, y, z;
		public short state;
		public byte voxel;
		
	}
	
	public static class Data implements Packet.PacketData {

		public Update[] updates;
		
		@Override
		public VoxelUpdatesPacket getPacket() {
			return Packets.VOXEL_UPDATES;
		}
	}
	
}