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

public class MenuUI {
    private Stage stage;
    private Skin skin;
    private TextButton newGameButton;
    private TextButton continueButton;
    private TextButton recordsButton;
    private TextButton exitButton;
    private Label titleLabel;
    private Label versionLabel;

    private MenuListener listener;

    public interface MenuListener {
        void onNewGame();
        void onContinue();
        void onRecords();
        void onExit();
    }

    public MenuUI(MenuListener listener) {
        this.listener = listener;
        stage = new Stage();

        createSkin();
        createUI();
    }

    private void createSkin() {
        skin = new Skin();

        // Шрифт обычный
        BitmapFont font = new BitmapFont();
        font.getData().setScale(2f);
        skin.add("default-font", font);

        // Шрифт для заголовка
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(4f);

        // Шрифт для версии
        BitmapFont smallFont = new BitmapFont();
        smallFont.getData().setScale(1f);

        // === Стили кнопок ===
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = new Color(0.7f, 0.8f, 1f, 1f);
        buttonStyle.overFontColor = new Color(1f, 0.9f, 0.3f, 1f);
        buttonStyle.downFontColor = new Color(1f, 0.5f, 0f, 1f);

        // Создаём текстуры для фона кнопок программно
        buttonStyle.up = createButtonBackground(new Color(0.05f, 0.05f, 0.1f, 0.6f));
        buttonStyle.over = createButtonBackground(new Color(0.1f, 0.15f, 0.3f, 0.8f));
        buttonStyle.down = createButtonBackground(new Color(0.05f, 0.08f, 0.15f, 0.9f));

        // Стиль для неактивной кнопки
        buttonStyle.disabled = createButtonBackground(new Color(0.03f, 0.03f, 0.05f, 0.4f));
        buttonStyle.disabledFontColor = new Color(0.3f, 0.3f, 0.4f, 0.5f);

        skin.add("default", buttonStyle);

        // === Стиль заголовка ===
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = new Color(0.8f, 0.9f, 1f, 1f);
        skin.add("title", titleStyle);

        // === Стиль обычного текста (default для Label) ===
        Label.LabelStyle defaultLabelStyle = new Label.LabelStyle();
        defaultLabelStyle.font = smallFont;
        defaultLabelStyle.fontColor = new Color(0.7f, 0.8f, 1f, 1f);
        skin.add("default", defaultLabelStyle); // <-- ВОТ ЧТО БЫЛО ПРОПУЩЕНО
    }

    /**
     * Создаёт фон кнопки из цвета (простой прямоугольник 1x1, растягиваемый)
     */
    private TextureRegionDrawable createButtonBackground(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(texture);
    }

    private void createUI() {
        Table table = new Table();
        table.setFillParent(true);
        table.center();

        // Заголовок
        titleLabel = new Label("SOUL KILLER", skin, "title");
        table.add(titleLabel).padBottom(60).row();

        // Кнопка New Game
        newGameButton = new TextButton("NEW GAME", skin);
        newGameButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onNewGame();
            }
        });
        table.add(newGameButton).width(250).height(60).padBottom(15).row();

        // Кнопка Continue (пока неактивна)
        continueButton = new TextButton("CONTINUE", skin);
        continueButton.setDisabled(true);
        continueButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onContinue();
            }
        });
        table.add(continueButton).width(250).height(60).padBottom(15).row();

        // Кнопка Records (пока неактивна)
        recordsButton = new TextButton("RECORDS", skin);
        recordsButton.setDisabled(true);
        recordsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onRecords();
            }
        });
        table.add(recordsButton).width(250).height(60).padBottom(15).row();

        // Кнопка Exit
        exitButton = new TextButton("EXIT", skin);
        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                listener.onExit();
            }
        });
        table.add(exitButton).width(250).height(60).padBottom(15).row();

        // Версия игры (использует стиль "default")
        versionLabel = new Label("v0.1 - Prototype", skin);
        versionLabel.setColor(0.3f, 0.3f, 0.4f, 1f);
        table.add(versionLabel).padTop(40);

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

    public void setContinueEnabled(boolean enabled) {
        continueButton.setDisabled(!enabled);
    }

    public void show() {
        Gdx.input.setInputProcessor(stage);
    }

    public void hide() {
        Gdx.input.setInputProcessor(stage);
    }
}
