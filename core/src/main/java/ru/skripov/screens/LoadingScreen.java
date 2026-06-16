package ru.skripov.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import ru.skripov.Main;

public class LoadingScreen extends ScreenAdapter {
    private final Main game;

    public LoadingScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        System.out.println("=== LOADING... ===");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.setScreen(new MenuScreen(game));  // ← Теперь на меню
    }
}
