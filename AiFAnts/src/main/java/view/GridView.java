package view;

import main.java.model.*;
import main.java.controller.Simulation;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class GridView extends JPanel {
    private Simulation simulation;
    private int cellSize = 20;

    public GridView(Simulation simulation) {
        this.simulation = simulation;
        setBackground(Color.WHITE);
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (simulation == null) {
            return;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Grid grid = simulation.getGrid();

        // Dessiner les cellules
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                Position pos = new Position(x, y);
                Cell cell = grid.getCell(pos);

                drawCell(g2d, cell, x, y);
            }
        }

        // Dessiner les fourmis
        for (Ant ant : simulation.getAnts()) {
            drawAnt(g2d, ant);
        }

        // Dessiner la grille
        g2d.setColor(new Color(200, 200, 200, 100));
        for (int x = 0; x <= grid.getWidth(); x++) {
            g2d.drawLine(x * cellSize, 0, x * cellSize, grid.getHeight() * cellSize);
        }
        for (int y = 0; y <= grid.getHeight(); y++) {
            g2d.drawLine(0, y * cellSize, grid.getWidth() * cellSize, y * cellSize);
        }
    }

    private void drawCell(Graphics2D g2d, Cell cell, int x, int y) {
        int px = x * cellSize;
        int py = y * cellSize;

        Color cellColor;
        switch (cell.getType()) {
            case WALL:
                cellColor = Color.DARK_GRAY;
                break;
            case NEST:
                cellColor = new Color(100, 200, 100);
                break;
            case FOOD:
                int foodAmount = cell.getFoodAmount();
                int intensity = Math.min(255, 150 + foodAmount * 10);
                cellColor = new Color(255, intensity, 0);
                break;
            case DEADLY:
                cellColor = new Color(200, 50, 50);
                break;
            default:
                cellColor = Color.WHITE;
        }

        g2d.setColor(cellColor);
        g2d.fillRect(px, py, cellSize, cellSize);

        // Afficher la quantité de nourriture
        if (cell.getType() == CellType.FOOD && cell.getFoodAmount() > 0) {
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String text = String.valueOf(cell.getFoodAmount());
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            g2d.drawString(text, px + (cellSize - textWidth) / 2, py + cellSize / 2 + 4);
        }
    }

    private void drawAnt(Graphics2D g2d, Ant ant) {
        Position pos = ant.getPosition();
        int px = pos.getX() * cellSize + cellSize / 2;
        int py = pos.getY() * cellSize + cellSize / 2;
        int size = 6;

        Color antColor;
        switch (ant.getType()) {
            case EXPLORER:
                antColor = Color.BLUE;
                break;
            case WORKER:
                antColor = Color.ORANGE;
                break;
            case SOLDIER:
                antColor = Color.RED;
                break;
            default:
                antColor = Color.BLACK;
        }

        if (ant.hasFood()) {
            // Fourmi avec nourriture - cercle plein
            g2d.setColor(antColor);
            g2d.fillOval(px - size, py - size, size * 2, size * 2);
            g2d.setColor(Color.YELLOW);
            g2d.fillOval(px - size/2, py - size/2, size, size);
        } else {
            // Fourmi sans nourriture - cercle vide
            g2d.setColor(antColor);
            g2d.fillOval(px - size, py - size, size * 2, size * 2);
        }

        // Barre d'énergie
        int energyBarWidth = cellSize - 4;
        int energyBarHeight = 3;
        int energyWidth = (int) ((ant.getEnergy() / (double) ant.getMaxEnergy()) * energyBarWidth);

        int bx = pos.getX() * cellSize + 2;
        int by = pos.getY() * cellSize + 2;

        g2d.setColor(Color.BLACK);
        g2d.drawRect(bx, by, energyBarWidth, energyBarHeight);

        if (ant.getEnergy() > ant.getMaxEnergy() * 0.5) {
            g2d.setColor(Color.GREEN);
        } else if (ant.getEnergy() > ant.getMaxEnergy() * 0.25) {
            g2d.setColor(Color.ORANGE);
        } else {
            g2d.setColor(Color.RED);
        }
        g2d.fillRect(bx + 1, by + 1, energyWidth - 1, energyBarHeight - 1);
    }

    @Override
    public Dimension getPreferredSize() {
        if (simulation != null) {
            Grid grid = simulation.getGrid();
            return new Dimension(grid.getWidth() * cellSize,
                    grid.getHeight() * cellSize);
        }
        return new Dimension(600, 400);
    }
}
