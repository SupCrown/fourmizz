package main.java.model;

import java.util.ArrayList;
import java.util.List;

public class Grid {
    private int width;
    private int height;
    private Cell[][] cells;
    private Position nestPosition;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];

        // Initialiser toutes les cellules
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(new Position(x, y), CellType.EMPTY);
            }
        }
    }

    public boolean isValid(Position pos) {
        return pos.getX() >= 0 && pos.getX() < width &&
                pos.getY() >= 0 && pos.getY() < height;
    }

    public Cell getCell(Position pos) {
        if (!isValid(pos)) {
            return null;
        }
        return cells[pos.getX()][pos.getY()];
    }

    public CellType getCellType(Position pos) {
        Cell cell = getCell(pos);
        return cell != null ? cell.getType() : null;
    }

    public void setCellType(Position pos, CellType type) {
        Cell cell = getCell(pos);
        if (cell != null) {
            cell.setType(type);
            if (type == CellType.NEST) {
                nestPosition = pos;
            }
        }
    }

    public boolean isWalkable(Position pos) {
        Cell cell = getCell(pos);
        return cell != null && cell.isWalkable();
    }

    public List<Position> getNeighbors(Position pos) {
        List<Position> neighbors = new ArrayList<>();

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};

        for (int[] dir : directions) {
            Position neighbor = new Position(pos.getX() + dir[0], pos.getY() + dir[1]);
            if (isValid(neighbor)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    public List<Position> getWalkableNeighbors(Position pos) {
        List<Position> neighbors = new ArrayList<>();

        for (Position neighbor : getNeighbors(pos)) {
            if (isWalkable(neighbor)) {
                neighbors.add(neighbor);
            }
        }

        return neighbors;
    }

    public int getManhattanDistance(Position a, Position b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    public void setFoodAmount(Position pos, int amount) {
        Cell cell = getCell(pos);
        if (cell != null) {
            cell.setFoodAmount(amount);
        }
    }

    public int getTotalFood() {
        int total = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cells[x][y].getType() == CellType.FOOD) {
                    total += cells[x][y].getFoodAmount();
                }
            }
        }
        return total;
    }

    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y] = new Cell(new Position(x, y), CellType.EMPTY);
            }
        }
    }

    public void evaporatePheromones(double rate) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                cells[x][y].evaporatePheromones(rate);
            }
        }
    }

    // Getters
    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Position getNestPosition() {
        return nestPosition;
    }

    public Cell[][] getCells() {
        return cells;
    }
}
