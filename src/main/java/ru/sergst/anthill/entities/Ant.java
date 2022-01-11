package ru.sergst.anthill.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.ToString.Include;
import lombok.val;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.function.BiPredicate;

import static java.lang.Integer.MAX_VALUE;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static ru.sergst.anthill.Util.nextInt;
import static ru.sergst.anthill.config.Constants.*;

@ToString(onlyExplicitlyIncluded = true)
@Getter
public class Ant extends Rectangle implements Entity {

    private final BiPredicate<Rectangle, Rectangle> HOME_PREDICATE = Rectangle::intersects;
    private final BiPredicate<Rectangle, Collection<Ant>> ANT_PREDICATE =
            (position, ants) -> ants.stream()
                    .filter(ant -> !ant.equals(this))
                    .anyMatch(ant -> ant.intersects(position));

    /**
     * Путь из прямоугольников пройденный муравьём от дома
     */
    private Deque<Rectangle> path;
    private final World world;
    @Setter
    private boolean withFood = false;
    private final AntHome antHome;
    private final int pathLength = 1;
    @Include
    private final String antName;

    public Ant(AntHome antHome, World world) {
        super(antHome.getLocation(), antHome.getSize());
        path = new LinkedList<>();
        path.push(antHome);
        this.world = world;
        this.antHome = antHome;
        this.antName = String.valueOf(nextInt(1, MAX_VALUE));
    }

    @Override
    public void compute() {
        if (path.isEmpty()) path.push(antHome);
        val currentPosition = path.peek();
        //пройти шаг или взять еду
        nextStep(currentPosition)
                .ifPresentOrElse(this::goTo, () -> takeFood(currentPosition));
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(withFood ? ANT_WITH_FOOD_COLOR : ANT_COLOR);
        graphics.fillRect(x, y, width, height);

//        for (Rectangle rectangle : path) {
//            if (withFood) {
//                graphics.setColor(Color.ORANGE);
//            } else {
//                graphics.setColor(Color.CYAN);
//            }
//            graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
//        }
    }

    private void goTo(Rectangle nextStep) {
        x = nextStep.x;
        y = nextStep.y;
        if (!withFood) {
            path.push(nextStep);
        }
    }

    /**
     * Определение следующего шага
     *
     * @param currentPosition текущая позиция
     * @return следующая позиция
     */
    private Optional<Rectangle> nextStep(Rectangle currentPosition) {
        //если мы с едой идем домой
        if (withFood) {
            //помечаем текущий квадрат феромонами, что это путь к еде
            markPosition(currentPosition);
            return Optional.of(path.pop());
        } else {
            val possibleNextPositions = getAroundArea(currentPosition)
                    .stream()
                    .filter(position -> HOME_PREDICATE.negate().test(position, antHome)) // не возвращаться домой
                    .filter(position -> ANT_PREDICATE.negate().test(position, world.getAnts())) //не ходить по товарищам
                    .collect(toList());
            if (possibleNextPositions.isEmpty()) {
                return Optional.of(currentPosition); //стоим
            }
            //если рядом еда - возвращаем пустой следующий шаг
            if (foundFood(possibleNextPositions)) {
                return Optional.empty();
            }
            //если рядом есть наиболее пахучий квадрат - идет туда
            return getMaxAttractivePosition(possibleNextPositions, currentPosition)
                    //иначе идем в случайном направлении
                    .or(() -> Optional.of(possibleNextPositions.get(nextInt(0, possibleNextPositions.size()))));
        }
    }

    private Optional<Rectangle> getMaxAttractivePosition(
            final List<Rectangle> possibleNextPositions,
            final Rectangle currentPosition
    ) {
        return possibleNextPositions.stream()
                .filter(
                        position -> world.getMarkedAreas().entrySet().stream()
                                .anyMatch(entry -> entry.getKey().intersects(position))
                ).max(comparing(position -> antHome.getLocation().distance(position.getLocation())));
    }

    private void markPosition(Rectangle position) {
        int mark = MARK_WEIGHT;
        if (!world.getMarkedAreas().containsKey(position)) {
            world.getMarkedAreas().put(position, mark);
        } else {
            Integer m = world.getMarkedAreas().get(position);
            world.getMarkedAreas().put(position, Math.min(m + mark, MAX_MARK_WEIGHT));
        }
    }

    private boolean foundFood(final List<Rectangle> possibleNextPositions) {
        return possibleNextPositions.stream()
                .anyMatch(
                        possiblePosition -> world.getFoods().stream()
                                .anyMatch(food -> food.intersects(possiblePosition))
                );
    }

    /**
     * Список возможных следующих шагов в непосредственной близости от текущей позиции
     *
     * @param currentPosition текущая позиция
     * @return список возможных шагов
     */
    private List<Rectangle> getAroundArea(final Rectangle currentPosition) {
        val result = asList(
                new Rectangle(currentPosition.x - width, currentPosition.y - height, width, height),
                new Rectangle(currentPosition.x, currentPosition.y - height, width, height),
                new Rectangle(currentPosition.x + width, currentPosition.y - height, width, height),
                new Rectangle(currentPosition.x + width, currentPosition.y, width, height),
                new Rectangle(currentPosition.x + width, currentPosition.y + height, width, height),
                new Rectangle(currentPosition.x, currentPosition.y + height, width, height),
                new Rectangle(currentPosition.x - width, currentPosition.y + height, width, height),
                new Rectangle(currentPosition.x - width, currentPosition.y, width, height)
        );

        //noinspection ConstantConditions
        return result.stream()
                .filter(rectangle -> !path.peek().intersects(rectangle)) // не предыдущая позиция
                .filter(rectangle -> !currentPosition.intersects(rectangle)) //не текущая позиция
                .filter(rectangle -> world.isInWorld(rectangle.x, rectangle.y)) // не за границей мира
                .collect(toList());
    }

    /**
     * Поднимаем ближний кусок еды и стираем его из мира
     *
     * @param currentPosition текущая позиция
     */
    private void takeFood(Rectangle currentPosition) {
        world.getFoods()
                .stream()
                .filter(food -> getAroundArea(currentPosition).stream().anyMatch(r -> r.intersects(food)))
                .findFirst()
                .ifPresent(food -> {
                    if (food.getFoodCount() > 0) {
                        food.takeOnePeace();
                    } else {
                        world.getFoods().remove(food);
                    }
                    getAroundArea(food).forEach(this::markPosition);
                });
        withFood = true; //помечаем себя как мы с едой
        path = shortPathToHome(currentPosition);
    }

    /**
     * Определение кратчайшего пути до дома
     *
     * @param currentPosition текущая позиция
     * @return путь домой
     */
    private Deque<Rectangle> shortPathToHome(final Rectangle currentPosition) {
        val result = new LinkedList<Rectangle>();
        while (result.isEmpty() || !antHome.intersects(result.getLast())) {
            val position = result.isEmpty() ? currentPosition : result.getLast();

            //Находим ближайшую к дому позицию и добавляем в стек пути до дома
            getAroundArea(position).stream()
                    .min(comparing(rectangle -> antHome.getLocation().distance(rectangle.getLocation())))
                    .ifPresent(result::addLast);
        }

        return result;
    }
}
