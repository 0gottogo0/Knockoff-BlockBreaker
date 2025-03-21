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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainScreen implements Screen {

  Texture blockTexture;
  Texture paddleTexture;
  Texture ballTexture;
  Sprite paddle;
  Sprite ball;

  SpriteBatch mainSpriteBatch;
  BitmapFont mainFont;
  SpriteBatch pauseSpriteBatch;
  ShapeRenderer pauseRenderer;
  BitmapFont pauseFont;
  FitViewport viewport;

  Rectangle blockRectangle;
  Rectangle paddleRectangle;
  Rectangle ballRectangle;
  
  SpriteBatch debugSpriteBatch;
  ShapeRenderer debugRenderer;
  BitmapFont debugFont;

  Array<Sprite> blocks;

  float paddleX;
  float ballX;
  float ballY;

  float ballXVelocity;
  boolean ballVelocityDown;

  boolean debugSlowDelta;
  boolean debugFastDelta;

  int score;
  int level;
  int lives;
  boolean pause;
  boolean launchedBall;
  boolean toggleCollision;

  @Override
  public void show() {
    blockTexture = new Texture("Block.png");
    paddleTexture = new Texture("Paddle.png");
    ballTexture = new Texture("Ball.png");

    paddle = new Sprite(paddleTexture);
    ball = new Sprite(ballTexture);
    
    mainSpriteBatch = new SpriteBatch();
    mainFont = new BitmapFont();
    pauseSpriteBatch = new SpriteBatch();
    pauseRenderer = new ShapeRenderer();
    pauseFont = new BitmapFont();
    viewport = new FitViewport(180, 180);

    blockRectangle = new Rectangle();
    paddleRectangle = new Rectangle();
    ballRectangle = new Rectangle();
    
    debugRenderer = new ShapeRenderer();
    debugSpriteBatch = new SpriteBatch();
    debugFont = new BitmapFont();

    blocks = new Array<>();

    // Set ball and paddle to the center of the screen
    paddleX = 74;
    ballX = paddleX + 14;
    ballY = paddleSize + 2;

    ballXVelocity = 0;
    ballVelocityDown = false;
    
    debugSlowDelta = false;
    debugFastDelta = false;

    score = 0;
    level = 0;
    lives = 3;
    pause = true;
    launchedBall = false;
    toggleCollision = true;

    resetBlocks();
  }

  @Override
  public void render(float delta) {
    toggleCollision = true;

    if (debugSlowDelta) {
      delta = delta / 20;
    } else if (debugFastDelta) {
      delta = delta * 10;
    }

    delta = delta * (level + 1);

    input(delta);
    stepPhysiscs(delta);

    ScreenUtils.clear(backroundColorR / 255, backroundColorG / 255, backroundColorB / 255, 1);

    viewport.apply(true);

    mainSpriteBatch.setProjectionMatrix(viewport.getCamera().combined);
    mainSpriteBatch.begin();
    mainFont.getData().setScale(0.5f);
    mainFont.draw(mainSpriteBatch, "Score: " + score, 3, 176);
    mainSpriteBatch.draw(paddle, paddleX, 1, paddleSize * 4, paddleSize);
    mainSpriteBatch.draw(ball, ballX, ballY, ballSize, ballSize);

    // This just stucks
    if (lives >= 1) {
      mainSpriteBatch.draw(ball, 173, 173, ballSize, ballSize);
    }

    if (lives >= 2) {
      mainSpriteBatch.draw(ball, 166, 173, ballSize, ballSize);
    }

    if (lives >= 3) {
      mainSpriteBatch.draw(ball, 159, 173, ballSize, ballSize);
    }

    for (Sprite blockSprite : blocks) {
      blockSprite.draw(mainSpriteBatch);
    }

    mainSpriteBatch.end();

    // Debug
    if (debugEnablePrinting) {
      debugSpriteBatch.setProjectionMatrix(viewport.getCamera().combined);
      debugSpriteBatch.begin();
      debugFont.getData().setScale(0.4f);
      debugFont.draw(debugSpriteBatch, "Paddle X: " + paddleX, paddleX, 14);
      debugFont.draw(debugSpriteBatch, "Ball X: " + ballX, 1, 6);
      debugFont.draw(debugSpriteBatch, "Ball Y: " + ballY, 1, 12);
      debugFont.draw(debugSpriteBatch, "Ball X Vel: " + ballXVelocity, 1, 18);
      debugFont.draw(debugSpriteBatch, "Ball Y Travel: " + ballVelocityDown, 1, 24);
      debugFont.draw(debugSpriteBatch, "Lives: " + lives, 1, 30);
      debugFont.draw(debugSpriteBatch, "Level: " + level, 1, 36);
      debugFont.draw(debugSpriteBatch, "Array Size: " + blocks.size, 1, 42);
      debugFont.draw(debugSpriteBatch, "Collisiont: " + toggleCollision, 1, 48);
      debugFont.draw(debugSpriteBatch, "Delta: " + delta, 1, 54);
      debugSpriteBatch.end();
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
      pauseFont.getData().setScale(0.4f);
      pauseFont.draw(pauseSpriteBatch, "R to Reset", 75, 80);
      pauseSpriteBatch.end();
    }

    if (lives <= 0 && !launchedBall) {
      resetGame();
    }

    if (blocks.size <= 0) {
      resetBlocks();
      ++level;
      score = score + 125;
    }
  }
  
  public void input(float delta) {
    if (pause) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
        resume();
      } else if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
        resetGame();
      }
      return;
    }
 
    if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
      paddleX += paddleSpeed * delta;
    } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
      paddleX += -paddleSpeed * delta;
    }

    if (!launchedBall) {
      if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
        launchBall();
      }
    }

    if (paddleX < 1) {
      paddleX = 1;
    } else if (paddleX > 147) {
      paddleX = 147;
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
      pause();
    }

    if (!debugEnableKeys) {
      return;
    }

    if (Gdx.input.isKeyPressed(Input.Keys.T)) {
      debugSlowDelta = true;
      debugFastDelta = false;
    } else if (Gdx.input.isKeyPressed(Input.Keys.Y)) {
      debugSlowDelta = false;
      debugFastDelta = true;
    } else {
      debugSlowDelta = false;
      debugFastDelta = false;
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.U)) {
      ++level;
    }

    if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
      --level;
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
    
    paddleRectangle.set(paddleX, 1, paddleSize * 4, paddleSize);
    ballRectangle.set(ballX, ballY, ballSize, ballSize);

    if (!launchedBall) {
      ballX = paddleX + 14;
      ballY = paddleSize + 2;

      ballXVelocity = ballSpeed / 2;
      ballVelocityDown = false;

      return;
    }

    collisionDetection();

    Math.clamp(ballXVelocity, (-ballSpeed) + 15, ballSpeed - 15);
    ballX += ballXVelocity * delta;

    if (ballVelocityDown) {
      ballY += -(ballSpeed - Math.abs(ballXVelocity)) * delta;
    } else {
      ballY += (ballSpeed - Math.abs(ballXVelocity)) * delta;
    }
  }

  public void collisionDetection() {
    if (ballX > 175) {
      ballXVelocity = -ballXVelocity;
      ballX -= 1;
    }

    if (ballX < 1) {
      ballXVelocity = -ballXVelocity;
      ballX += 1;
    }

    if (ballY > 175) {
      ballVelocityDown = true;
      ballY -= 1;
    }
    
    if (ballY < 1) {
      launchedBall = false;
    }

    for (int i = blocks.size - 1; i >= 0; i--) {
      Sprite blockSprite = blocks.get(i);
      blockRectangle.set(blockSprite.getX() - 0.5f, blockSprite.getY() - 0.5f, (blockSize * 2) + 1, blockSize + 1);

      if (ballRectangle.overlaps(blockRectangle)) {
        blockCollision(blockSprite);
        blocks.removeIndex(i);
        score = score + 50;
        toggleCollision = false;
      }
    }

    if (paddleRectangle.overlaps(ballRectangle)) {
      ballVelocityDown = false;
      ballXVelocity = ((ballX + blockSize / 2) - (paddleX + (paddleSize * 2))) * (level + 1);
    }
  }

  public void blockCollision(Sprite block) {

    if (!toggleCollision) {
      return;
    }

    toggleCollision = false;

    // Calculate this with slight room for error
    if ((ballX + ballSize) - block.getX() < ballSize || ballX - (block.getX() + (blockSize * 2)) > -ballSize) {
      ballXVelocity = -ballXVelocity;
    } else if (ballRectangle.getY() < block.getY() + (blockSize / 2)) {
      ballVelocityDown = true;
    } else {
      ballVelocityDown = false;
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
    --lives;

    if (MathUtils.randomBoolean()) {
      ballXVelocity = -ballXVelocity;
    }
  }

  public void resetGame() {
    launchedBall = false;
    score = 0;
    level = 0;
    lives = 3;
    paddleX = 74;
    resetBlocks();
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

  @Override
  public void dispose() {
    blockTexture.dispose();
    paddleTexture.dispose();
    ballTexture.dispose();
    
    mainSpriteBatch.dispose();
    pauseSpriteBatch.dispose();
    pauseRenderer.dispose();
    pauseFont.dispose();

    debugSpriteBatch.dispose();
    debugRenderer.dispose();
    debugFont.dispose();
  }
}