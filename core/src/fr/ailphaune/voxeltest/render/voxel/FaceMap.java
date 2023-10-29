package fr.ailphaune.voxeltest.render.voxel;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import com.badlogic.gdx.utils.Array;

import fr.ailphaune.voxeltest.data.world.Chunk;

public class FaceMap {
	
	private HashMap<Integer, Array<Face>> map;
	private Chunk chunk;
	
	public FaceMap(Chunk chunk) {
		this.map = new HashMap<>();
		this.chunk = chunk;
	}
	
	public Set<Entry<Integer, Array<Face>>> getEntries() {
		return map.entrySet();
	}
	
	public Array<Face> getFaces(int faceId) {
		Array<Face> faces = map.getOrDefault(faceId, null);
		if(faces == null) {
			faces = new Array<Face>();
			map.put(faceId, faces);
		}
		return faces;
	}
	
	public boolean addFace(int faceId, Face face, VoxelRenderer renderer) {
		Array<Face> faces = getFaces(faceId);
		boolean added = false;
		if(faces.size > 0) {
			int i = faces.size - 1;
			Face lastFace = faces.get(i);
			if(renderer.combineFaces(faceId, lastFace, face, chunk)) {
				added = true;
			}
		}
		if(!added) {
			faces.add(face);
			return true;
		}
		return false;
	}
}