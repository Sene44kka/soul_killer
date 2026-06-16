package ru.skripov;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import ru.skripov.screens.LoadingScreen;
import ru.skripov.screens.RoomScreen;

public class Main extends Game {
    private RoomScreen activeRoomScreen; // Храним ссылку на активную игру

    @Override
    public void create() {
        this.setScreen(new LoadingScreen(this));
    }

    public void setActiveRoomScreen(RoomScreen screen) {
        this.activeRoomScreen = screen;
    }

    public RoomScreen getActiveRoomScreen() {
        return activeRoomScreen;
    }

    public boolean hasActiveGame() {
        return activeRoomScreen != null && !activeRoomScreen.isGameOver();
    }
}
