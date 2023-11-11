package fr.ailphaune.voxeltest.threading;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.ailphaune.voxeltest.events.EventBus;
import fr.ailphaune.voxeltest.events.SubscribeEvent;
import fr.ailphaune.voxeltest.mods.events.ContentInitializeEvent.GameDisposedEvent;

public class ThreadPools {

	public static final int CLIENT_RENDERING_THREADS = 5;
	public static final ExecutorService CLIENT_RENDERING = Executors.newFixedThreadPool(CLIENT_RENDERING_THREADS);
	
	@SubscribeEvent
	public static void onGameDisposed(EventBus bus, GameDisposedEvent event) {
		CLIENT_RENDERING.shutdownNow();
	}
}