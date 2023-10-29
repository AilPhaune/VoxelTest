package fr.ailphaune.voxeltest.render.voxel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;

public class VoxelShaders {
	
	private static final String projTrans = "u_projTrans";
	
	public static ShaderProgram terrainShader;
	public static ShaderProgram outlineShader;

	public static void bindTerrain(Matrix4 combined) {
		terrainShader.bind();
		terrainShader.setUniformMatrix(projTrans, combined);
	}
	
	public static void bindOutline(Matrix4 combined) {
		outlineShader.bind();
		outlineShader.setUniformMatrix(projTrans, combined);
	}
	
	public static boolean loadShaders() {
		try {
			terrainShader = new ShaderProgram(Gdx.files.internal("voxeltest/shaders/terrain.vert"), Gdx.files.internal("voxeltest/shaders/terrain.frag"));
			outlineShader = new ShaderProgram(Gdx.files.internal("voxeltest/shaders/outline.vert"), Gdx.files.internal("voxeltest/shaders/outline.frag"));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		String log = "Shader log: \n";
		boolean error = false;

		if (!terrainShader.isCompiled()) {
			error = true;
			log += terrainShader.getLog() + "\n";
		}
		if (!outlineShader.isCompiled()) {
			error = true;
			log += outlineShader.getLog() + "\n";
		}
		
		if(error) {
			System.err.println(log);
		}
		
		return !error;
	}
	
}