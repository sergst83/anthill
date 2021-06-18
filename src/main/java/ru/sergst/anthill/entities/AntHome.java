package ru.sergst.anthill.entities;

import ru.sergst.anthill.config.Constants;

import java.awt.*;

public class AntHome extends Rectangle implements Entity {

    public AntHome(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    public AntHome(final Point point, final Dimension dimension) {
        super(point, dimension);
    }

    @Override
    public void compute() {
    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(Constants.antHomeColor);
        graphics.fillRect(x, y, width, height);
    }
}
