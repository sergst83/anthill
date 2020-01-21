package ru.sergst.anthill.config;

import java.awt.*;

public final class Constants {
    private Constants() {}

    /**
     * Action delay ms
     */
    public static int delay = 1;

    public static int maxAntCount = 1;

    public static final int WORLD_WIDTH = 400;
    public static final int WORLD_HEIGHT = 300;
    public static final Color backGround = Color.WHITE;
    public static final Color antColor = Color.BLACK;
    public static final Color antHomeColor = Color.GRAY;

    public static final int entityWidth = 5;
    public static final int entityHeight = 5;

    public static final int foodsPerAntMultiplier = 5;
    public static final int foodPointsCount = 5;

    public static final int marWaight = 10;
    public static final int markRemovePerTick = 1;
}
