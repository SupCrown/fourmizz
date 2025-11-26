package main.java.model;

import java.util.ArrayList;
import java.util.List;

public class Cell {
    private Position position;
    private CellType type;
    private int foodAmount;
    private List<Ant> ants;
    private double pheromoneFood;
    private double pheromoneNest;

    public Cell(Position position, CellType type) {
        this.position = position;
        this.type = type;
        this.foodAmount = 0;
        this.ants = new ArrayList<>();
        this.pheromoneFood = 0.0;
        this.pheromoneNest = 0.0;
    }

    public boolean isWalkable() {
        return type == CellType.EMPTY || type == CellType.FOOD ||
                type == CellType.NEST || type == CellType.DEADLY;
    }

    public void addAnt(Ant ant) {
        if (!ants.contains(ant)) {
            ants.add(ant);
        }
    }

    public void removeAnt(Ant ant) {
        ants.remove(ant);
    }

    public List<Ant> getAnts() {
        return new ArrayList<>(ants);
    }

    public boolean hasFood() {
        return type == CellType.FOOD && foodAmount > 0;
    }

    public int collectFood(int amount) {
        int collected = Math.min(amount, foodAmount);
        foodAmount -= collected;
        return collected;
    }

    public void addPheromoneFood(double amount) {
        pheromoneFood = Math.min(100.0, pheromoneFood + amount);
    }

    public void addPheromoneNest(double amount) {
        pheromoneNest = Math.min(100.0, pheromoneNest + amount);
    }

    public void evaporatePheromones(double evaporationRate) {
        pheromoneFood *= (1.0 - evaporationRate);
        pheromoneNest *= (1.0 - evaporationRate);

        if (pheromoneFood < 0.01) pheromoneFood = 0.0;
        if (pheromoneNest < 0.01) pheromoneNest = 0.0;
    }

    // Getters et Setters
    public Position getPosition() {
        return position;
    }

    public CellType getType() {
        return type;
    }

    public void setType(CellType type) {
        this.type = type;
    }

    public int getFoodAmount() {
        return foodAmount;
    }

    public void setFoodAmount(int foodAmount) {
        this.foodAmount = Math.max(0, foodAmount);
    }

    public double getPheromoneFood() {
        return pheromoneFood;
    }

    public void setPheromoneFood(double pheromoneFood) {
        this.pheromoneFood = Math.max(0.0, Math.min(100.0, pheromoneFood));
    }

    public double getPheromoneNest() {
        return pheromoneNest;
    }

    public void setPheromoneNest(double pheromoneNest) {
        this.pheromoneNest = Math.max(0.0, Math.min(100.0, pheromoneNest));
    }
}
