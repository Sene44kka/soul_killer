package ru.skripov.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import ru.skripov.Main;
import ru.skripov.ui.MenuUI;

public class MenuScreen extends ScreenAdapter {
    private final Main game;
    private final MenuUI menuUI;

    public MenuScreen(Main game) {
        this.game = game;

        menuUI = new MenuUI(new MenuUI.MenuListener() {
            @Override
            public void onNewGame() {
                System.out.println("=== NEW GAME ===");
                game.setScreen(new RoomScreen(game));
            }

            @Override
            public void onContinue() {
                System.out.println("=== CONTINUE ===");
                if (game.hasActiveGame()) {
                    game.setScreen(game.getActiveRoomScreen());
                }
            }

            @Override
            public void onRecords() {
                System.out.println("Records not available yet");
            }

            @Override
            public void onExit() {
                System.out.println("=== EXIT ===");
                Gdx.app.exit();
            }
        });

        // Активируем Continue если есть активная игра
        menuUI.setContinueEnabled(game.hasActiveGame());
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.03f, 0.03f, 0.06f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        menuUI.render(delta);
    }

    @Override
    public void resize(int width, int height) {
        menuUI.resize(width, height);
    }

    @Override
    public void dispose() {
        menuUI.dispose();
    }
}
