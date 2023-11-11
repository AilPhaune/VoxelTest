package fr.ailphaune.voxeltest.saves;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

import fr.ailphaune.voxeltest.data.collections.SynchronizedHashSet;
import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.data.world.World;

public class WorldSaver implements Disposable {

	public static final String REGIONS_DIR_NAME = "regions";

	public final World world;
	public final FileHandle directory;

	private final FileHandle regionsHandle;

	protected ConcurrentHashMap<RegionPos, WorldRegion> regions;
	protected SynchronizedHashSet<RegionPos> saveQueue;

	private final RegionPos tempRegionPos = new RegionPos();

	protected final AtomicReference<Throwable> error = new AtomicReference<>(null);

	public WorldSaver(final World world, final FileHandle directory) {
		this.world = world;
		this.directory = directory;
		this.regions = new ConcurrentHashMap<>();
		this.saveQueue = new SynchronizedHashSet<>();

		regionsHandle = directory.child(REGIONS_DIR_NAME);
	}

	@Override
	public void dispose() {
		clear();
	}

	public WorldRegion getRegion(RegionPos pos) {
		WorldRegion region = regions.getOrDefault(pos, null);
		if (region != null) {
			synchronized(region) {
				return region;
			}
		}
		region = new WorldRegion(pos, world);
		synchronized(region) {
			regions.put(region.pos, region);
			loadRegion(region);
			return region;
		}
	}

	public void saveChunk(Chunk chunk) {
		tempRegionPos.set(chunk.getChunkPos());
		WorldRegion region = getRegion(tempRegionPos);
		synchronized(region) {
			if (!region.loaded)
				return;
			region.putChunk(chunk);
			saveRegion(region);
		}
	}

	public void saveRegion(WorldRegion region) {
		synchronized(saveQueue) {
			regions.put(region.pos, region);
			saveQueue.add(region.pos);
		}
	}

	public void clear() {
		synchronized(saveQueue) {
			regions.clear();
			saveQueue.clear();
		}
	}

	public FileHandle getRegionsDirectoryHandle() {
		return regionsHandle;
	}

	public FileHandle getRegionFile(RegionPos regionPos) {
		String name = regionPos.x + "_" + regionPos.y + "_" + regionPos.z + ".region.gz";
		return getRegionsDirectoryHandle().child(name);
	}

	public void saveRegions() throws IOException {
		synchronized(saveQueue) {
			for (RegionPos pos : saveQueue) {
				FileHandle handle = getRegionFile(pos);
				OutputStream os = handle.write(false);
				GZIPOutputStream gzos = new GZIPOutputStream(os);
				WorldRegion region = regions.get(pos);
				synchronized(region) {
					region.writeTo(gzos);
				}
				gzos.close();
			}
			this.saveQueue.clear();
		}
	}

	public Chunk loadChunk(Chunk chunk) {
		WorldRegion region = getRegion(new RegionPos(chunk.getChunkPos()));
		synchronized(region) {
			return region.get(chunk);
		}
	}

	public WorldRegion loadRegion(WorldRegion region) {
		synchronized(region) {
			try {
				FileHandle handle = getRegionFile(region.pos);
				if (!handle.exists()) {
					region.loaded = true;
					Arrays.fill(region.chunks, null);
					return region;
				}
				InputStream is = handle.read();
				GZIPInputStream gzis = new GZIPInputStream(is);
				region.readFrom(gzis);
				gzis.close();
				region.loaded = true;
			} catch (IOException e) {
				region.loaded = true;
				exception(e);
			}
			return region;
		}
	}

	public void exception(Throwable t) {
		t.printStackTrace();
		this.error.set(t);
	}

	public void checkException() throws Throwable {
		Throwable t = this.error.get();
		if (t != null)
			throw t;
	}
}