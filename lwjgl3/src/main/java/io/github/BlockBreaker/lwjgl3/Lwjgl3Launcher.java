package io.github.BlockBreaker.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import io.github.BlockBreaker.Main;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Knockoff BlockBreaker");
        configuration.useVsync(true);
        configuration.setResizable(false);
        if (io.github.BlockBreaker.Constants.Debuging.debugEnableFramerateSlowdown) {configuration.setForegroundFPS(15);}
        else {configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);}
        configuration.setWindowedMode(1080, 1080);
        configuration.setWindowIcon("BallSmall.png");
        return configuration;
    }
}