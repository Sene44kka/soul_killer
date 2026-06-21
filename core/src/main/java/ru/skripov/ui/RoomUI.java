package ru.skripov.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class RoomUI {
    private Stage stage;

    public RoomUI() {
        stage = new Stage();
    }

    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    public void hide() {
        Gdx.input.setInputProcessor(stage);
    }
}
