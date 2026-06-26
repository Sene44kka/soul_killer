package ru.skripov.entities;

import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Player {
    public Rectangle bounds;
    public float speed = 200f;

    // Health
    public int maxHealth = 3;
    public int health = 300000000;

    // Invincibility frames
    public float invincibleTimer = 0f;
    public float invincibleDuration = 1.5f;
    public boolean isInvincible = false;

    // Frenzy
    public int frenzyKillCount = 0;
    public float frenzyTimer = 0f;
    public float frenzyDuration = 4f;
    public boolean isFrenzy = false;

    public List<HitEffect> hitEffects;

    // Score
    public int score = 0;

    public Player(float startX, float startY) {
        this.bounds = new Rectangle(startX, startY, 20, 20);
        this.hitEffects = new ArrayList<>();
    }

    public void update(float delta) {
        for (HitEffect e : hitEffects) {
            e.update(delta);
        }

        hitEffects.removeIf(hitEffect -> !hitEffect.alive);

        if (isInvincible) {
            invincibleTimer -= delta;
            if (invincibleTimer <= 0) {
                isInvincible = false;
            }
        }

        if (isFrenzy) {
            frenzyTimer -= delta;
            if (frenzyTimer <= 0) {
                isFrenzy = false;
                frenzyKillCount = 0;
                System.out.println("=== FRENZY ENDED ===");
            }
        }
    }

    public void takeDamage() {
        if (isInvincible) {
            return;
        }

        health--;

        for (int i = 0; i < 5; i++) {
            hitEffects.add(new HitEffect(getCenterX(), getCenterY(), false));
        }

        if (health <= 0) {
            System.out.println("=== SOUL SHATTERED. GAME OVER ===");
            return;
        }

        isInvincible = true;
        invincibleTimer = invincibleDuration;
    }

    public void onKill() {
        frenzyKillCount++;
        score += 100; // +100 очков за убийство

        if (frenzyKillCount >= 5 && !isFrenzy) {
            isFrenzy = true;
            frenzyTimer = frenzyDuration;
            System.out.println("=== FRENZY ACTIVATED! ===");
        } else if (isFrenzy) {
            frenzyTimer = frenzyDuration;
        }
    }

    public boolean isDead() {
        return health <= 0;
    }

    public float getCenterX() {
        return bounds.x + bounds.width / 2f;
    }

    public float getCenterY() {
        return bounds.y + bounds.height / 2f;
    }
}
