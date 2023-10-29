package fr.ailphaune.voxeltest.controller;

import com.badlogic.gdx.math.Matrix4;

public class PerspectiveCamera extends com.badlogic.gdx.graphics.PerspectiveCamera {
	
	public final Matrix4 rotation = new Matrix4();

	private final Matrix4 tempMatrix4 = new Matrix4();
	
	public PerspectiveCamera() {
		super();
	}
	
	public PerspectiveCamera(int fovY, int viewportWidth, int viewportHeight) {
		super(fovY, viewportWidth, viewportHeight);
	}

	public void updateFrustum() {
		invProjectionView.set(combined);
		Matrix4.inv(invProjectionView.val);
		frustum.update(invProjectionView);
	}

	@Override
	public void update(boolean updateFrustum) {
		if(rotation == null) {
			return;
		}
		float aspect = viewportWidth / viewportHeight;
		projection.setToProjection(Math.abs(near), Math.abs(far), fieldOfView, aspect);
		view.set(rotation).mul(tempMatrix4.idt().setToTranslation(position).inv());
		combined.set(projection);
		Matrix4.mul(combined.val, view.val);
		
		if (updateFrustum)
			updateFrustum();
	}
}