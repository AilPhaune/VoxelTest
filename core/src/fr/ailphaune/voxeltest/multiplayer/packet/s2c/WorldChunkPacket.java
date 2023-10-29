package fr.ailphaune.voxeltest.multiplayer.packet.s2c;

import java.io.IOException;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.multiplayer.packet.Packet;
import fr.ailphaune.voxeltest.multiplayer.packet.Packets;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataInputStream;
import fr.ailphaune.voxeltest.utils.io.ExtendedDataOutputStream;

public class WorldChunkPacket extends Packet<WorldChunkPacket.Data> {

	public WorldChunkPacket() {
		super("WorldChunkPacket");
	}

	@Override
	public Data readData(ExtendedDataInputStream in) throws IOException {
		Data data = new Data();
		
		int len;
		in.readChunkPos(data.chunkPos = new ChunkPos());
		
		len = in.readInt();
		in.readBytes(data.voxels = new byte[len]);
		
		len = in.readInt();
		in.readShorts(data.states = new short[len]);
		
		len = in.readInt();
		in.readShorts(data.biomes = new short[len]);
		
		return data;
	}

	@Override
	public boolean writeUncheckedData(PacketData data, ExtendedDataOutputStream out) throws IOException {
		if(data == null || !(data instanceof Data)) return false;
		Data d = (Data) data;
		if(d.voxels == null) return false;
		if(d.states == null) return false;
		if(d.biomes == null) return false;
		if(d.chunkPos == null) return false;
		out.writeChunkPos(d.chunkPos);
		out.writeInt(d.voxels.length);
		out.writeBytes(d.voxels);
		out.writeInt(d.states.length);
		out.writeShorts(d.states);
		out.writeInt(d.biomes.length);
		out.writeShorts(d.biomes);
		return true;
	}

	public Data fromChunk(Chunk chunk) {
		Data data = new Data();
		data.chunkPos = chunk.getChunkPos();
		data.states = chunk.states;
		data.voxels = chunk.voxels;
		data.biomes = new short[chunk.biomes.length];
		return data;
	}

	public static class Data implements Packet.PacketData {

		public ChunkPos chunkPos;
		public byte[] voxels;
		public short[] states;
		public short[] biomes;
		
		@Override
		public WorldChunkPacket getPacket() {
			return Packets.WORLD_CHUNK;
		}
	}
}