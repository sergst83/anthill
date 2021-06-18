package ru.sergst.anthill.entities;

import lombok.Getter;
import lombok.val;
import ru.sergst.anthill.config.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.security.SecureRandom;
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
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;
import static ru.sergst.anthill.config.Constants.*;

@Getter
public class World extends JPanel implements ActionListener {

    private final Timer timer = new Timer(delay, this);

    public static final Random random = new SecureRandom();

    private final Set<Food> foods = new HashSet<>();
    private final Set<Ant> ants = new HashSet<>();
    private final List<AntHome> antHomes = new ArrayList<>();
    private final Map<Rectangle, Integer> markedAreas = new HashMap<>();

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
        //создаем муравейник
        antHomes.add(new AntHome(point, entityDimension));
    }

    private void initFood() {
        Point point = getRandomPont();
        Food food;
        do {
            food = new Food(point.x, point.y, entityWidth, entityHeight);
        } while (isEntitiesIntersects(food));
        for (int i = 0; i < maxAntCount * foodsPerAntMultiplier; i++) {
            foods.add(food);
        }
    }

    private boolean isEntitiesIntersects(final Entity entity) {
        return foods.stream().anyMatch(food -> food.intersects((Rectangle2D) entity))
                || antHomes.stream().anyMatch(antHome -> antHome.intersects((Rectangle2D) entity))
                || ants.stream().anyMatch(ant -> ant.intersects((Rectangle2D) entity));
    }

    private Point getRandomPont() {
        int x = random.ints(1, WORLD_WIDTH - entityWidth).findAny().getAsInt();
        int y = random.ints(1, WORLD_HEIGHT - entityHeight).findAny().getAsInt();

        return new Point(x, y);
    }

    private void computeWorldTick() {
        if (!foods.isEmpty() && ants.size() < maxAntCount) {
            initNewAnt();
        }

        //получаем список муравьев возле норки с едой
        val antsToRemove = ants.stream()
                .filter(ant -> ant.getBounds().intersects(getAntHome()))
                .filter(ant -> ((Ant)ant).isWithFood())
                .collect(toList());
        //удаляем муравьев принесших еду с поля и увеличиваем счетчик еды
        antsToRemove.forEach(ants::remove);
        collectedFoods += antsToRemove.size();
        System.out.println("Collected foods: " + collectedFoods);

        ants.forEach(Entity::compute);
        foods.forEach(Entity::compute);
        antHomes.forEach(Entity::compute);

        //испарение ферромона
        if (!markedAreas.isEmpty()) {
            val keys = new ArrayList<>(markedAreas.keySet());
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
        val newAnt = new Ant(getAntHome(), this);
        System.out.println(newAnt);
        ants.add(newAnt);
    }

    private AntHome getAntHome() {
        return antHomes.get(0);
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

        ants.forEach(evtity -> evtity.draw(g));
        foods.forEach(evtity -> evtity.draw(g));
        antHomes.forEach(evtity -> evtity.draw(g));
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
