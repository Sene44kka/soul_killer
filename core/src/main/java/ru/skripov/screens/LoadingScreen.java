package ru.skripov.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import ru.skripov.Main;
import ru.skripov.ui.LoadingUI;

public class LoadingScreen extends ScreenAdapter {
    LoadingUI loadingUI;

    private final Main game;

    private boolean loaded = false;

    public LoadingScreen(Main game) {
        this.game = game;
        loadingUI = new LoadingUI();
    }

    @Override
    public void show() {
        loadingUI.show();
        System.out.println("=== LOADING... ===");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!loaded) {
            loaded = true;
            game.setScreen(new MenuScreen(game));
        }
    }

    @Override
    public void hide() {
        loadingUI.hide();
    }
}
