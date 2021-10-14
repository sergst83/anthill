package ru.sergst.anthill;

import lombok.experimental.UtilityClass;

import java.security.SecureRandom;
import java.util.Random;

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
}
