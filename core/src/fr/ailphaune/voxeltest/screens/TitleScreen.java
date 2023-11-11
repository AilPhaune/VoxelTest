package fr.ailphaune.voxeltest.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import fr.ailphaune.voxeltest.VoxelTestGame;
import fr.ailphaune.voxeltest.data.world.World;
import fr.ailphaune.voxeltest.files.GameFiles;
import fr.ailphaune.voxeltest.light.Lighting;
import fr.ailphaune.voxeltest.multiplayer.server.Server;

public class TitleScreen extends ScreenAdapter {

	public TextButton singlePlayer;
	public TextButton multiPlayer;

	public VerticalGroup buttonList;
	
	public TextButtonStyle tbStyle;
	
	public Stage stage;
	
	public TitleScreen() {
		stage = new Stage(new ScreenViewport());

		buttonList = new VerticalGroup();
		buttonList.setFillParent(true);
		buttonList.space(20);
		buttonList.center();
		
		tbStyle = new TextButtonStyle();
		tbStyle.font = new BitmapFont();
		tbStyle.fontColor = Color.WHITE;
		
		singlePlayer = new TextButton("Singleplayer", tbStyle);
		singlePlayer.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(singlePlayer.isChecked()) event.cancel();
				try {
					Server server = VoxelTestGame.getInstance().createServer(new World(115, new Lighting(), true, GameFiles.getSavesFolder().child("world1")), 12345);
					server.start();
					server.open();
					Screens.GAME_SCREEN.client = VoxelTestGame.getInstance().createConnectedLocalClient();
					Screens.setScreen(Screens.GAME_SCREEN);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		multiPlayer = new TextButton("Multiplayer", tbStyle);
		multiPlayer.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(multiPlayer.isChecked()) event.cancel();
				Screens.setScreen(Screens.MULTIPLAYER_SELECT_SCREEN);
			}
		});

		buttonList.addActor(singlePlayer);
		buttonList.addActor(multiPlayer);
		
		stage.addActor(buttonList);
		
		if(VoxelTestGame.getInstance().IS_DEBUG) {
			buttonList.setDebug(true, true);
		}
	}
	
	@Override
	public void render(float deltaTime) {
		ScreenUtils.clear(0.4f, 0.4f, 0.4f, 1f, true);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		
		stage.act(deltaTime);
		
		stage.draw();
	}
	
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void show() {
		super.show();
		Gdx.input.setInputProcessor(stage);
	}
	
	@Override
	public void dispose() {
		stage.dispose();
		tbStyle.font.dispose();
	}
}