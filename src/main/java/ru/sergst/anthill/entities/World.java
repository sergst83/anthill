package ru.sergst.anthill.entities;

import lombok.Getter;
import ru.sergst.anthill.config.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.sergst.anthill.config.Constants.WORLD_HEIGHT;
import static ru.sergst.anthill.config.Constants.WORLD_WIDTH;
import static ru.sergst.anthill.config.Constants.entityHeight;
import static ru.sergst.anthill.config.Constants.entityWidth;
import static ru.sergst.anthill.config.Constants.foodPointsCount;
import static ru.sergst.anthill.config.Constants.foodsPerAntMultiplier;
import static ru.sergst.anthill.config.Constants.markRemovePerTick;
import static ru.sergst.anthill.config.Constants.maxMarkWaight;

@Getter
public class World extends JPanel implements ActionListener {

    Timer timer = new Timer(Constants.delay, this);

    public static Random random = new Random();

    private Map<Class, Set<Entity>> entities = new HashMap<>();
    private Map<Rectangle, Integer> markedAreas = new HashMap<>();

    private int collectedFoods = 0;

    public World() {
        init();

        timer.start();
    }

    private void init() {
        setPreferredSize(new Dimension(WORLD_WIDTH, WORLD_HEIGHT));
        setFocusable(true);

        initAntHome();
        for (int i = 0; i < foodPointsCount; i++) {
            initFood();
        }
        initNewAnt();
    }

    private void initAntHome() {
        Point point = getRandomPont();
        entities.putIfAbsent(AntHome.class, Collections.singleton(new AntHome(point.x, point.y, entityWidth, entityHeight)));
    }

    private void initFood() {
        Point point = getRandomPont();
        Food food;
        do {
            food = new Food(point.x, point.y, entityWidth, entityHeight);
        } while (isEntitiesIntersects(food));
        Set<Entity> foods = entities.getOrDefault(Food.class, new HashSet<>());
        for (int i = 0; i < Constants.maxAntCount * foodsPerAntMultiplier; i++) {
            foods.add(food);
        }
        entities.put(Food.class, foods);
    }

    private boolean isEntitiesIntersects(final Entity entity) {
        return entities.values()
                .stream()
                .flatMap(Collection::stream)
                .anyMatch(e -> e.intersects((Rectangle2D) entity));
    }

    private Point getRandomPont() {
        int x = random.ints(1, WORLD_WIDTH - entityWidth).findAny().getAsInt();
        int y = random.ints(1, WORLD_HEIGHT - entityHeight).findAny().getAsInt();

        return new Point(x, y);
    }

    private void computeWorldTick() {
        if (!this.entities.get(Food.class).isEmpty() && this.entities.get(Ant.class).size() < Constants.maxAntCount) {
            initNewAnt();
        }

        //resend ant with food
        List<Entity> antsToRemove = entities.get(Ant.class)
                .stream()
                .filter(ant -> ant.getBounds().intersects(getAntHome()))
                .filter(ant -> ((Ant)ant).isWithFood())
                .collect(Collectors.toList());
        for (Entity entity : antsToRemove) {
            entities.get(Ant.class).remove(entity);
            collectedFoods++;
            System.out.println("Collected foods: " + collectedFoods);
        }

        entities.values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(Entity::compute);

        //испарение ферромона
        if (!markedAreas.isEmpty()) {
            List<Rectangle> keys = new ArrayList<>(markedAreas.keySet());
            for (Rectangle key : keys) {
                int v = Math.max(markedAreas.get(key) - markRemovePerTick, 0);
                if (v == 0) {
                    markedAreas.remove(key);
                } else {
                    markedAreas.put(key, v);
                }
            }
        }
    }

    private void initNewAnt() {
        Set<Entity> ants = entities.getOrDefault(Ant.class, new HashSet<>());
        Ant newAnt = new Ant(getAntHome(), this);
        System.out.println(newAnt);
        ants.add(newAnt);
        entities.put(Ant.class, ants);
    }

    private AntHome getAntHome() {
        return (AntHome) entities.get(AntHome.class).toArray()[0];
    }

    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        g.setColor(Constants.backGround);
        g.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        markedAreas.forEach((k, v) -> {
            int b = 200 - (255 * v) / maxMarkWaight;
            if (b > 255) {
                b = 255;
            } else if (b < 0) {
                b = 0;
            }
            Color color = new Color(255, 255, b); //градауии желтого
            g.setColor(color);
            g.fillRect(k.x, k.y, k.width, k.height);
        });

        entities.values()
                .stream()
                .flatMap(Collection::stream)
                .forEach(entity -> entity.draw(g));
    }

    public boolean isInWorld(int x, int y) {
        return x > 0 && x < WORLD_WIDTH && y > 0 && y < WORLD_HEIGHT;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        computeWorldTick();
        repaint();
    }
}
