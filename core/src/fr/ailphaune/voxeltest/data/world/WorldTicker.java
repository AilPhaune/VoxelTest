package fr.ailphaune.voxeltest.data.world;

import java.util.concurrent.atomic.AtomicBoolean;

public class WorldTicker extends Thread {
	
	public int targetTPS;
	public final World world;
	
	public final AtomicBoolean stop;
	
	private long nextTickTime;
	
	public WorldTicker(World world, int targetTPS) {
		super("WorldTicker");
		this.world = world;
		this.targetTPS = targetTPS;
		this.stop = new AtomicBoolean(false);
	}
	
	@Override
	public void run() {
		nextTickTime = 0;
		while(!stop.get()) {
			if(System.nanoTime() < nextTickTime) {
				long waitNanos = nextTickTime - System.nanoTime();
				try {
					Thread.sleep(waitNanos / 1_000_000L);
				} catch (InterruptedException e) {
					e.printStackTrace();
					continue;
				}
			}
			nextTickTime = System.nanoTime() + (1_000_000_000L / targetTPS);
			world.tick();
		}
	}
}