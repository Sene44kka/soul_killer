package ru.skripov.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class SoulParticle {
    public Rectangle bounds;
    public float velocityX;
    public float velocityY;
    public boolean alive;
    public float lifeTimer;    // Частица мерцает и исчезает со временем
    public float maxLife = 1.5f; // Живёт 1.5 секунды

    private static final float SIZE = 6f;
    private static final float SPEED = 380f;

    public SoulParticle(float startX, float startY, float dirX, float dirY) {
        this.bounds = new Rectangle(startX, startY, SIZE, SIZE);
        this.alive = true;
        this.lifeTimer = maxLife;

        // Нормализуем направление
        float length = (float) Math.sqrt(dirX * dirX + dirY * dirY);
        if (length > 0) {
            dirX /= length;
            dirY /= length;
        }
        this.velocityX = dirX * SPEED;
        this.velocityY = dirY * SPEED;
    }

    public void update(float delta) {
        bounds.x += velocityX * delta;
        bounds.y += velocityY * delta;
        lifeTimer -= delta;

        // Частица умирает, если вышла за экран или истекло время жизни
        if (bounds.x < -SIZE || bounds.x > 480 ||
            bounds.y < -SIZE || bounds.y > 320 ||
            lifeTimer <= 0) {
            alive = false;
        }
    }

    // Насколько частица прозрачна (мерцает к концу жизни)
    public float getAlpha() {
        return MathUtils.clamp(lifeTimer / maxLife, 0.2f, 1f);
    }
}
