package fr.ailphaune.voxeltest.data.world;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import fr.ailphaune.voxeltest.multiplayer.server.Server;

public class WorldTicker extends Thread {
	
	public int targetTPS;
	public final World world;
	
	public final AtomicBoolean stop;
	
	private long nextTickTime;
	
	private long tickCount = 0;
	
	private Server server;
	
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
			if(hasServer()) server.tick();
			
			tickCount++;
		}
	}
	
	public long getTickCount() {
		return tickCount;
	}
	
	public void setTickCount(long newTicks) {
		tickCount = newTicks;
	}
	
	public long getTickAmountForNanos(long nanos) {
		return (nanos * targetTPS) / 1_000_000_000L;
	}

	public long getTickAmountForDuration(long amount, TimeUnit unit) {
		return getTickAmountForNanos(TimeUnit.NANOSECONDS.convert(amount, unit));
	}

	public void useServer(Server server) {
		if(this.server != null) throw new IllegalStateException("WorldTicker already associated to a Server");
		this.server = server;
	}
	
	public boolean hasServer() {
		return this.server != null;
	}
	
	public Server getServer() {
		return server;
	}
}