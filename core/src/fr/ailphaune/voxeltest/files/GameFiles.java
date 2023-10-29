package fr.ailphaune.voxeltest.files;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class GameFiles {

	public static FileHandle getDataFolder() {
		return Gdx.files.local("gameData");
	}

	public static FileHandle getSavesFolder() {
		return getDataFolder().child("saves");
	}
}