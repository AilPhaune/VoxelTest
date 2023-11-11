package fr.ailphaune.voxeltest.light;

import java.util.Arrays;
import java.util.HashSet;

import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Queue;

import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.voxels.AbstractVoxel;
import fr.ailphaune.voxeltest.voxels.Voxels;

public class Lighting {
	
	public static short getLight(int r, int g, int b) {
		return (short) (((r & 0xF) << 8) | ((g & 0xF) << 4) | (b & 0xF));
	}
	
	public static short getLight(int r, int g, int b, int sky) {
		return (short) (((sky & 0xF) << 12) | ((r & 0xF) << 8) | ((g & 0xF) << 4) | (b & 0xF));
	}
	
	public static short addLight(short light1, short light2) {
		int sky1 = 0xF & (light1 >> 12);
		int r1 = 0xF & (light1 >> 8);
		int g1 = 0xF & (light1 >> 4);
		int b1 = 0xF & (light1 >> 0);
		int sky2 = 0xF & (light2 >> 12);
		int r2 = 0xF & (light2 >> 8);
		int g2 = 0xF & (light2 >> 4);
		int b2 = 0xF & (light2 >> 0);
		int r = r1 > r2 ? r1 : r2;
		int g = g1 > g2 ? g1 : g2;
		int b = b1 > b2 ? b1 : b2;
		int sky = sky1 > sky2 ? sky1 : sky2;
		return getLight(r, g, b, sky);
	}
	
	public static short decreaseLight(short light) {
		int sky = 0xF & (light >> 12);
		int r = 0xF & (light >> 8);
		int g = 0xF & (light >> 4);
		int b = 0xF & (light >> 0);
		if(r == 0) r++;
		if(g == 0) g++;
		if(b == 0) b++;
		if(sky == 0) sky++;
		return getLight(r-1, g-1, b-1, sky-1);
	}
	
	public static short decreaseLightKeepSky(short light) {
		int sky = 0xF & (light >> 12);
		int r = 0xF & (light >> 8);
		int g = 0xF & (light >> 4);
		int b = 0xF & (light >> 0);
		if(r == 0) r++;
		if(g == 0) g++;
		if(b == 0) b++;
		return getLight(r-1, g-1, b-1, sky);
	}
	
	public static class LightQueue extends Pool<VoxelPos> {
		
		public Queue<VoxelPos> queue = new Queue<>();
		public HashSet<VoxelPos> set = new HashSet<>();
		
		public LightQueue() {
			super();
		}

		public LightQueue(int poolInitialCapacity) {
			super(poolInitialCapacity);
		}

		public LightQueue(int poolInitialCapacity, int poolMax) {
			super(poolInitialCapacity, poolMax);
		}
		
		@Override
		protected VoxelPos newObject() {
			return new VoxelPos();
		}
		
		public VoxelPos removeFirst(VoxelPos out) {
			VoxelPos pos = queue.removeFirst();
			set.remove(pos);
			out.set(pos);
			free(pos);
			return out;
		}
		
		public void addLast(int x, int y, int z) {
			VoxelPos pos = obtain();
			pos.set(x, y, z);
			if(set.contains(pos)) {
				free(pos);
				return;
			}
			set.add(pos);
			queue.addLast(pos);
		}
		
		public void addLast(VoxelPos pos) {
			if(set.contains(pos)) return;
			VoxelPos p = obtain();
			p.set(pos);
			set.add(p);
			queue.addLast(p);
		}
		
		public void clearQueue() {
			for(VoxelPos pos : queue) {
				free(pos);
			}
			queue.clear();
		}
	}
	
	public LightQueue LIGHT_QUEUE;

	private VoxelPos tempVoxelPos = new VoxelPos();
	private VoxelPos tempVoxelPos2 = new VoxelPos();
	
	public Lighting() {
		LIGHT_QUEUE = new LightQueue();
	}
	
