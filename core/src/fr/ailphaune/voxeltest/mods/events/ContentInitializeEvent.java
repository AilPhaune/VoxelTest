package fr.ailphaune.voxeltest.mods.events;

import fr.ailphaune.voxeltest.events.BaseEvent;

public final class ContentInitializeEvent extends BaseEvent {

	public ContentInitializeEvent() {
		super(false);
	}

	public static class PostContentInitializeEvent extends BaseEvent {
		
		public PostContentInitializeEvent() {
			super(false);
		}
		
	}

	public static class GameDisposedEvent extends BaseEvent {
		
		public GameDisposedEvent() {
			super(false);
		}
		
	}
}