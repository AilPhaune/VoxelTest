package fr.ailphaune.voxeltest.multiplayer.server.events;

import fr.ailphaune.voxeltest.data.world.Chunk;
import fr.ailphaune.voxeltest.events.BaseEvent;

public class ServerChunkLoadedEvent extends BaseEvent {

	public final Chunk chunk;
	
	public ServerChunkLoadedEvent(Chunk chunk) {
		super(false, BubbleBehaviour.ALWAYS);
		this.chunk = chunk;
	}
	
}