	public synchronized void calculateLight(World world, Chunk chunk) {
		LIGHT_QUEUE.clearQueue();
		scanChunk(world, chunk);
		short light, nLight, neighborLight;
		// TODO: sky light
		// short sky;
		while(LIGHT_QUEUE.queue.size > 0) {
			VoxelPos pos = LIGHT_QUEUE.removeFirst(tempVoxelPos2);
			if(!world.isValidPosition(pos)) continue;
			// chunk.getChunkPos().getRelativeVoxelPos(pos, tempVoxelPos);
			light = world.getLightFast(pos.x, pos.y, pos.z);
			neighborLight = decreaseLight(light);
			//sky = decreaseLightKeepSky(light);
			
			// -X
			if(!Voxels.getAbstractVoxel(world.getVoxelIfLoaded(pos.x - 1, pos.y, pos.z)).isOpaque(chunk, tempVoxelPos.set(pos.x - 1, pos.y, pos.z))) {
				light = world.getLightFast(pos.x - 1, pos.y, pos.z);
				nLight = addLight(light, neighborLight);
				if(light != nLight) {
					world.setLight(pos.x - 1, pos.y, pos.z, nLight);
					LIGHT_QUEUE.addLast(pos.x - 1, pos.y, pos.z);
				}
			}
			// +X
			if(!Voxels.getAbstractVoxel(world.getVoxelIfLoaded(pos.x + 1, pos.y, pos.z)).isOpaque(chunk, tempVoxelPos.set(pos.x + 1, pos.y, pos.z))) {
				light = world.getLightFast(pos.x + 1, pos.y, pos.z);
				nLight = addLight(light, neighborLight);
				if(light != nLight) {
					world.setLight(pos.x + 1, pos.y, pos.z, nLight);
					LIGHT_QUEUE.addLast(pos.x + 1, pos.y, pos.z);
				}
			}
			// -Y
			if(!Voxels.getAbstractVoxel(world.getVoxelIfLoaded(pos.x, pos.y - 1, pos.z)).isOpaque(chunk, tempVoxelPos.set(pos.x, pos.y - 1, pos.z))) {
				light = world.getLightFast(pos.x, pos.y - 1, pos.z);
				nLight = addLight(light, neighborLight);
				if(light != nLight) {
					world.setLight(pos.x, pos.y - 1, pos.z, nLight);
					LIGHT_QUEUE.addLast(pos.x, pos.y - 1, pos.z);
				}
			}
			// +Y
			if(!Voxels.getAbstractVoxel(world.getVoxelIfLoaded(pos.x, pos.y + 1, pos.z)).isOpaque(chunk, tempVoxelPos.set(pos.x, pos.y + 1, pos.z))) {
				light = world.getLightFast(pos.x, pos.y + 1, pos.z);
				nLight = addLight(light, neighborLight);
				if(light != nLight) {
					world.setLight(pos.x, pos.y + 1, pos.z, nLight);
					LIGHT_QUEUE.addLast(pos.x, pos.y + 1, pos.z);
				}
			}
			// -Z
			if(!Voxels.getAbstractVoxel(world.getVoxelIfLoaded(pos.x, pos.y, pos.z - 1)).isOpaque(chunk, tempVoxelPos.set(pos.x, pos.y, pos.z - 1))) {
				light = world.getLightFast(pos.x, pos.y, pos.z - 1);
				nLight = addLight(light, neighborLight);
				if(light != nLight) {
					world.setLight(pos.x, pos.y, pos.z - 1, nLight);
					LIGHT_QUEUE.addLast(pos.x, pos.y, pos.z - 1);
				}
			}
			// +Z
			if(!Voxels.getAbstractVoxel(world.getVoxelIfLoaded(pos.x, pos.y, pos.z + 1)).isOpaque(chunk, tempVoxelPos.set(pos.x, pos.y, pos.z + 1))) {
				light = world.getLightFast(pos.x, pos.y, pos.z + 1);
				nLight = addLight(light, neighborLight);
				if(light != nLight) {
					world.setLight(pos.x, pos.y, pos.z + 1, nLight);
					LIGHT_QUEUE.addLast(pos.x, pos.y, pos.z + 1);
				}
			}
		}
		chunk.needsRelighting = false;
	}
	
	protected synchronized void scanChunk(World world, Chunk chunk) {
		synchronized(chunk.light) {
			Arrays.fill(chunk.light, (short) 0);
			for(int x = 0; x < Chunk.SIZE; x++) {
				for(int y = 0; y < Chunk.SIZE; y++) {
					for(int z = 0; z < Chunk.SIZE; z++) {
						boolean added = false;
						AbstractVoxel voxel = Voxels.getAbstractVoxel(chunk.get(x, y, z));
						if(voxel == null) {
							continue;
						}
						short light = voxel.getLightLevel(chunk, chunk.getVoxelPos(x, y, z, tempVoxelPos));
						if(light != 0 && !added) {
							LIGHT_QUEUE.addLast(tempVoxelPos);
							added = true;
						}
						chunk.setLight(x, y, z, addLight(chunk.getLight(x, y, z), light));
						if(voxel.isOpaque(chunk, tempVoxelPos)) continue;
						
						if(x == 0) {
							light = decreaseLight(world.getLightFast(tempVoxelPos.x - 1, tempVoxelPos.y, tempVoxelPos.z));
							chunk.setLight(x, y, z, addLight(chunk.getLight(x, y, z), light));
							if(light != 0 && !added) {
								LIGHT_QUEUE.addLast(tempVoxelPos);
								added = true;
							}
						}
						if(y == 0) {
							light = decreaseLight(world.getLightFast(tempVoxelPos.x, tempVoxelPos.y - 1, tempVoxelPos.z));
							chunk.setLight(x, y, z, addLight(chunk.getLight(x, y, z), light));
							if(light != 0 && !added) {
								LIGHT_QUEUE.addLast(tempVoxelPos);
								added = true;
							}
						}
						if(z == 0) {
							light = decreaseLight(world.getLightFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z - 1));
							chunk.setLight(x, y, z, addLight(chunk.getLight(x, y, z), light));
							if(light != 0 && !added) {
								LIGHT_QUEUE.addLast(tempVoxelPos);
								added = true;
							}
						}
						if(x == Chunk.SIZE - 1) {
							light = decreaseLight(world.getLightFast(tempVoxelPos.x + 1, tempVoxelPos.y, tempVoxelPos.z));
							chunk.setLight(x, y, z, addLight(chunk.getLight(x, y, z), light));
							if(light != 0 && !added) {
								LIGHT_QUEUE.addLast(tempVoxelPos);
								added = true;
							}
						}
						if(y == Chunk.SIZE - 1) {
							light = decreaseLight(world.getLightFast(tempVoxelPos.x, tempVoxelPos.y + 1, tempVoxelPos.z));
							chunk.setLight(x, y, z, addLight(chunk.getLight(x, y, z), light));
							if(light != 0 && !added) {
								LIGHT_QUEUE.addLast(tempVoxelPos);
								added = true;
							}
						}
						if(z == Chunk.SIZE - 1) {
							light = decreaseLight(world.getLightFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z + 1));
							chunk.setLight(x, y, z, addLight(chunk.getLight(x, y, z), light));
							if(light != 0 && !added) {
								LIGHT_QUEUE.addLast(tempVoxelPos);
								added = true;
							}
						}
					}
				}
			}
		}
	}
}