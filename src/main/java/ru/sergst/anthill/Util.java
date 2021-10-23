package ru.sergst.anthill;

import lombok.experimental.UtilityClass;

import java.awt.*;
import java.security.SecureRandom;
import java.util.Random;

import static ru.sergst.anthill.config.Constants.*;
import static ru.sergst.anthill.config.Constants.ENTITY_HEIGHT;

@UtilityClass
public final class Util {

    public static final Random random = new SecureRandom();

    /**
     * Возвращает псевдослучайное число из диапазона между min (включительно) и max (исключительно)
     *
     * @param min минимальное значение диапазона
     * @param max максимальное значение диапазона
     * @return псевдослучайное число
     */
    public static int nextInt(final int min, final int max) {
        return random.nextInt(max - min) + min;
    }

    public static Point getRandomPont() {
        return new Point(
                nextInt(1, WORLD_WIDTH - ENTITY_WIDTH),
                nextInt(1, WORLD_HEIGHT - ENTITY_HEIGHT)
        );
    }
}
