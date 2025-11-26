package model;

import javax.swing.*;
import java.awt.*;

public class GUI extends JPanel {
    private Environment env;
    private int cellSize = 20;

    public GUI(Environment env) {
        this.env = env;
        this.setPreferredSize(new Dimension(Constants.GRID_WIDTH * cellSize, Constants.GRID_HEIGHT * cellSize));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. Dessiner la grille
        for (int x = 0; x < Constants.GRID_WIDTH; x++) {
            for (int y = 0; y < Constants.GRID_HEIGHT; y++) {
                int px = x * cellSize;
                int py = y * cellSize;

                // Fond selon type
                if (env.gridType[x][y] == 1) g.setColor(Color.DARK_GRAY); // Mur
                else if (env.gridType[x][y] == 2) g.setColor(Color.BLUE); // Nid
                else if (env.gridType[x][y] == 3) g.setColor(Color.RED);  // Danger
                else g.setColor(Color.WHITE); // Vide

                // Nourriture
                if (env.foodGrid[x][y] > 0) {
                    g.setColor(Color.GREEN);
                }

                g.fillRect(px, py, cellSize, cellSize);
                g.setColor(Color.LIGHT_GRAY);
                g.drawRect(px, py, cellSize, cellSize);

                // Visualisation phéromones (Optionnel : opacité)
                // Ici on simplifie, on ne dessine pas les chiffres des Q-Values pour la lisibilité
            }
        }

        // 2. Dessiner les fourmis
        for (Ant ant : env.ants) {
            int px = ant.getX() * cellSize;
            int py = ant.getY() * cellSize;

            if (ant.getType() == Ant.Type.EXPLORER) g.setColor(new Color(100, 100, 255));
            else if (ant.getType() == Ant.Type.SOLDIER) g.setColor(new Color(255, 100, 100));
            else g.setColor(Color.BLACK); // Harvester

            // Si elle porte à manger -> cercle plein, sinon cercle vide
            if(ant.getState() == Ant.State.RETURNING)
                g.fillOval(px+4, py+4, cellSize-8, cellSize-8);
            else
                g.drawOval(px+4, py+4, cellSize-8, cellSize-8);
        }

        g.setColor(Color.BLACK);
        g.drawString("Nourriture Nid: " + env.getTotalFoodCollected(), 10, 20);
    }
}
