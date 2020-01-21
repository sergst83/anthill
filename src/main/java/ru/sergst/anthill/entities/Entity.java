package ru.sergst.anthill.entities;

import java.awt.*;

public interface Entity extends Shape {
    /**
     * Вычисляем следующее состояние сущности
     */
    void compute();

    /**
     * Рисуем сущность
     * @param graphics объект
     */
    void draw(Graphics graphics);
}
