package main.java.model;

import java.util.HashMap;
import java.util.Map;

public class Nest {
    private Position position;
    private int foodCollected;
    private Map<AntType, Integer> availableAnts;

    public Nest(Position position) {
        this.position = position;
        this.foodCollected = 0;
        this.availableAnts = new HashMap<>();

        for (AntType type : AntType.values()) {
            availableAnts.put(type, 0);
        }
    }

    public void addFood(int amount) {
        if (amount > 0) {
            foodCollected += amount;
        }
    }

    public void addAvailableAnt(AntType type, int count) {
        if (count > 0) {
            availableAnts.put(type, availableAnts.get(type) + count);
        }
    }

    public void setAvailableAnts(AntType type, int count) {
        if (count >= 0) {
            availableAnts.put(type, count);
        }
    }

    public boolean removeAvailableAnt(AntType type) {
        int current = availableAnts.get(type);
        if (current > 0) {
            availableAnts.put(type, current - 1);
            return true;
        }
        return false;
    }

    public void returnAnt(AntType type) {
        availableAnts.put(type, availableAnts.get(type) + 1);
    }

    public int getAvailableCount(AntType type) {
        return availableAnts.get(type);
    }

    public boolean hasAvailableAnt(AntType type) {
        return availableAnts.get(type) > 0;
    }

    public int getTotalAvailableAnts() {
        int total = 0;
        for (int count : availableAnts.values()) {
            total += count;
        }
        return total;
    }

    public Position getPosition() {
        return position;
    }

    public int getFoodCollected() {
        return foodCollected;
    }
}
