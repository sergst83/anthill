package ru.sergst.anthill.entities;

import lombok.Getter;
import lombok.Setter;
import ru.sergst.anthill.config.Constants;

import java.awt.*;

@Getter
@Setter
public class Food extends Rectangle implements Entity {

    private int foodCount = Constants.maxAntCount * Constants.foodsPerAntMultiplier;

    public Food(int x, int y, int width, int height) {
        super(x, y, width, height);
    }

    @Override
    public void compute() {

    }

    @Override
    public void draw(Graphics graphics) {
        graphics.setColor(Color.MAGENTA);
        graphics.fillRect(x, y, width, height);
    }
}
