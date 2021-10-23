package ru.sergst.anthill.config;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.awt.Color.*;

@UtilityClass
public final class Constants {
    /**
     * Action delay ms
     */
    public static int DELAY = 10;

    public static int MAX_ANT_COUNT = 20;

    public static final int WORLD_WIDTH = 400;
    public static final int WORLD_HEIGHT = 400;

    public static final Color BACKGROUND_COLOR = WHITE;
    public static final Color ANT_COLOR = BLACK;
    public static final Color ANT_WITH_FOOD_COLOR = GREEN;
    public static final Color ANT_HOME_COLOR = RED;

    public static final int ENTITY_WIDTH = 5;
    public static final int ENTITY_HEIGHT = 5;

    public static final Dimension ENTITY_DIMENSION = new Dimension(ENTITY_WIDTH, ENTITY_HEIGHT);

    public static final int FOODS_PER_ANT_MULTIPLIER = 5;
    public static final int FOOD_POINTS_COUNT = 5;

    public static final int MARK_REMOVE_PER_TICK_PERCENT = 1;
    public static final int MARK_WEIGHT = 255;
    public static final int MAX_MARK_WEIGHT = MARK_WEIGHT * MAX_ANT_COUNT;

    public static final Function<Integer, Integer> MARK_REMOVE_FUNCTION =
            value -> (int) (value.doubleValue() - MARK_REMOVE_PER_TICK_PERCENT * value.doubleValue() / 100);
}
