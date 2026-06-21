package ru.skripov.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import ru.skripov.Main;
import ru.skripov.ui.GameOverUI;

public class GameOverScreen extends ScreenAdapter {
    private final Main game;
    private GameOverUI gameOverUI;

    public GameOverScreen(Main game, int score) {
        this.game = game;

        this.gameOverUI = new GameOverUI(score, new GameOverUI.GameOverListener() {
            @Override
            public void onNewGame() {
                System.out.println("=== NEW GAME FROM GAME OVER ===");
                game.setScreen(new RoomScreen(game));
            }

            @Override
            public void onMainMenu() {
                System.out.println("=== RETURN TO MENU ===");
                game.setScreen(new MenuScreen(game));
            }
        });
    }

    @Override
    public void show() {
        gameOverUI.show();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.03f, 0.03f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        gameOverUI.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        gameOverUI.resize(width, height);
    }

    @Override
    public void dispose() {
        gameOverUI.dispose();
    }

    @Override
    public void hide() {
        gameOverUI.hide();
    }
}
