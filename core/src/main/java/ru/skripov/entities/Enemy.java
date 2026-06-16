package ru.skripov.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    public Rectangle bounds;
    public int hp;
    public int maxHp;
    public float speed;
    public boolean alive;
    public boolean isBig; // Для визуала

    public Enemy(float x, float y, boolean big) {
        this.isBig = big;
        if (big) {
            this.bounds = new Rectangle(x, y, 25, 25);
            this.hp = 5;
            this.maxHp = 5;
            this.speed = 60f; // Большие медленнее
        } else {
            this.bounds = new Rectangle(x, y, 15, 15);
            this.hp = 1;
            this.maxHp = 1;
            this.speed = 100f;
        }
        this.alive = true;
    }

    public void takeDamage() {
        hp--;
        if (hp <= 0) {
            alive = false;
        }
    }

    public boolean isDead() {
        return !alive;
    }

    public float getCenterX() {
        return bounds.x + bounds.width / 2f;
    }

    public float getCenterY() {
        return bounds.y + bounds.height / 2f;
    }

    public float getHpPercent() {
        return (float) hp / maxHp;
    }
}
