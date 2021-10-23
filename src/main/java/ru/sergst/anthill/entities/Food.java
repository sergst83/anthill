package ru.sergst.anthill.entities;

import lombok.Getter;

import java.awt.*;

import static java.awt.Color.MAGENTA;
import static ru.sergst.anthill.config.Constants.FOODS_PER_ANT_MULTIPLIER;
import static ru.sergst.anthill.config.Constants.MAX_ANT_COUNT;

@Getter
public class Food extends Rectangle implements Entity {

    private int foodCount = MAX_ANT_COUNT * FOODS_PER_ANT_MULTIPLIER;

    public Food(final  Point point, final Dimension dimension) {
        super(point, dimension);
    }

    @Override
    public void compute() {
        //do nothing
    }

    @Override
    public void draw(final Graphics graphics) {
        graphics.setColor(MAGENTA);
        graphics.fillRect(x, y, width, height);
    }

    public void takeOnePeace() {
        foodCount -= 1;
    }
}
