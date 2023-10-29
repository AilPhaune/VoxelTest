package fr.ailphaune.voxeltest.render.outlines;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;

import fr.ailphaune.voxeltest.render.voxel.VoxelShaders;

public class OutlineRenderer implements Disposable {
	
	protected static final Vector3 ZERO = new Vector3(0,0,0);
	
	protected Mesh mesh;
	
	public void render(Camera camera, Color color, float thickness, Vector3 offset, boolean useDepthBuffer) {
		if(mesh == null) return;
		if(offset == null) offset = ZERO;
		
		VoxelShaders.bindOutline(camera.combined);
		VoxelShaders.outlineShader.setUniformf("u_offset", offset);
		VoxelShaders.outlineShader.setUniformf("u_color", color);
		
		Gdx.gl.glLineWidth(thickness);
		if(useDepthBuffer) Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		
		mesh.render(VoxelShaders.outlineShader, GL20.GL_LINES);

		if(useDepthBuffer) Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
	}
	
	public void render(Camera camera, Color color, float thickness) {
		render(camera, color, thickness, ZERO, true);
	}
	
	public void render(Camera camera, float thickness) {
		render(camera, Color.WHITE, thickness, ZERO, true);
	}
	
	public void render(Camera camera) {
		render(camera, Color.WHITE, 1, ZERO, true);
	}
	
	@Override
	public void dispose() {
		if(mesh != null) mesh.dispose();
	}
}