package ru.skripov.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class Level {
    // Размер уровня (квадратный)
    public static final float LEVEL_SIZE = 1600f;

    private final List<Obstacle> obstacles;
    private final List<Rectangle> obstacleBounds;

    // Типы препятствий
    public enum ObstacleType {
        TREE,      // Дерево
        HOUSE,     // Дом
        ROCK,      // Камень
        RUINS,     // Руины
        BUSH       // Куст
    }

    public Level() {
        this.obstacles = new ArrayList<>();
        this.obstacleBounds = new ArrayList<>();
        generateObstacles();
    }

    private void generateObstacles() {
        // Генерируем 30-50 препятствий
        int obstacleCount = MathUtils.random(30, 50);

        for (int i = 0; i < obstacleCount; i++) {
            int attempts = 0;
            boolean placed = false;

            while (!placed && attempts < 50) {
                // Случайный размер
                float size = 0;
                ObstacleType type;

                float typeRoll = MathUtils.random();
                if (typeRoll < 0.3f) {
                    type = ObstacleType.TREE;
                    size = MathUtils.random(20, 35);
                } else if (typeRoll < 0.5f) {
                    type = ObstacleType.HOUSE;
                    size = MathUtils.random(40, 70);
                } else if (typeRoll < 0.7f) {
                    type = ObstacleType.ROCK;
                    size = MathUtils.random(15, 30);
                } else if (typeRoll < 0.85f) {
                    type = ObstacleType.RUINS;
                    size = MathUtils.random(30, 50);
                } else {
                    type = ObstacleType.BUSH;
                    size = MathUtils.random(15, 25);
                }

                // Случайная позиция (с отступом от краев)
                float x = MathUtils.random(50, LEVEL_SIZE - size - 50);
                float y = MathUtils.random(50, LEVEL_SIZE - size - 50);

                Rectangle bounds = new Rectangle(x, y, size, size);

                // Проверяем, не пересекается ли с другими препятствиями
                boolean overlaps = false;
                for (Rectangle existing : obstacleBounds) {
                    // Добавляем небольшой отступ между объектами
                    Rectangle expanded = new Rectangle(
                        existing.x - 20,
                        existing.y - 20,
                        existing.width + 40,
                        existing.height + 40
                    );
                    if (bounds.overlaps(expanded)) {
                        overlaps = true;
                        break;
                    }
                }

                if (!overlaps) {
                    obstacles.add(new Obstacle(x, y, size, type));
                    obstacleBounds.add(bounds);
                    placed = true;
                }
                attempts++;
            }
        }
    }

    public List<Obstacle> getObstacles() {
        return obstacles;
    }

    public List<Rectangle> getObstacleBounds() {
        return obstacleBounds;
    }

    // Класс препятствия
    public static class Obstacle {
        public final Rectangle bounds;
        public final ObstacleType type;
        public final float size;

        public Obstacle(float x, float y, float size, ObstacleType type) {
            this.bounds = new Rectangle(x, y, size, size);
            this.size = size;
            this.type = type;
        }
    }
}
