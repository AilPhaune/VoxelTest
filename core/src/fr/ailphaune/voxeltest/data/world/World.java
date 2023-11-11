package fr.ailphaune.voxeltest.data.world;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;

import fr.ailphaune.voxeltest.data.ChunkPos;
import fr.ailphaune.voxeltest.data.VoxelPos;
import fr.ailphaune.voxeltest.data.VoxelTarget;
import fr.ailphaune.voxeltest.light.Lighting;
import fr.ailphaune.voxeltest.multiplayer.server.events.ServerChunkLoadedEvent;
import fr.ailphaune.voxeltest.render.voxel.VoxelIntersect;
import fr.ailphaune.voxeltest.saves.WorldSaver;
import fr.ailphaune.voxeltest.voxels.Voxels;
import fr.ailphaune.voxeltest.worldgen.WorldGenerator;

public class World implements Disposable {

	private final static long murmurHash3(long x) {
		x ^= x >>> 33;
		x *= 0xff51afd7ed558ccdL;
		x ^= x >>> 33;
		x *= 0xc4ceb9fe1a85ec53L;
		x ^= x >>> 33;

		return x;
	}

	private static final int MAX_CHUNK_COORD = 50_000_000;

	protected ConcurrentHashMap<ChunkPos, Chunk> loaded_chunks;

	public final long seed;

	protected long chunk_seed_data[];

	protected ChunkPos tempChunkPos = new ChunkPos();
	protected VoxelPos tempVoxelPos = new VoxelPos();

	protected WorldSaver saver;
	protected WorldGenerator generator;
	protected Lighting lighting;

	protected Set<Chunk> relightQueue;
	protected ConcurrentLinkedQueue<Runnable> worldGenTasks = new ConcurrentLinkedQueue<>();

	public final List<WorldLoadRegion> loadRegions;

	public final WorldTicker ticker;

	public World(long seed, Lighting lighting, boolean serverWorld) {
		loaded_chunks = new ConcurrentHashMap<>();
		this.seed = seed;
		this.chunk_seed_data = new long[256];
		this.lighting = lighting;
		this.loadRegions = new ArrayList<WorldLoadRegion>();

		relightQueue = new HashSet<>();

		if (serverWorld) {
			RandomXS128 random = new RandomXS128(seed);
			for (int i = 0; i < this.chunk_seed_data.length; i++) {
				this.chunk_seed_data[i] = random.nextLong();
			}
			generator = new WorldGenerator(this);

			this.ticker = new WorldTicker(this, 1);
			this.ticker.start();
		} else {
			this.ticker = null;
			this.generator = null;
			this.saver = null;
		}
	}

	public World(long seed, Lighting lighting, boolean serverWorld, FileHandle saveDirectory) {
		this(seed, lighting, serverWorld);
		setSaveDirectory(saveDirectory);
	}

	public void setSaveDirectory(FileHandle directory) {
		if (isClient())
			return;
		if (saver != null)
			saver.dispose();
		assert directory.isDirectory();
		saver = new WorldSaver(this, directory);
	}

	public boolean isServer() {
		return this.ticker != null;
	}

	public boolean isClient() {
		return this.ticker == null;
	}

	public long getCompoundSeed(long compound) {
		return murmurHash3(compound) ^ seed;
	}

	public long getChunkSeed(ChunkPos pos) {
		int idx = ((pos.x << 3) | (pos.y << 9) | (pos.z << 15)) & (0xA1E20B80 | (pos.x + pos.y + pos.z));
		idx = (int) (Integer.toUnsignedLong(idx) % chunk_seed_data.length);
		long seed = chunk_seed_data[idx] * ((pos.x | 0xD1) + (pos.y | 0xD1) + (pos.z | 0xD1));
		return seed;
	}

	public Chunk createEmptyChunk(ChunkPos pos) {
		return new Chunk(pos.x, pos.y, pos.z, this);
	}

	public Chunk getChunk(int chunkX, int chunkY, int chunkZ) {
		ChunkPos pos = new ChunkPos(chunkX, chunkY, chunkZ);
		Chunk chunk = loaded_chunks.getOrDefault(pos, null);
		if (chunk != null)
			return chunk;

		chunk = createEmptyChunk(pos);

		if (isClient()) {
			addLoadedChunk(chunk);
			return chunk;
		}

		Chunk loadedChunk = saver.loadChunk(chunk);
		synchronized(chunk) {
			if (loadedChunk == null) {
				generateChunk(chunk);
			} else {
				addLoadedChunk(loadedChunk);
				chunk = loadedChunk;
			}
		}
		
		return chunk;
	}

	public Chunk getLoadedChunk(int chunkX, int chunkY, int chunkZ) {
		ChunkPos pos = new ChunkPos(chunkX, chunkY, chunkZ);
		return loaded_chunks.getOrDefault(pos, null);
	}

