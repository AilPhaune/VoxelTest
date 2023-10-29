package fr.ailphaune.voxeltest.screens;

import com.badlogic.gdx.Screen;

import fr.ailphaune.voxeltest.VoxelTestGame;

public class Screens {
	
	public static GameScreen GAME_SCREEN;
	public static TitleScreen TITLE_SCREEN;
	public static MultiplayerSelectScreen MULTIPLAYER_SELECT_SCREEN;
	
	public static void register() {
		GAME_SCREEN = new GameScreen();
		TITLE_SCREEN = new TitleScreen();
		MULTIPLAYER_SELECT_SCREEN = new MultiplayerSelectScreen();
	}

	public static void setScreen(Screen screen) {
		VoxelTestGame.getInstance().setScreen(screen);;
	}
}