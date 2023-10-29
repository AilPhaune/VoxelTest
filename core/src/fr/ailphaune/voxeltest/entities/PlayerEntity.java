package fr.ailphaune.voxeltest.entities;

import java.util.UUID;

import com.badlogic.gdx.math.Vector3;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.multiplayer.Client;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.DestroyVoxelPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.PlaceVoxelPacket;
import fr.ailphaune.voxeltest.multiplayer.packet.c2s.PlayerMovePacket;

public class PlayerEntity extends LivingEntity {

	protected Client client;

	private PlayerMovePacket.Data moveDataPacket = new PlayerMovePacket.Data();
	private DestroyVoxelPacket.Data destroyPacketData = new DestroyVoxelPacket.Data();
	private PlaceVoxelPacket.Data placePacketData = new PlaceVoxelPacket.Data();
	
	private int reachDestroy = 10;
	
	public PlayerEntity(UUID uuid, Vector3 position, long hp) {
		super(uuid, position, hp);
	}
	
	public Vector3 getCameraPosition(Vector3 out) {
		return out.set(position).add(0, 1, 0);
	}

	public VoxelPos getVoxelPos(VoxelPos out) {
		return out.set((int) position.x, (int) position.y, (int) position.z);
	}
	
	public Client getClient() {
		return client;
	}
	
	public void setClient(Client newClient) {
		client = newClient;
	}
	
	public boolean hasClient() {
		return client != null && client.isConnected();
	}
	
	@Override
	public Vector3 setPosition(Vector3 newPosition) {
		Vector3 pos = super.setPosition(newPosition);
		if(hasClient()) {
			moveDataPacket.from(position);
			client.sendPacket(moveDataPacket);
		}
		return pos;
	}
	
	public void destroyVoxel(int x, int y, int z) {
		if(hasClient()) {
			destroyPacketData.position.set(x, y, z);
			client.sendPacket(destroyPacketData);
		}
	}
	
	public void destroyVoxel(VoxelPos pos) {
		destroyVoxel(pos.x, pos.y, pos.z);
	}
	
	public void placeVoxel(int x, int y, int z, byte voxel) {
		if(hasClient()) {
			placePacketData.position.set(x, y, z);
			placePacketData.voxel = voxel;
			client.sendPacket(placePacketData);
		}
	}
	
	public void placeVoxel(VoxelPos pos, byte voxel) {
		placeVoxel(pos.x, pos.y, pos.z, voxel);
	}

	public int getMaxReachDestroy() {
		return reachDestroy;
	}
	
	public void setMaxReachDestroy(int reach) {
		reachDestroy = reach;
	}
}