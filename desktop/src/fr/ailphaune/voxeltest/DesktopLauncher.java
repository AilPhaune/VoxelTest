package fr.ailphaune.voxeltest;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

// Please note that on macOS your application needs to be started with the -XstartOnFirstThread JVM argument
public class DesktopLauncher {
	
	public static void main(String[] args) {
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		final boolean debugMode = args.length > 0 && "--debug".equals(args[0]);
		
		Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
		config.setForegroundFPS(60);
		config.setTitle("voxel game");
		config.setIdleFPS(10);
		config.useVsync(false);
		// config.setMaximized(true);
		try {
			new Lwjgl3Application(new VoxelTestGame(debugMode), config);
		} catch(Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
	
}