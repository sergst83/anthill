package ru.sergst.anthill.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.sergst.anthill.config.Constants;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

import static ru.sergst.anthill.config.Constants.marWaight;

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

    public Ant(AntHome antHome, World world) {
        super(antHome.x, antHome.y, antHome.width, antHome.height);
        path = new Stack<>();
        path.push(antHome);
        this.world = world;
        this.antHome = antHome;
    }

    @Override
    public void compute() {
        Rectangle currentPosition = path.peek();
        //посмотреть куда идти
        Optional<Rectangle> next = nextStep(currentPosition);
        next.ifPresentOrElse(this::goTo, () -> takeFood(currentPosition)); //пройти шаг или взять еду
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(Constants.antColor);
        graphics.fillRect(x, y, width, height);

//        for (Rectangle rectangle : path) {
//            if (withFood) {
//                graphics.setColor(Color.ORANGE);
//            } else {
//                graphics.setColor(Color.YELLOW);
//            }
//            graphics.fillRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
//        }
    }

    private void goTo(Rectangle nextStep) {
        x = nextStep.x;
        y = nextStep.y;
        if(!withFood) {
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
            List<Rectangle> possibleNextPositions = getAroundArea(currentPosition);
            //если рядом есть наиболее пахучий квадрат идет туда
            Optional<Rectangle> m = getMaxAttractivePosition(possibleNextPositions);
            if (m.isPresent()) {
                return m;
            }
            //если рядом еда - возвращаем пустой следующий шах
            if (possibleNextPositions.stream()
                    .anyMatch(rectangle -> world.getEntities()
                            .get(Food.class)
                            .stream()
                            .anyMatch(f -> f.intersects(rectangle)))) {
                return Optional.empty();
            }
            //иначе идем в случайном направлении
            int index = World.random.ints(0, possibleNextPositions.size()).findAny().getAsInt();
            return Optional.of(possibleNextPositions.get(index));
        }
    }

    private Optional<Rectangle> getMaxAttractivePosition(List<Rectangle> list) {
        Optional<Rectangle> r = Optional.empty();
        int value = 0;
        for (Rectangle rectangle : list) {
            Integer marked = world.getMarkedAreas()
                    .keySet()
                    .stream()
                    .filter(a -> a.intersects(rectangle))
                    .map(a -> world.getMarkedAreas().get(a))
                    .findFirst()
                    .orElse(0);
            if (marked > value) {
                r = Optional.of(rectangle);
            }
        }
        return r;
    }

    private void markPosition(Rectangle position) {
        if (!world.getMarkedAreas().containsKey(position)) {
            world.getMarkedAreas().put(position, marWaight);
        } else {
            Integer m = world.getMarkedAreas().get(position);
            world.getMarkedAreas().put(position, m + marWaight);
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
                .filter(r -> !path.peek().equals(r))
                .filter(r -> !antHome.equals(r))
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
                        ((Food) f).setFoodCount(foods - 1);
                    } else {
                        world.getEntities().get(Food.class).remove(f);
                    }
                });
        //Говорим себе что мы с едой
        withFood = true;
    }
}
