package ru.skripov.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class LoadingUI {
    private Stage stage;

    public LoadingUI() {
        stage = new Stage();
    }

    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    public void hide() {
        Gdx.input.setInputProcessor(stage);
    }
}
