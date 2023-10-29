package fr.ailphaune.voxeltest.events;

public class BaseEvent {
	
	public final boolean cancelable;
	
	protected boolean canceled;
	
	public BaseEvent(boolean cancelable) {
		this.cancelable = cancelable;
	}
	
	public void cancel() {
		this.canceled = cancelable;
	}
	
	public boolean isCancelled() {
		return this.canceled;
	}

	public boolean bubbles() {
		return !this.canceled;
	}
}