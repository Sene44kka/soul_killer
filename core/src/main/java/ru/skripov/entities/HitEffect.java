package ru.skripov.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class HitEffect {
    public Rectangle bounds;
    public float velocityX;
    public float velocityY;
    public float lifeTimer;
    public float maxLife;
    public boolean alive;
    public boolean isDeathEffect; // Для смерти — другой цвет и размер

    public HitEffect(float x, float y, boolean isDeath) {
        this.bounds = new Rectangle(x, y, isDeath ? 6 : 4, isDeath ? 6 : 4);
        this.maxLife = isDeath ? 0.6f : 0.3f;
        this.lifeTimer = maxLife;
        this.alive = true;
        this.isDeathEffect = isDeath;

        // Случайное направление разлёта
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float speed = isDeath ? MathUtils.random(80f, 180f) : MathUtils.random(40f, 100f);
        this.velocityX = MathUtils.cos(angle) * speed;
        this.velocityY = MathUtils.sin(angle) * speed;
    }

    public void update(float delta) {
        bounds.x += velocityX * delta;
        bounds.y += velocityY * delta;
        lifeTimer -= delta;
        if (lifeTimer <= 0) {
            alive = false;
        }
    }

    public float getAlpha() {
        return MathUtils.clamp(lifeTimer / maxLife, 0f, 1f);
    }
}