	public Chunk getChunk(ChunkPos pos) {
		return getChunk(pos.x, pos.y, pos.z);
	}

	public Chunk getLoadedChunk(ChunkPos pos) {
		return getLoadedChunk(pos.x, pos.y, pos.z);
	}

	public synchronized byte getVoxel(int x, int y, int z) {
		tempVoxelPos.set(x, y, z);
		tempVoxelPos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(tempVoxelPos, tempVoxelPos);

		return getChunk(tempChunkPos).getFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z);
	}

	public synchronized short getVoxelState(int x, int y, int z) {
		tempVoxelPos.set(x, y, z);
		tempVoxelPos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(tempVoxelPos, tempVoxelPos);

		return getChunk(tempChunkPos).getStateFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z);
	}

	public synchronized short getVoxelState(VoxelPos pos) {
		return getVoxelState(pos.x, pos.y, pos.z);
	}

	public synchronized byte getVoxelIfLoaded(int x, int y, int z) {
		tempVoxelPos.set(x, y, z);
		tempVoxelPos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(tempVoxelPos, tempVoxelPos);

		Chunk chunk = getLoadedChunk(tempChunkPos);

		if (chunk == null)
			return Voxels.AIR;

		return chunk.getFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z);
	}

	public byte getVoxel(VoxelPos pos) {
		return getVoxel(pos.x, pos.y, pos.z);
	}

	public synchronized void setVoxel(int x, int y, int z, byte voxel) {
		tempVoxelPos.set(x, y, z);
		tempVoxelPos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(tempVoxelPos, tempVoxelPos);

		Chunk modifiedChunk = getChunk(tempChunkPos);

		modifiedChunk.setFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z, voxel);
		saveChunk(modifiedChunk);
		
		for (int X = -1; X <= 1; X++) {
			for (int Y = -1; Y <= 1; Y++) {
				for (int Z = -1; Z <= 1; Z++) {
					if (x == 0 && y == 0 && z == 0)
						continue;
					Chunk chunk = getLoadedChunk(tempChunkPos.x + X, tempChunkPos.y + Y, tempChunkPos.z + Z);
					if (chunk == null)
						continue;
					relightQueue.add(chunk);
				}
			}
		}
		relightQueue.add(modifiedChunk);
	}

	public synchronized void setVoxelState(int x, int y, int z, short state) {
		tempVoxelPos.set(x, y, z);
		tempVoxelPos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(tempVoxelPos, tempVoxelPos);

		Chunk modifiedChunk = getChunk(tempChunkPos);

		modifiedChunk.setStateFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z, state);
		saveChunk(modifiedChunk);

		for (int X = -1; X <= 1; X++) {
			for (int Y = -1; Y <= 1; Y++) {
				for (int Z = -1; Z <= 1; Z++) {
					if (x == 0 && y == 0 && z == 0)
						continue;
					Chunk chunk = getLoadedChunk(tempChunkPos.x + X, tempChunkPos.y + Y, tempChunkPos.z + Z);
					if (chunk == null)
						continue;
					relightQueue.add(chunk);
				}
			}
		}
		relightQueue.add(modifiedChunk);
	}

	public void setVoxel(VoxelPos pos, byte voxel) {
		setVoxel(pos.x, pos.y, pos.z, voxel);
	}

	public void setVoxelState(VoxelPos pos, short state) {
		setVoxelState(pos.x, pos.y, pos.z, state);
	}

	public synchronized void updateLightSync(Chunk chunk) {
		lighting.calculateLight(this, chunk);
		chunk.needsRelighting = false;
	}

	public synchronized short getLight(int x, int y, int z) {
		tempVoxelPos.set(x, y, z);
		tempVoxelPos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(tempVoxelPos, tempVoxelPos);

		Chunk chunk = getChunk(tempChunkPos);
		Chunk topChunk = getLoadedChunk(tempChunkPos.x, tempChunkPos.y + 1, tempChunkPos.z);

		int skyLight = 0;
		if (topChunk == null) {
			skyLight = chunk.getSkyLight(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z);
		}

		return (short) (Lighting.getLight(0, 0, 0, skyLight)
				| (chunk.getLight(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z) & 0xFFFFFF));
	}

	public synchronized short getLightFast(int x, int y, int z) {
		tempVoxelPos.set(x, y, z);
		tempVoxelPos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(tempVoxelPos, tempVoxelPos);

		Chunk chunk = getLoadedChunk(tempChunkPos);
		if (chunk == null)
			return 0;

		return chunk.getLight(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z);
	}

	public synchronized void setLight(int x, int y, int z, short light) {
		tempVoxelPos.set(x, y, z);
		tempVoxelPos.getChunkPos(tempChunkPos);
		tempChunkPos.getRelativeVoxelPos(tempVoxelPos, tempVoxelPos);

		Chunk chunk = getLoadedChunk(tempChunkPos);
		if (chunk == null)
			return;
		chunk.setLight(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z, light);
	}

	public synchronized void calculateLightQueue() {
		for (Chunk chunk : relightQueue) {
			chunk.clearLights();
		}
		for (Chunk chunk : relightQueue) {
			updateLightSync(chunk);
		}
		relightQueue.clear();
	}

	private VoxelTarget _getTargetedVoxel_target;
	private boolean _getTargetedVoxel_intersected;
	private Ray _getTargetedVoxel_ray = new Ray();

	public synchronized VoxelTarget getTargetedVoxel(Camera camera, int maxDistance) {
		_getTargetedVoxel_target = new VoxelTarget(new VoxelPos(0, 0, 0), 0, new Vector3());
		_getTargetedVoxel_intersected = false;

		VoxelIntersect.raycastToVoxelGrid(camera.position, camera.direction, maxDistance, (x, y, z, face) -> {
			byte voxel;
			if ((voxel = getVoxel((int) x, (int) y, (int) z)) != Voxels.AIR) {
				_getTargetedVoxel_target.pos.set((int) x, (int) y, (int) z);
				Chunk chunk = getChunk(_getTargetedVoxel_target.pos.getChunkPos(tempChunkPos));
				tempChunkPos.getRelativeVoxelPos(_getTargetedVoxel_target.pos, tempVoxelPos);

				if (Voxels.getAbstractVoxel(voxel).getVoxelTarget(chunk, tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z,
						chunk.getStateFast(tempVoxelPos.x, tempVoxelPos.y, tempVoxelPos.z), _getTargetedVoxel_target,
						_getTargetedVoxel_ray.set(camera.position, camera.direction))) {
					return _getTargetedVoxel_intersected = true;
				}
			}
			return false;
		});

		return _getTargetedVoxel_intersected ? _getTargetedVoxel_target : null;
	}

	public Set<Entry<ChunkPos, Chunk>> getLoadedChunks() {
		return loaded_chunks.entrySet();
	}

	public int getLoadedChunkCount() {
		return loaded_chunks.size();
	}

	public synchronized void generateChunk(Chunk chunk) {
		if (!isValidChunk(tempChunkPos.set(chunk.chunkX, chunk.chunkY, chunk.chunkZ))) {
			return;
		}

		System.out.println("Generated chunk " + chunk.getChunkPos());
		generator.proceduralGenerateChunk(chunk);
		lighting.calculateLight(this, chunk);
		
		addLoadedChunk(chunk);
	}

	public synchronized void tryUnloadChunks() throws IOException {
		ArrayList<Chunk> unload = new ArrayList<>();
		for (Entry<ChunkPos, Chunk> chunk : getLoadedChunks()) {
			if (!isValidChunk(chunk.getKey()) || WorldLoadRegion.allowsUnload(loadRegions, chunk.getKey())) {
				unload.add(chunk.getValue());
			}
		}
		for (Chunk chunk : unload) {
			unload(chunk);
		}
	}

	public synchronized void unload(Chunk chunk) {
		saveChunk(chunk);
		loaded_chunks.remove(chunk.getChunkPos());
	}

	public boolean isValidChunk(ChunkPos pos) {
		return Math.abs(pos.x) <= MAX_CHUNK_COORD && Math.abs(pos.z) <= MAX_CHUNK_COORD && pos.y >= -2 && pos.y < 20;
	}

	public boolean isValidPosition(VoxelPos pos) {
		return isValidChunk(pos.getChunkPos(tempChunkPos));
	}

	public void saveChunk(Chunk chunk) {
		if (saver != null)
			saver.saveChunk(chunk);
	}

	@Override
	public void dispose() {
		if (ticker != null)
			ticker.stop.set(true);
		if (saver != null)
			saver.dispose();
		loaded_chunks.clear();
		relightQueue.clear();
	}

	public void addLoadedChunk(Chunk sourceChunk) {
		if(ticker != null && ticker.hasServer()) {
			ticker.getServer().getEventBus().dispatchEvent(new ServerChunkLoadedEvent(sourceChunk));
		}
		
		loaded_chunks.put(sourceChunk.getChunkPos(), sourceChunk);
		sourceChunk.needsRelighting = true;
		saveChunk(sourceChunk);
	}

	private long lastSave = 0;
	public synchronized void tick() {
		Runnable task;

		try {
			tryUnloadChunks();
			long now = System.nanoTime();
			if(saver != null && now - lastSave >= 10_000_000_000L) {
				saver.saveRegions();
				lastSave = System.nanoTime();
				System.out.println("Saved world");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		calculateLightQueue();

		while ((task = worldGenTasks.poll()) != null) {
			task.run();
		}

		// System.out.println("TICK");
	}

	public Lighting getLightingEngine() {
		return lighting;
	}
}