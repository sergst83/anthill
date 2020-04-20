package ru.sergst.anthill.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import static ru.sergst.anthill.config.Constants.antColor;
import static ru.sergst.anthill.config.Constants.antWithFoodColor;
import static ru.sergst.anthill.config.Constants.markWaight;
import static ru.sergst.anthill.config.Constants.maxMarkWaight;

@ToString
@Getter
public class Ant extends Rectangle implements Entity {

    /**
     * Путь из прямоугольников пройденный муравбем от дома, чтобы вернуться назад
     */
    private Stack<Rectangle> path;
    private World world;
    @Setter
    private boolean withFood = false;
    private AntHome antHome;
    private int pathLength = 1;

    public Ant(AntHome antHome, World world) {
        super(antHome.x, antHome.y, antHome.width, antHome.height);
        path = new Stack<>();
        path.push(antHome);
        this.world = world;
        this.antHome = antHome;
    }

    @Override
    public void compute() {
        if (path.isEmpty()) path.push(antHome);
        Rectangle currentPosition = path.peek();
        //посмотреть куда идти
        Optional<Rectangle> next = nextStep(currentPosition);
        //пройти шаг или взять еду
        next.ifPresentOrElse(this::goTo, () -> takeFood(currentPosition));
    }

    @Override
    public void draw(Graphics graphics) {
        Color color;
        if (withFood) {
            color = antWithFoodColor;
        } else {
            color = antColor;
        }
        graphics.setColor(color);
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

    private Optional<Rectangle> nextStep(Rectangle currentPosition) {
        //если мы с едой идем по своим следам назад
        if (withFood) {
            //помечаем текущий квадрат что это путь к еде
            markPosition(currentPosition);
            return Optional.of(path.pop());
        } else {
            List<Rectangle> possibleNextPositions = getAroundArea(currentPosition)
                    .stream()
                    .filter(position -> !position.intersects(antHome))
                    .collect(Collectors.toList());
            //если рядом еда - возвращаем пустой следующий шах
            if (possibleNextPositions.stream()
                    .anyMatch(rectangle -> world.getEntities()
                            .get(Food.class)
                            .stream()
                            .anyMatch(f -> f.intersects(rectangle)))) {
                return Optional.empty();
            }
            //если рядом есть наиболее пахучий квадрат идет туда
            Optional<Rectangle> m = getMaxAttractivePosition(possibleNextPositions, currentPosition);
            if (m.isPresent()) {
                return m;
            }
            //иначе идем в случайном направлении
            int index = World.random.ints(0, possibleNextPositions.size()).findAny().getAsInt();
            return Optional.of(possibleNextPositions.get(index));
        }
    }

    private Optional<Rectangle> getMaxAttractivePosition(List<Rectangle> list, Rectangle currentPosition) {
        Optional<Rectangle> r = Optional.empty();
        int value = 0;
        for (Rectangle rectangle : list) {
                Integer marked = world.getMarkedAreas()
                        .entrySet()
                        .stream()
                        .filter(entry -> entry.getKey().intersects(rectangle) && entry.getValue() > 0)
                        .map(Map.Entry::getValue)
                        .max(Comparator.comparingInt(Integer::intValue))
                        .orElse(0);
                if (marked > value) {
                    r = Optional.of(rectangle);
                }
        }
        return r;
    }

    private void markPosition(Rectangle position) {
        int mark = markWaight;
        if (!world.getMarkedAreas().containsKey(position)) {
            world.getMarkedAreas().put(position, mark);
        } else {
            Integer m = world.getMarkedAreas().get(position);
            world.getMarkedAreas().put(position, Math.min(m + mark, maxMarkWaight));
        }
    }

    private List<Rectangle> getAroundArea(Rectangle currentPosition) {
        List<Rectangle> result = Arrays.asList(
                new Rectangle(currentPosition.x - width, currentPosition.y - height, width, height),
                new Rectangle(currentPosition.x, currentPosition.y - height, width, height),
                new Rectangle(currentPosition.x + width, currentPosition.y - height, width, height),
                new Rectangle(currentPosition.x + width, currentPosition.y, width, height),
                new Rectangle(currentPosition.x + width, currentPosition.y + height, width, height),
                new Rectangle(currentPosition.x, currentPosition.y + height, width, height),
                new Rectangle(currentPosition.x - width, currentPosition.y + height, width, height),
                new Rectangle(currentPosition.x - width, currentPosition.y, width, height)
        );

        return result.stream()
                .filter(r -> !currentPosition.intersects(r))
//                .filter(r -> !antHome.intersects(r))
                .filter(r -> world.isInWorld(r.x, r.y))
                .collect(Collectors.toList()); //убираем из коллекции квадраты, откуда мы пришли сюда
    }

    private void takeFood(Rectangle currentPosition) {
        //поднимаем кусок еды и стираем его из мира
        world.getEntities().get(Food.class)
                .stream()
                .filter(f -> getAroundArea(currentPosition).stream().anyMatch(r -> r.intersects(f.getBounds2D())))
                .findFirst()
                .ifPresent(f -> {
                    int foods = ((Food) f).getFoodCount();
                    if (foods > 0) {
                        ((Food) f).takeOnePeace();
                    } else {
                        world.getEntities().get(Food.class).remove(f);
                    }
                    getAroundArea(f.getBounds()).forEach(this::markPosition);
                });
        //помечаем себя как мы с едой
        withFood = true;
        //вычисляем кратчайший путь, по которому будем возвращаться
        path = shortPathToHome(currentPosition);
    }

    private Stack<Rectangle> shortPathToHome(Rectangle currentPosition) {
        Stack<Rectangle> resultQueue = new Stack<>();
        while (resultQueue.isEmpty() || !antHome.intersects(resultQueue.peek())) {
            Rectangle position = resultQueue.isEmpty() ? currentPosition : resultQueue.peek();

            //Смотрим вокруг
            List<Rectangle> around = getAroundArea(position);

            //Находим ближайщую к дому позицию
            around.stream()
                    .min(Comparator.comparing(rectangle -> antHome.getLocation().distance(rectangle.getLocation())))
                    .ifPresent(resultQueue::push);
        }

        //сортируем по удаленности от текущей точки по возрастнию.
        Stack<Rectangle> pathToHome = new Stack<>();
        while (!resultQueue.isEmpty()) {
            pathToHome.push(resultQueue.pop());
        }
        return pathToHome;
    }
}
