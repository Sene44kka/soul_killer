package ru.skripov.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import ru.skripov.Main;
import ru.skripov.entities.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoomScreen extends CommonScreen {
    private final Main game;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    // Player
    private Player player;

    // Soul Particles
    private final List<SoulParticle> particles;
    private float particleCooldown = 0f;
    private final float baseFireRate = 0.35f;

    // Enemies
    private List<Enemy> enemies;
    private float spawnTimer = 0f;
    private float spawnInterval = 1.5f;

    // Effects
    private List<HitEffect> deathEffects;

    // Room dimensions
    private final float wallThickness = 20f;
    private final float roomWidth = 640f;
    private final float roomHeight = 480f;
    private final float gameAreaX;
    private final float gameAreaY;
    private final float gameAreaWidth;
    private final float gameAreaHeight;

    // States
    private boolean gameOver = false;

    public RoomScreen(Main game) {
        this.game = game;
        this.shapeRenderer = new ShapeRenderer();
        this.batch = new SpriteBatch();
        this.font = new BitmapFont();
        font.getData().setScale(2f);

        this.gameAreaX = wallThickness;
        this.gameAreaY = wallThickness;
        this.gameAreaWidth = roomWidth - wallThickness * 2;
        this.gameAreaHeight = roomHeight - wallThickness * 2;

        this.player = new Player(
            gameAreaX + gameAreaWidth / 2f - 10,
            gameAreaY + gameAreaHeight / 2f - 10
        );
        this.particles = new ArrayList<>();
        this.enemies = new ArrayList<>();
        this.deathEffects = new ArrayList<>();

        game.setActiveRoomScreen(this);
        System.out.println("=== THE ROOM AWAKENS ===");
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

    private void update(float delta) {
        if (delta > 0.05f) delta = 0.05f;
        if (gameOver) {
            return;
        }

        player.update(delta);
        updateDeathEffects(delta);
        handlePlayerInput(delta);
        handleShooting(delta);
        updateParticles(delta);
//        spawnEnemies(delta);
        moveEnemiesToPlayer(delta);
        checkCollisions();
        clampPlayerToGameArea();

        if (player.isDead()) {
            gameOver = true;
            System.out.println("=== GAME OVER - Score: " + player.score + " ===");
            Gdx.app.postRunnable(() -> {
                game.setScreen(new GameOverScreen(game, player.score));
            });
        }
    }

    private void handlePlayerInput(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.A)) player.bounds.x -= player.speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) player.bounds.x += player.speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) player.bounds.y += player.speed * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) player.bounds.y -= player.speed * delta;
    }

    private void handleShooting(float delta) {
        particleCooldown -= delta;
        if (particleCooldown > 0f) return;

        // Стреляем только если зажата ЛКМ (или ПКМ, если хочешь)
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) return;

        // Получаем координаты мыши в игровых координатах
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.input.getY();

        // LibGDX: Y экрана инвертирован (0 сверху), переворачиваем
        mouseY = roomHeight - mouseY;

        // Направление от игрока к мыши
        float dirX = mouseX - player.getCenterX();
        float dirY = mouseY - player.getCenterY();

        // Если мышь прямо на игроке — не стреляем
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length < 5f) return;

        // Нормализуем
        dirX /= length;
        dirY /= length;

        particles.add(new SoulParticle(player.getCenterX() - 3f, player.getCenterY() - 3f, dirX, dirY));

        particleCooldown = player.isFrenzy ? baseFireRate * 0.5f : baseFireRate;
    }

    private void updateParticles(float delta) {
        for (SoulParticle p : particles) p.update(delta);
        particles.removeIf(p -> !p.alive);
    }

    private void updateDeathEffects(float delta) {
        for (HitEffect e : deathEffects) e.update(delta);
        deathEffects.removeIf(e -> !e.alive);
    }

    private void spawnEnemies(float delta) {
        spawnTimer += delta;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            boolean isBig = MathUtils.random(0, 14) == 0;
            float ex, ey;

            int side = MathUtils.random(0, 3);
            if (isBig) {
                Enemy big = new Enemy(0, 0, true);
                switch (side) {
                    case 0: ex = MathUtils.random(gameAreaX, gameAreaX + gameAreaWidth - big.bounds.width);
                        ey = gameAreaY + gameAreaHeight; break;
                    case 1: ex = MathUtils.random(gameAreaX, gameAreaX + gameAreaWidth - big.bounds.width);
                        ey = gameAreaY - big.bounds.height; break;
                    case 2: ex = gameAreaX - big.bounds.width;
                        ey = MathUtils.random(gameAreaY, gameAreaY + gameAreaHeight - big.bounds.height); break;
                    default: ex = gameAreaX + gameAreaWidth;
                        ey = MathUtils.random(gameAreaY, gameAreaY + gameAreaHeight - big.bounds.height); break;
                }
                big.bounds.x = ex;
                big.bounds.y = ey;
                enemies.add(big);
            } else {
                Enemy small = new Enemy(0, 0, false);
                switch (side) {
                    case 0: ex = MathUtils.random(gameAreaX, gameAreaX + gameAreaWidth - small.bounds.width);
                        ey = gameAreaY + gameAreaHeight; break;
                    case 1: ex = MathUtils.random(gameAreaX, gameAreaX + gameAreaWidth - small.bounds.width);
                        ey = gameAreaY - small.bounds.height; break;
                    case 2: ex = gameAreaX - small.bounds.width;
                        ey = MathUtils.random(gameAreaY, gameAreaY + gameAreaHeight - small.bounds.height); break;
                    default: ex = gameAreaX + gameAreaWidth;
                        ey = MathUtils.random(gameAreaY, gameAreaY + gameAreaHeight - small.bounds.height); break;
                }
                small.bounds.x = ex;
                small.bounds.y = ey;
                enemies.add(small);
            }
        }
    }

    private void moveEnemiesToPlayer(float delta) {
        float px = player.getCenterX();
        float py = player.getCenterY();
        for (Enemy enemy : enemies) {
            float angle = MathUtils.atan2(py - enemy.getCenterY(), px - enemy.getCenterX());
            enemy.bounds.x += MathUtils.cos(angle) * enemy.speed * delta;
            enemy.bounds.y += MathUtils.sin(angle) * enemy.speed * delta;
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
            enemies.removeIf(e -> e.isDead());
        }
    }

    private void clampPlayerToGameArea() {
        if (player.bounds.x < gameAreaX) player.bounds.x = gameAreaX;
        if (player.bounds.x > gameAreaX + gameAreaWidth - player.bounds.width)
            player.bounds.x = gameAreaX + gameAreaWidth - player.bounds.width;
        if (player.bounds.y < gameAreaY) player.bounds.y = gameAreaY;
        if (player.bounds.y > gameAreaY + gameAreaHeight - player.bounds.height)
            player.bounds.y = gameAreaY + gameAreaHeight - player.bounds.height;
    }

    // ==================== DRAW ====================

    private void draw() {
        Gdx.gl.glClearColor(0.02f, 0.02f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // --- Игровой мир ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

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

        // Стены (поверх врагов)
        drawWalls();

        shapeRenderer.end();

        // --- HUD (поверх игры) ---
        batch.begin();
        drawHUD();
        batch.end();
    }

    private void drawHUD() {
        // Фон для HUD (полупрозрачная полоса сверху)
        // Рисуем через ShapeRenderer в том же batch? Нет, проще через font

        // HP
        String hpText = "HP: ";
        for (int i = 0; i < player.maxHealth; i++) {
            hpText += i < player.health ? "♥ " : "♡ ";
        }
        font.setColor(1f, 0.2f, 0.2f, 0.9f);
        font.draw(batch, hpText, 25, roomHeight - 8);

        // Score
        String scoreText = "Score: " + player.score;
        font.setColor(1f, 0.9f, 0.3f, 0.9f);
        font.draw(batch, scoreText, roomWidth - 180, roomHeight - 8);

        // Frenzy indicator
        if (player.isFrenzy) {
            font.setColor(1f, 0.5f, 0f, 0.8f + MathUtils.sin(player.frenzyTimer * 10f) * 0.2f);
            font.draw(batch, "FRENZY!", roomWidth / 2f - 50, roomHeight - 8);
        }
    }

    private void drawWalls() {
        shapeRenderer.setColor(0.15f, 0.15f, 0.2f, 1f);
        shapeRenderer.rect(0, roomHeight - wallThickness, roomWidth, wallThickness);
        shapeRenderer.rect(0, 0, roomWidth, wallThickness);
        shapeRenderer.rect(0, 0, wallThickness, roomHeight);
        shapeRenderer.rect(roomWidth - wallThickness, 0, wallThickness, roomHeight);

        shapeRenderer.setColor(0.25f, 0.25f, 0.35f, 1f);
        shapeRenderer.rectLine(gameAreaX, gameAreaY + gameAreaHeight, gameAreaX + gameAreaWidth, gameAreaY + gameAreaHeight, 2f);
        shapeRenderer.rectLine(gameAreaX, gameAreaY, gameAreaX + gameAreaWidth, gameAreaY, 2f);
        shapeRenderer.rectLine(gameAreaX, gameAreaY, gameAreaX, gameAreaY + gameAreaHeight, 2f);
        shapeRenderer.rectLine(gameAreaX + gameAreaWidth, gameAreaY, gameAreaX + gameAreaWidth, gameAreaY + gameAreaHeight, 2f);

        shapeRenderer.setColor(0.3f, 0.3f, 0.4f, 1f);
        float cs = 8f;
        shapeRenderer.rect(gameAreaX - 2, gameAreaY + gameAreaHeight - 2, cs, cs);
        shapeRenderer.rect(gameAreaX + gameAreaWidth - cs + 2, gameAreaY + gameAreaHeight - 2, cs, cs);
        shapeRenderer.rect(gameAreaX - 2, gameAreaY - 2, cs, cs);
        shapeRenderer.rect(gameAreaX + gameAreaWidth - cs + 2, gameAreaY - 2, cs, cs);
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
        batch.dispose();
        font.dispose();
    }

    @Override
    public void show() {
        // Когда возвращаемся в игру (Continue) — InputProcessor уже для клавиш не нужен,
        // но если бы был UI в игре (пауза-меню), здесь бы мы его восстановили.
        // Пока пусто, клавиши обрабатываются через Gdx.input в update()
    }
}
