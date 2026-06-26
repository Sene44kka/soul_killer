package ru.skripov.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ru.skripov.Main;
import ru.skripov.entities.*;
import ru.skripov.entities.Level.Obstacle;
import ru.skripov.entities.Level.ObstacleType;
import ru.skripov.ui.RoomUI;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoomScreen extends ScreenAdapter {
    private Stage stage;
    private final RoomUI roomUI;

    private final Main game;
    private final ShapeRenderer shapeRenderer;
    private final SpriteBatch batch;
    private final BitmapFont font;

    // Камера и Viewport
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final Vector3 mousePos;

    // Уровень
    private final Level level;
    private final List<Obstacle> obstacles;

    // Player
    private final Player player;

    // Soul Particles
    private final List<SoulParticle> particles;
    private float particleCooldown = 0f;
    private final float baseFireRate = 0.35f;

    // Enemies
    private final List<Enemy> enemies;
    private float spawnTimer = 0f;
    private float spawnInterval = 1.5f;

    // Effects
    private final List<HitEffect> deathEffects;

    // Размер видимой области
    private final float viewportWidth = 640f;
    private final float viewportHeight = 480f;

    private boolean gameOver = false;

    public RoomScreen(Main game) {
        this.game = game;
        this.roomUI = new RoomUI();

        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        font.getData().setScale(2f);

        // Камера
        this.camera = new OrthographicCamera();
        this.viewport = new FitViewport(viewportWidth, viewportHeight, camera);
        this.mousePos = new Vector3();

        // Создаем уровень
        this.level = new Level();
        this.obstacles = level.getObstacles();

        // Игрок в центре
        float startX = Level.LEVEL_SIZE / 2f - 15;
        float startY = Level.LEVEL_SIZE / 2f - 15;
        this.player = new Player(startX, startY);

        camera.position.set(player.getCenterX(), player.getCenterY(), 0);
        camera.update();

        this.particles = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.deathEffects = new ArrayList<>();

        game.setActiveRoomScreen(this);
        System.out.println("=== LEVEL GENERATED ===");
        System.out.println("Size: " + Level.LEVEL_SIZE + "x" + Level.LEVEL_SIZE);
        System.out.println("Obstacles: " + obstacles.size());
    }

    @Override
    public void show() {
        roomUI.show();
    }

    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MenuScreen(game));
            return;
        }

        update(delta);
        draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    private void update(float delta) {
        if (delta > 0.05f) delta = 0.05f;
        if (gameOver) return;

        player.update(delta);
        updateDeathEffects(delta);
        handlePlayerInput(delta);
        handleShooting(delta);
        updateParticles(delta);
        spawnEnemies(delta);
        moveEnemiesToPlayer(delta);
        checkCollisions();
        clampPlayerToLevel();

        // Камера следует за игроком
        updateCamera();

        if (player.isDead()) {
            gameOver = true;
            System.out.println("=== GAME OVER - Score: " + player.score + " ===");
            Gdx.app.postRunnable(() -> {
                game.setScreen(new GameOverScreen(game, player.score));
            });
        }
    }

    private void updateCamera() {
        float targetX = player.getCenterX();
        float targetY = player.getCenterY();

        // Плавное слежение
        camera.position.x += (targetX - camera.position.x) * 0.1f;
        camera.position.y += (targetY - camera.position.y) * 0.1f;

        // Ограничиваем камеру
        float halfWidth = viewportWidth / 2f;
        float halfHeight = viewportHeight / 2f;

        camera.position.x = MathUtils.clamp(camera.position.x, halfWidth, Level.LEVEL_SIZE - halfWidth);
        camera.position.y = MathUtils.clamp(camera.position.y, halfHeight, Level.LEVEL_SIZE - halfHeight);

        camera.update();
    }

    private void handlePlayerInput(float delta) {
        float newX = player.bounds.x;
        float newY = player.bounds.y;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) newX -= player.speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) newX += player.speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) newY += player.speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) newY -= player.speed * delta;

        // Проверяем столкновения с препятствиями
        Rectangle testBounds = new Rectangle(newX, newY, player.bounds.width, player.bounds.height);
        boolean canMove = true;

        for (Obstacle obstacle : obstacles) {
            if (testBounds.overlaps(obstacle.bounds)) {
                canMove = false;
                break;
            }
        }

        if (canMove) {
            player.bounds.x = newX;
            player.bounds.y = newY;
        } else {
            // Только по X
            testBounds.x = newX;
            testBounds.y = player.bounds.y;
            canMove = true;
            for (Obstacle obstacle : obstacles) {
                if (testBounds.overlaps(obstacle.bounds)) {
                    canMove = false;
                    break;
                }
            }
            if (canMove) {
                player.bounds.x = newX;
            }

            // Только по Y
            testBounds.x = player.bounds.x;
            testBounds.y = newY;
            canMove = true;
            for (Obstacle obstacle : obstacles) {
                if (testBounds.overlaps(obstacle.bounds)) {
                    canMove = false;
                    break;
                }
            }
            if (canMove) {
                player.bounds.y = newY;
            }
        }
    }

    private void clampPlayerToLevel() {
        player.bounds.x = MathUtils.clamp(player.bounds.x, 0, Level.LEVEL_SIZE - player.bounds.width);
        player.bounds.y = MathUtils.clamp(player.bounds.y, 0, Level.LEVEL_SIZE - player.bounds.height);
    }

    private void handleShooting(float delta) {
        particleCooldown -= delta;
        if (particleCooldown > 0f) return;

        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) return;

        mousePos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mousePos);

        float dirX = mousePos.x - player.getCenterX();
        float dirY = mousePos.y - player.getCenterY();

        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length < 5f) return;

        dirX /= length;
        dirY /= length;

        particles.add(new SoulParticle(player.getCenterX() - 3f, player.getCenterY() - 3f, dirX, dirY));
        particleCooldown = player.isFrenzy ? baseFireRate * 0.5f : baseFireRate;
    }

    private void updateParticles(float delta) {
        Iterator<SoulParticle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            SoulParticle p = iterator.next();
            p.update(delta);
            if (!p.alive) {
                iterator.remove();
            }
        }
    }

    private void updateDeathEffects(float delta) {
        Iterator<HitEffect> iterator = deathEffects.iterator();
        while (iterator.hasNext()) {
            HitEffect e = iterator.next();
            e.update(delta);
            if (!e.alive) {
                iterator.remove();
            }
        }
    }

    private void spawnEnemies(float delta) {
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;

            boolean isBig = MathUtils.random(0, 14) == 0;
            Enemy enemy = new Enemy(0, 0, isBig);

            // Спавним в случайном месте, но не слишком близко к игроку
            int attempts = 0;
            boolean placed = false;

            while (!placed && attempts < 30) {
                float x = MathUtils.random(50, Level.LEVEL_SIZE - 50 - enemy.bounds.width);
                float y = MathUtils.random(50, Level.LEVEL_SIZE - 50 - enemy.bounds.height);

                Rectangle testBounds = new Rectangle(x, y, enemy.bounds.width, enemy.bounds.height);
                boolean overlaps = false;

                // Проверяем, не на препятствии ли
                for (Obstacle obstacle : obstacles) {
                    if (testBounds.overlaps(obstacle.bounds)) {
                        overlaps = true;
                        break;
                    }
                }

                // Проверяем, не слишком близко к игроку
                float distToPlayer = (float) Math.sqrt(
                    Math.pow(x - player.getCenterX(), 2) +
                        Math.pow(y - player.getCenterY(), 2)
                );

                if (!overlaps && distToPlayer > 300) {
                    enemy.bounds.x = x;
                    enemy.bounds.y = y;
                    enemies.add(enemy);
                    placed = true;
                }
                attempts++;
            }
        }
    }

    private void moveEnemiesToPlayer(float delta) {
        float px = player.getCenterX();
        float py = player.getCenterY();

        for (Enemy enemy : enemies) {
            float angle = MathUtils.atan2(py - enemy.getCenterY(), px - enemy.getCenterX());
            float newX = enemy.bounds.x + MathUtils.cos(angle) * enemy.speed * delta;
            float newY = enemy.bounds.y + MathUtils.sin(angle) * enemy.speed * delta;

            Rectangle testBounds = new Rectangle(newX, newY, enemy.bounds.width, enemy.bounds.height);
            boolean canMove = true;

            for (Obstacle obstacle : obstacles) {
                if (testBounds.overlaps(obstacle.bounds)) {
                    canMove = false;
                    break;
                }
            }

            if (canMove) {
                enemy.bounds.x = newX;
                enemy.bounds.y = newY;
            }
        }
    }

    private void checkCollisions() {
        // Частицы vs враги
        Iterator<SoulParticle> partIt = particles.iterator();
        while (partIt.hasNext()) {
            SoulParticle p = partIt.next();
            boolean hit = false;
            for (Enemy enemy : enemies) {
                if (p.bounds.overlaps(enemy.bounds)) {
                    enemy.takeDamage();
                    for (int i = 0; i < 4; i++) {
                        deathEffects.add(new HitEffect(enemy.getCenterX(), enemy.getCenterY(), false));
                    }
                    if (enemy.isDead()) {
                        for (int i = 0; i < 12; i++) {
                            deathEffects.add(new HitEffect(enemy.getCenterX(), enemy.getCenterY(), true));
                        }
                        player.onKill();
                    }
                    hit = true;
                    break;
                }
            }
            if (hit) partIt.remove();
        }

        enemies.removeIf(e -> e.isDead());

        // Враги vs игрок
        if (!player.isInvincible) {
            for (Enemy enemy : enemies) {
                if (enemy.bounds.overlaps(player.bounds)) {
                    player.takeDamage();
                    enemy.takeDamage();
                    if (enemy.isDead()) {
                        for (int i = 0; i < 12; i++) {
                            deathEffects.add(new HitEffect(enemy.getCenterX(), enemy.getCenterY(), true));
                        }
                    }
                    break;
                }
            }
            enemies.removeIf(Enemy::isDead);
        }
    }

    private void draw() {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Пол (фон)
        shapeRenderer.setColor(0.02f, 0.02f, 0.04f, 1f);
        shapeRenderer.rect(0, 0, Level.LEVEL_SIZE, Level.LEVEL_SIZE);

        // Сетка пола
        shapeRenderer.setColor(0.04f, 0.04f, 0.07f, 1f);
        for (float x = 0; x < Level.LEVEL_SIZE; x += 64) {
            for (float y = 0; y < Level.LEVEL_SIZE; y += 64) {
                if (((int)(x / 64) + (int)(y / 64)) % 2 == 0) {
                    shapeRenderer.rect(x, y, 64, 64);
                }
            }
        }

        // Рисуем препятствия
        for (Obstacle obstacle : obstacles) {
            drawObstacle(obstacle);
        }

        // Враги
        for (Enemy enemy : enemies) {
            if (enemy.isBig) {
                float hpPct = enemy.getHpPercent();
                shapeRenderer.setColor(0.8f, 0.4f * hpPct + 0.1f, 0.1f, 1f);
                shapeRenderer.circle(enemy.getCenterX(), enemy.getCenterY(), enemy.bounds.width / 2f);
            } else {
                shapeRenderer.setColor(0.6f, 0.1f, 0.1f, 1f);
                shapeRenderer.circle(enemy.getCenterX(), enemy.getCenterY(), enemy.bounds.width / 2f);
            }
        }

        // Эффекты
        for (HitEffect e : deathEffects) {
            if (e.alive) {
                float alpha = e.getAlpha();
                shapeRenderer.setColor(
                    e.isDeathEffect ? 1f : 1f,
                    e.isDeathEffect ? 0.8f : 0.9f,
                    e.isDeathEffect ? 0.2f : 0.9f,
                    alpha
                );
                shapeRenderer.rect(e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height);
            }
        }

        for (HitEffect e : player.hitEffects) {
            if (e.alive) {
                shapeRenderer.setColor(1f, 0.2f, 0.2f, e.getAlpha());
                shapeRenderer.rect(e.bounds.x, e.bounds.y, e.bounds.width, e.bounds.height);
            }
        }

        // Частицы
        for (SoulParticle p : particles) {
            if (p.alive) {
                float alpha = p.getAlpha();
                if (player.isFrenzy) {
                    shapeRenderer.setColor(1f, 0.5f, 0f, alpha);
                } else {
                    shapeRenderer.setColor(0.3f, 0.8f, 1f, alpha);
                }
                shapeRenderer.rect(p.bounds.x, p.bounds.y, p.bounds.width, p.bounds.height);
            }
        }

        // Игрок
        if (player.isInvincible) {
            float blink = MathUtils.sin(player.invincibleTimer * 20f) > 0 ? 1f : 0.2f;
            shapeRenderer.setColor(
                player.isFrenzy ? 1f : 0.3f,
                player.isFrenzy ? 0.3f : 0.6f,
                player.isFrenzy ? 0f : 1f,
                blink
            );
        } else {
            shapeRenderer.setColor(
                player.isFrenzy ? 1f : 0.3f,
                player.isFrenzy ? 0.3f : 0.6f,
                player.isFrenzy ? 0f : 1f,
                1f
            );
        }
        shapeRenderer.rect(player.bounds.x, player.bounds.y, player.bounds.width, player.bounds.height);

        shapeRenderer.end();

        // HUD
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();

        // HP
        StringBuilder hpText = new StringBuilder("HP: ");
        for (int i = 0; i < player.maxHealth; i++) {
            hpText.append(i < player.health ? "♥ " : "♡ ");
        }
        font.setColor(1f, 0.2f, 0.2f, 0.9f);
        font.draw(batch, hpText.toString(), 25, Gdx.graphics.getHeight() - 20);

        // Score
        String scoreText = "Score: " + player.score;
        font.setColor(1f, 0.9f, 0.3f, 0.9f);
        font.draw(batch, scoreText, Gdx.graphics.getWidth() - 200, Gdx.graphics.getHeight() - 20);

        // Frenzy
        if (player.isFrenzy) {
            font.setColor(1f, 0.5f, 0f, 0.8f + MathUtils.sin(player.frenzyTimer * 10f) * 0.2f);
            font.draw(batch, "⚡ FRENZY! ⚡", Gdx.graphics.getWidth() / 2f - 80, Gdx.graphics.getHeight() - 20);
        }

        batch.end();
    }

    private void drawObstacle(Obstacle obstacle) {
        float x = obstacle.bounds.x;
        float y = obstacle.bounds.y;
        float size = obstacle.size;

        switch (obstacle.type) {
            case TREE:
                // Ствол
                shapeRenderer.setColor(0.3f, 0.2f, 0.1f, 1f);
                shapeRenderer.rect(x + size * 0.4f, y + size * 0.3f, size * 0.2f, size * 0.5f);
                // Крона
                shapeRenderer.setColor(0.1f, 0.4f, 0.1f, 1f);
                shapeRenderer.circle(x + size * 0.5f, y + size * 0.6f, size * 0.5f);
                break;

            case HOUSE:
                // Стены
                shapeRenderer.setColor(0.4f, 0.3f, 0.2f, 1f);
                shapeRenderer.rect(x, y, size, size);
                // Крыша
                shapeRenderer.setColor(0.6f, 0.2f, 0.1f, 1f);
                float[] vertices = {
                    x, y + size,
                    x + size / 2, y + size * 1.4f,
                    x + size, y + size
                };
                shapeRenderer.triangle(vertices[0], vertices[1], vertices[2], vertices[3], vertices[4], vertices[5]);
                // Окна
                shapeRenderer.setColor(0.8f, 0.7f, 0.3f, 0.7f);
                shapeRenderer.rect(x + size * 0.15f, y + size * 0.4f, size * 0.2f, size * 0.2f);
                shapeRenderer.rect(x + size * 0.6f, y + size * 0.4f, size * 0.2f, size * 0.2f);
                // Дверь
                shapeRenderer.setColor(0.3f, 0.2f, 0.1f, 1f);
                shapeRenderer.rect(x + size * 0.4f, y, size * 0.2f, size * 0.3f);
                break;

            case ROCK:
                shapeRenderer.setColor(0.4f, 0.4f, 0.4f, 1f);
                shapeRenderer.circle(x + size / 2, y + size / 2, size / 2);
                shapeRenderer.setColor(0.5f, 0.5f, 0.5f, 1f);
                shapeRenderer.circle(x + size * 0.4f, y + size * 0.6f, size * 0.15f);
                break;

            case RUINS:
                // Разрушенные стены
                shapeRenderer.setColor(0.3f, 0.25f, 0.2f, 1f);
                shapeRenderer.rect(x, y, size, size * 0.3f);
                shapeRenderer.rect(x, y + size * 0.3f, size * 0.3f, size * 0.2f);
                shapeRenderer.rect(x + size * 0.7f, y + size * 0.3f, size * 0.3f, size * 0.2f);
                shapeRenderer.rect(x + size * 0.3f, y + size * 0.5f, size * 0.4f, size * 0.3f);
                // Обломки
                shapeRenderer.setColor(0.4f, 0.35f, 0.3f, 1f);
                shapeRenderer.circle(x + size * 0.2f, y + size * 0.2f, size * 0.1f);
                shapeRenderer.circle(x + size * 0.8f, y + size * 0.15f, size * 0.08f);
                break;

            case BUSH:
                shapeRenderer.setColor(0.1f, 0.3f, 0.1f, 1f);
                shapeRenderer.circle(x + size * 0.3f, y + size * 0.3f, size * 0.4f);
                shapeRenderer.circle(x + size * 0.7f, y + size * 0.3f, size * 0.4f);
                shapeRenderer.circle(x + size * 0.5f, y + size * 0.5f, size * 0.45f);
                break;
        }
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

    @Override
    public void hide() {
        roomUI.hide();
    }
}
