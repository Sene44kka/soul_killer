package ru.skripov.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class GameOverUI {
    private Stage stage;
    private Skin skin;
    private Label titleLabel;
    private Label scoreLabel;
    private Label soulsLabel;
    private TextButton newGameButton;
    private TextButton mainMenuButton;

    private GameOverListener listener;

    public interface GameOverListener {
        void onNewGame();
        void onMainMenu();
    }

    public GameOverUI(int score, GameOverListener listener) {
        this.listener = listener;
        stage = new Stage();
        Gdx.input.setInputProcessor(stage);

        createSkin();
        createUI(score);
    }

    private void createSkin() {
        skin = new Skin();

        // Шрифты
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);

        BitmapFont scoreFont = new BitmapFont();
        scoreFont.getData().setScale(3f);

        BitmapFont buttonFont = new BitmapFont();
        buttonFont.getData().setScale(2f);

        BitmapFont smallFont = new BitmapFont();
        smallFont.getData().setScale(1.5f);

        // Стиль кнопок
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = buttonFont;
        buttonStyle.fontColor = new Color(0.7f, 0.8f, 1f, 1f);
        buttonStyle.overFontColor = new Color(1f, 0.9f, 0.3f, 1f);
        buttonStyle.downFontColor = new Color(1f, 0.5f, 0f, 1f);
        buttonStyle.up = createBackground(new Color(0.05f, 0.05f, 0.1f, 0.6f));
        buttonStyle.over = createBackground(new Color(0.1f, 0.15f, 0.3f, 0.8f));
        buttonStyle.down = createBackground(new Color(0.05f, 0.08f, 0.15f, 0.9f));
        skin.add("default", buttonStyle);

        // Стиль заголовка
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = new Color(0.9f, 0.2f, 0.2f, 1f); // Красный
        skin.add("title", titleStyle);

        // Стиль очков
        Label.LabelStyle scoreStyle = new Label.LabelStyle();
        scoreStyle.font = scoreFont;
        scoreStyle.fontColor = new Color(1f, 0.9f, 0.3f, 1f); // Золотой
        skin.add("score", scoreStyle);

        // Стиль обычного текста
        Label.LabelStyle defaultStyle = new Label.LabelStyle();
        defaultStyle.font = smallFont;
        defaultStyle.fontColor = new Color(0.7f, 0.8f, 1f, 1f);
        skin.add("default", defaultStyle);
    }

    private TextureRegionDrawable createBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }

    private void createUI(int score) {
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Заголовок
        titleLabel = new Label("SOUL SHATTERED", skin, "title");
        table.add(titleLabel).padBottom(40).row();

        // Текст "Souls collected"
        soulsLabel = new Label("Souls collected:", skin, "default");
        table.add(soulsLabel).padBottom(10).row();

        // Очки
        scoreLabel = new Label(String.valueOf(score), skin, "score");
        table.add(scoreLabel).padBottom(50).row();

        // Кнопка New Game
        newGameButton = new TextButton("NEW GAME", skin);
        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onNewGame();
            }
        });
        table.add(newGameButton).width(250).height(60).padBottom(15).row();

        // Кнопка Main Menu
        mainMenuButton = new TextButton("MAIN MENU", skin);
        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onMainMenu();
            }
        });
        table.add(mainMenuButton).width(250).height(60);

        stage.addActor(table);
    }

    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
