package io.github.BlockBreaker;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {

  MainScreen mainScreen = new MainScreen();

  @Override
  public void create() {
    setScreen(mainScreen);
  }
}