package ru.skripov.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;

public class CommonScreen extends ScreenAdapter {
    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }
}
