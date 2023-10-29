package fr.ailphaune.voxeltest.data.world;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import com.badlogic.gdx.graphics.Camera;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.VoxelTarget;
import fr.ailphaune.voxeltest.light.Lighting;

public class ClientWorldProxy extends ClientWorld {
	
	private World world;
	
	public ClientWorldProxy(World world) {
		super(world.getLightingEngine());
		this.world = world;
	}
	
	@Override
	public void addLoadedChunk(Chunk sourceChunk) {
		world.addLoadedChunk(sourceChunk);
	}
	
	@Override
	public synchronized void calculateLightQueue() {
		world.calculateLightQueue();
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return new ClientWorldProxy(world);
	}
	
	@Override
	public void dispose() {
		world.dispose();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj == this || world.equals(obj);
	}
	
	@Override
	public synchronized void generateChunk(Chunk chunk) {
		world.generateChunk(chunk);
	}
	
	@Override
	public Chunk getChunk(ChunkPos pos) {
		return world.getChunk(pos);
	}
	
	@Override
	public synchronized Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
		return world.getChunk(chunkX, chunkY, chunkZ);
	}
	
	@Override
	public long getChunkSeed(ChunkPos pos) {
		return 0;
	}
	
	@Override
	public long getCompoundSeed(long compound) {
		return 0;
	}
	
	@Override
	public synchronized short getLight(int x, int y, int z) {
		return world.getLight(x, y, z);
	}
	
	@Override
	public synchronized short getLightFast(int x, int y, int z) {
		return world.getLightFast(x, y, z);
	}
	
	@Override
	public Lighting getLightingEngine() {
		return world.getLightingEngine();
	}
	
	@Override
	public Chunk getLoadedChunk(ChunkPos pos) {
		return world.getLoadedChunk(pos);
	}
	
	@Override
	public synchronized Chunk getLoadedChunk(int chunkX, int chunkY, int chunkZ) {
		return world.getLoadedChunk(chunkX, chunkY, chunkZ);
	}
	
	@Override
	public int getLoadedChunkCount() {
		return world.getLoadedChunkCount();
	}
	
	@Override
	public Set<Entry<ChunkPos, Chunk>> getLoadedChunks() {
		return world.getLoadedChunks();
	}
	
	@Override
	public synchronized VoxelTarget getTargetedVoxel(Camera camera, int maxDistance) {
		return world.getTargetedVoxel(camera, maxDistance);
	}
	
	@Override
	public synchronized byte getVoxel(int x, int y, int z) {
		return world.getVoxel(x, y, z);
	}
	
	@Override
	public byte getVoxel(VoxelPos pos) {
		return world.getVoxel(pos);
	}
	
	@Override
	public synchronized byte getVoxelIfLoaded(int x, int y, int z) {
		return world.getVoxelIfLoaded(x, y, z);
	}
	
	@Override
	public synchronized short getVoxelState(int x, int y, int z) {
		return world.getVoxelState(x, y, z);
	}
	
	@Override
	public synchronized short getVoxelState(VoxelPos pos) {
		return world.getVoxelState(pos);
	}

	@Override
	public int hashCode() {
		return Objects.hash(world, false);
	}
	
	@Override
	public boolean isClient() {
		return false;
	}
	
	@Override
	public boolean isServer() {
		return false;
	}
	
	@Override
	public boolean isValidChunk(ChunkPos pos) {
		return world.isValidChunk(pos);
	}
	
	@Override
	public boolean isValidPosition(VoxelPos pos) {
		return world.isValidPosition(pos);
	}
	
	@Override
	public synchronized void setLight(int x, int y, int z, short light) {
		world.setLight(x, y, z, light);
	}
	
	@Override
	public synchronized void setVoxel(int x, int y, int z, byte voxel) {
		world.setVoxel(x, y, z, voxel);
	}
	
	@Override
	public void setVoxel(VoxelPos pos, byte voxel) {
		world.setVoxel(pos, voxel);
	}
	
	@Override
	public synchronized void setVoxelState(int x, int y, int z, short state) {
		world.setVoxelState(x, y, z, state);
	}
	
	@Override
	public void setVoxelState(VoxelPos pos, short state) {
		world.setVoxelState(pos, state);
	}
	
	@Override
	public synchronized void tick() {}
	
	@Override
	public String toString() {
		return "ClientWorldProxy{world=" + world + "}";
	}
	
	@Override
	public synchronized void tryUnloadChunks() throws IOException {
		world.tryUnloadChunks();
	}
	
	@Override
	public synchronized void updateLightSync(Chunk chunk) {
		world.updateLightSync(chunk);
	}
}