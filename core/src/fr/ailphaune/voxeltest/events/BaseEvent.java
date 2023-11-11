package fr.ailphaune.voxeltest.events;

public class BaseEvent {
	
	public static enum BubbleBehaviour {
		ALWAYS, WHEN_BUBBLE_DISABLE, WHEN_NOT_CANCELLED;
	}
	
	public final boolean cancelable;
	public final BubbleBehaviour behaviour;
	
	protected boolean canceled = false;
	protected boolean bubbles = true;
	
	public BaseEvent(boolean cancelable) {
		this(cancelable, BubbleBehaviour.ALWAYS);
	}
	
	public BaseEvent(boolean cancelable, BubbleBehaviour behaviour) {
		this.cancelable = cancelable;
		this.behaviour = behaviour;
	}
	
	public void cancel() {
		this.canceled = cancelable;
		if(behaviour == BubbleBehaviour.WHEN_NOT_CANCELLED) {
			this.bubbles = false;
		}
	}
	
	public boolean isCancelled() {
		return this.canceled;
	}
	
	public void stopBubbling() {
		bubbles &= behaviour != BubbleBehaviour.WHEN_BUBBLE_DISABLE;
	}

	public boolean bubbles() {
		return this.bubbles;
	}
}