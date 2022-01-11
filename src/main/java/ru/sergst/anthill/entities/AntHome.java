package ru.sergst.anthill.entities;

import java.awt.*;

import static ru.sergst.anthill.config.Constants.ANT_HOME_COLOR;

public class AntHome extends Rectangle implements Entity {

    public AntHome(final Point point, final Dimension dimension) {
        super(point, dimension);
    }

    @Override
    public void compute() {
        //do nothing
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(ANT_HOME_COLOR);
        graphics.fillRect(x, y, width, height);
    }
}
