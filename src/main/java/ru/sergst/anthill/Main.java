package ru.sergst.anthill;

import ru.sergst.anthill.entities.World;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame f = new JFrame("Ant hill");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setResizable(false);
        f.setLayout(new BorderLayout());
        f.add(new World(), BorderLayout.CENTER);
        f.setLocationRelativeTo(null);
        f.pack();
        f.setVisible(true);
    }
}
