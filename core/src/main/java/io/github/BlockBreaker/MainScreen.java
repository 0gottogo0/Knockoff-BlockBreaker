package io.github.BlockBreaker;

import static io.github.BlockBreaker.Constants.RGB.*;
import static io.github.BlockBreaker.Constants.Speeds.*;
import static io.github.BlockBreaker.Constants.Sizes.*;
import static io.github.BlockBreaker.Constants.Debuging.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainScreen implements Screen {

  Texture blockTexture;
  Texture paddleTexture;
  Texture ballTexture;
  Sprite paddle;
  Sprite ball;

  World world;
  SpriteBatch mainSpriteBatch;
  SpriteBatch pauseSpriteBatch;
  ShapeRenderer pauseRenderer;
  BitmapFont pauseFont;
  FitViewport viewport;
  
  ShapeRenderer debugRenderer;
  SpriteBatch debugSpriteBatch;
  BitmapFont debugFont;

  Array<Sprite> blocks;

  float paddleX;
  float ballX;
  float ballY;

  float ballXVelocity;
  float ballYVelocity;

  boolean pause;
  boolean launchedBall;

  @Override
  public void show() {
    blockTexture = new Texture("Block.png");
    paddleTexture = new Texture("Paddle.png");
    ballTexture = new Texture("Ball.png");

    paddle = new Sprite(paddleTexture);
    ball = new Sprite(ballTexture);
    
    mainSpriteBatch = new SpriteBatch();
    pauseSpriteBatch = new SpriteBatch();
    pauseRenderer = new ShapeRenderer();
    pauseFont = new BitmapFont();
    viewport = new FitViewport(180, 180);
    
    debugRenderer = new ShapeRenderer();
    debugSpriteBatch = new SpriteBatch();
    debugFont = new BitmapFont();

    blocks = new Array<>();

    paddleX = 74;
    ballX = paddleX + 14;
    ballY = paddleSize + 2;

    ballXVelocity = ballSpeed;
    ballYVelocity = ballSpeed;

    pause = true;
    launchedBall = false;

    resetBlocks();
  }

  @Override
  public void render(float delta) {
    input(delta);
    stepPhysiscs(delta);

    ScreenUtils.clear(BackroundColorR / 255, BackroundColorG / 255, BackroundColorB / 255, 1);

    viewport.apply(true);
    mainSpriteBatch.setProjectionMatrix(viewport.getCamera().combined);
    mainSpriteBatch.begin();
    mainSpriteBatch.draw(paddle, paddleX, 1, paddleSize * 4, paddleSize);
    mainSpriteBatch.draw(ball, ballX, ballY, ballSize, ballSize);

    for (Sprite blockSprite : blocks) {
      blockSprite.draw(mainSpriteBatch);
    }

    mainSpriteBatch.end();

    // Debug
    if (debugEnable) {
      if (debugEnablePrinting) {
        debugSpriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        debugSpriteBatch.begin();
        debugFont.getData().setScale(0.4f);
        debugFont.draw(debugSpriteBatch, "Paddle X: " + paddleX, paddleX, 14);
        debugFont.draw(debugSpriteBatch, "Ball X: " + ballX, 1, 6);
        debugFont.draw(debugSpriteBatch, "Ball X Vel: " + ballXVelocity, 1, 12);
        debugFont.draw(debugSpriteBatch, "Ball Y VeL: " + ballYVelocity, 1, 18);
        debugSpriteBatch.end();
      }
    }

    if (pause) {
      pauseRenderer.begin(ShapeRenderer.ShapeType.Filled);
      pauseRenderer.setColor(0, 0, 0, 1);
      pauseRenderer.rect(0, 0, viewport.getScreenWidth(), viewport.getScreenHeight());
      pauseRenderer.end();
      pauseSpriteBatch.setProjectionMatrix(viewport.getCamera().combined);
      pauseSpriteBatch.begin();
      pauseFont.getData().setScale(1.2f);
      pauseFont.draw(pauseSpriteBatch, "Paused", 60, 100);
      pauseSpriteBatch.end();
    }
  }
  
  public void input(float delta) {
    if (pause) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        resume();
      }
      
      return;
    }

    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
      paddleX += paddleSpeed * delta;
    } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
      paddleX += -paddleSpeed * delta;
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
      launchBall();
    }

    if (paddleX < 1) {
      paddleX = 1;
    } else if (paddleX > 147) {
      paddleX = 147;
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      pause();
    }

  }

  @Override
  public void resize(int width, int height) {
    viewport.update(width, height, true);
  }

  public void stepPhysiscs(float delta) {
    if (pause) {
      return;
    }

    if (!launchedBall) {
      ballX = paddleX + 14;
      ballY = paddleSize + 2;

      ballXVelocity = ballSpeed;
      ballYVelocity = ballSpeed;  

      return;
    }

    collisionDetection();

    // TODO: Add function to make ball stay at constant speed
    ballX += ballXVelocity * delta;
    ballY += ballYVelocity * delta;
  }

  public void collisionDetection() {
    if (ballX > 177) {
      ballXVelocity = -ballXVelocity;
      ballX -= 1;
    }

    if (ballX < 1) {
      ballXVelocity = -ballXVelocity;
      ballX += 1;
    }

    if (ballY > 177) {
      ballYVelocity = -ballYVelocity;
      ballY -= 1;
    }
    
    if (ballY < 0) {
      ballYVelocity = -ballYVelocity;
      launchedBall = false;
    }

    // TODO: Impliment some other function, I beleve an overlap function or somthing exists
    if (ballX >= (paddleX - 1) && ballX <= (paddleX + 1) + (paddleSize * 4) && ballY < (paddleSize + 1)) {
      ballYVelocity = -ballYVelocity;
      float offset = (ballX - (paddleX + (paddleSize * 2))) * 10;
      ballXVelocity += offset;
      offset = 0;
    }
  }

  public void resetBlocks() {
    for (int i = 0; i <= 31; i++) {
      Sprite blockSprite = new Sprite(blockTexture);
      blockSprite.setSize(blockSize * 2, blockSize);
      if (i < 8) {
        blockSprite.setX(((blockSize * 2) * i) + i + 23);
        blockSprite.setY(124);
      } else if (i >= 8 && i < 16) {
        blockSprite.setX(((blockSize * 2) * (i - 8)) + (i - 8) + 23);
        blockSprite.setY(blockSize + 125);
      } else if (i >= 16 && i < 24) {
        blockSprite.setX(((blockSize * 2) * (i - 16)) + (i - 16) + 23);
        blockSprite.setY((blockSize * 2) + 126);
      } else if (i >= 24 && i < 32) {
        blockSprite.setX(((blockSize * 2) * (i - 24)) + (i - 24) + 23);
        blockSprite.setY((blockSize * 3) + 127);
      }
      blocks.add(blockSprite);
    }
  }

  public void launchBall() {
    launchedBall = true;

    if (MathUtils.randomBoolean()) {
      ballXVelocity = -ballXVelocity;
    }
  }

  @Override
  public void pause() {
    pause = true;
  }

  @Override
  public void resume() {
    pause = false;
  }

  @Override
  public void hide() {}

  // TODO: Use dispose function for memory cleanup
  @Override
  public void dispose() {}
}