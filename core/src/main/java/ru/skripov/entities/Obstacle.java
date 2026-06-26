package ru.skripov.entities;

import com.badlogic.gdx.math.Rectangle;

public class Obstacle {
    public enum Type {
        RUINS,      // Разрушенное здание
        TREE,       // Дерево
        ROCK,       // Камень
        FENCE,      // Забор
        CRATER      // Кратер
    }

    public final Rectangle bounds;
    public final Type type;
    public final float size;

    public Obstacle(float x, float y, float size, Type type) {
        this.bounds = new Rectangle(x, y, size, size);
        this.size = size;
        this.type = type;
    }
}
