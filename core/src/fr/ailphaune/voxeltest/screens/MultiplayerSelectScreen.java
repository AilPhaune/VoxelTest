package fr.ailphaune.voxeltest.screens;

import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import fr.ailphaune.voxeltest.VoxelTestGame;
import fr.ailphaune.voxeltest.multiplayer.Client;

public class MultiplayerSelectScreen extends ScreenAdapter {
	
	public TextButton btnCancel;
	public TextButton btnConnect;

	public VerticalGroup hostAndButtons;
	public HorizontalGroup buttonList;

	public TextField hostInput;

	public TextButtonStyle tbStyle;
	public TextFieldStyle hiStyle;
	
	public Stage stage;
	
	public MultiplayerSelectScreen() {
		stage = new Stage(new ScreenViewport());

		hostAndButtons = new VerticalGroup();
		hostAndButtons.setFillParent(true);
		hostAndButtons.space(20);
		hostAndButtons.center();
		
		buttonList = new HorizontalGroup();
		buttonList.space(20);
		buttonList.center();
		
		hiStyle = new TextFieldStyle();
		hiStyle.font = new BitmapFont();
		hiStyle.fontColor = Color.WHITE;
		
		hostInput = new TextField("localhost:12345", hiStyle);
		
		tbStyle = new TextButtonStyle();
		tbStyle.font = new BitmapFont();
		tbStyle.fontColor = Color.WHITE;
		
		btnCancel = new TextButton("Cancel", tbStyle);
		btnCancel.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(btnCancel.isChecked()) event.cancel();
				btnCancel.setChecked(false);
				Screens.setScreen(Screens.TITLE_SCREEN);
			}
		});
		
		btnConnect = new TextButton("Connect", tbStyle);
		btnConnect.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				if(btnConnect.isChecked()) event.cancel();
				try {
					String host = hostInput.getText().split(":")[0];
					String port = hostInput.getText().split(":")[1];
					
					Client client = VoxelTestGame.getInstance().createClient(host, Integer.parseInt(port));
					client.connect(VoxelTestGame.getInstance().getUsername());
					
					Screens.GAME_SCREEN.client = client;
					Screens.setScreen(Screens.GAME_SCREEN);
				} catch(IOException e) {
					e.printStackTrace();
				} catch(Throwable t) {}
			}
		});

		hostAndButtons.addActor(hostInput);
		hostAndButtons.addActor(buttonList);
		
		buttonList.addActor(btnCancel);
		buttonList.addActor(btnConnect);
		
		stage.addActor(hostAndButtons);
		
		if(VoxelTestGame.getInstance().IS_DEBUG) {
			hostAndButtons.setDebug(true, true);
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