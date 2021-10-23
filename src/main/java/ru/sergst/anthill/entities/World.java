package ru.sergst.anthill.entities;

import lombok.Getter;
import lombok.val;

import javax.swing.Timer;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.*;

import static java.lang.Math.max;
import static java.util.stream.Collectors.toList;
import static ru.sergst.anthill.Util.nextInt;
import static ru.sergst.anthill.config.Constants.*;

@Getter
public class World extends JPanel implements ActionListener {

    private final Timer timer = new Timer(DELAY, this);

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
        for (int i = 0; i < FOOD_POINTS_COUNT; i++) {
            initFood();
        }
        initNewAnt();
    }

    private void initAntHome() {
        //создаем муравейник
        antHomes.add(new AntHome(getRandomPont(), ENTITY_DIMENSION));
    }

    private void initFood() {
        val point = getRandomPont();
        Food food;
        do {
            food = new Food(point.x, point.y, ENTITY_WIDTH, ENTITY_HEIGHT);
        } while (isEntitiesIntersects(food));
        for (int i = 0; i < MAX_ANT_COUNT * FOODS_PER_ANT_MULTIPLIER; i++) {
            foods.add(food);
        }
    }

    private boolean isEntitiesIntersects(final Entity entity) {
        return foods.stream().anyMatch(food -> food.intersects((Rectangle2D) entity))
                || antHomes.stream().anyMatch(antHome -> antHome.intersects((Rectangle2D) entity))
                || ants.stream().anyMatch(ant -> ant.intersects((Rectangle2D) entity));
    }

    private Point getRandomPont() {
        return new Point(
                nextInt(1, WORLD_WIDTH - ENTITY_WIDTH),
                nextInt(1, WORLD_HEIGHT - ENTITY_HEIGHT)
        );
    }

    private void computeWorldTick() {
        if (!foods.isEmpty() && ants.size() < MAX_ANT_COUNT) {
            initNewAnt();
        }

        //получаем список муравьев возле норки с едой
        val antsToRemove = ants.stream()
                .filter(ant -> ant.intersects(getAntHome()))
                .filter(Ant::isWithFood)
                .collect(toList());
        //удаляем муравьев принесших еду с поля и увеличиваем счетчик еды
        antsToRemove.forEach(ants::remove);
        incrementCollectedFood(antsToRemove.size());

        ants.forEach(Entity::compute);
        foods.forEach(Entity::compute);
        antHomes.forEach(Entity::compute);

        pheromoneEvaporation();
    }

    /**
     * Испарение феромона
     */
    private void pheromoneEvaporation() {
        if (!markedAreas.isEmpty()) {
            val markedRectangles = new ArrayList<>(markedAreas.keySet());
            for (Rectangle markedRectangle : markedRectangles) {
                int brightness = max(MARK_REMOVE_FUNCTION.apply(markedAreas.get(markedRectangle)), 0);
                if (brightness == 0) {
                    markedAreas.remove(markedRectangle); //если все испарилось, удаляем из помеченных
                } else {
                    markedAreas.put(markedRectangle, brightness);
                }
            }
        }
    }

    private void incrementCollectedFood(final int foodCount) {
        if (foodCount > 0) {
            collectedFoods += foodCount;
            System.out.println("Collected foods: " + collectedFoods);
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
    public void paint(final Graphics graphics) {
        graphics.clearRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        markedAreas.forEach((rectangle, brightness) -> {
            graphics.setColor(calcMarkedRectangleColor(brightness));
            graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
        });

        ants.forEach(entity -> entity.draw(graphics));
        foods.forEach(entity -> entity.draw(graphics));
        antHomes.forEach(entity -> entity.draw(graphics));
    }

    /**
     * Вычисление цвета, помеченной феромоном, ячейки
     *
     * @param brightness яркость компонента
     * @return цвет
     */
    private Color calcMarkedRectangleColor(final int brightness) {
        int blue = 200 - (255 * brightness) / MAX_MARK_WEIGHT;
        if (blue > 255) {
            blue = 255;
        } else if (blue < 0) {
            blue = 0;
        }

        return new Color(255, 255, blue); //градации желтого
    }

    public boolean isInWorld(final int x, final int y) {
        return x > 0 && x < WORLD_WIDTH && y > 0 && y < WORLD_HEIGHT;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        computeWorldTick();
        repaint();
    }
}
