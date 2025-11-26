package model;

import java.util.ArrayList;
import java.util.List;

public class Environment {
    public int[][] gridType; // 0:Empty, 1:Wall, 2:Nest, 3:Deadly
    public int[][] foodGrid; // Quantité de nourriture

    public QTable qTableFood; // Utilisé quand on cherche Food
    public QTable qTableNest; // Utilisé quand on cherche Nid

    public List<Ant> ants;
    public List<Ant> deadAnts;

    private int nestX, nestY;
    private int totalFoodCollected = 0;

    public Environment() {
        gridType = new int[Constants.GRID_WIDTH][Constants.GRID_HEIGHT];
        foodGrid = new int[Constants.GRID_WIDTH][Constants.GRID_HEIGHT];
        qTableFood = new QTable(Constants.GRID_WIDTH, Constants.GRID_HEIGHT);
        qTableNest = new QTable(Constants.GRID_WIDTH, Constants.GRID_HEIGHT);
        ants = new ArrayList<>();
        deadAnts = new ArrayList<>();

        initMap(); // Créer la carte du PDF
    }

    private void initMap() {
        // Initialisation basique (Tout vide)
        for(int x=0; x<Constants.GRID_WIDTH; x++) {
            for(int y=0; y<Constants.GRID_HEIGHT; y++) {
                gridType[x][y] = 0;
                foodGrid[x][y] = 0;
            }
        }

        // Placement Nid (au centre approx pour l'exemple)
        nestX = 5; nestY = 5;
        gridType[nestX][nestY] = 2; // Nid

        // Mur exemple (basé sur le PDF "Mur Nourriture")
        for(int i=10; i<15; i++) gridType[i][10] = 1;

        // Zone mortelle
        gridType[12][12] = 3;

        // Nourriture 1 (20000)
        foodGrid[15][5] = 20000;

        // Nourriture 2 (1000)
        foodGrid[15][20] = 1000;

        // Ajouter les fourmis initiales au nid
        addAntsAtNest(2, Ant.Type.EXPLORER);
        addAntsAtNest(1, Ant.Type.SOLDIER);
        addAntsAtNest(3, Ant.Type.HARVESTER);
    }

    private void addAntsAtNest(int count, Ant.Type type) {
        for(int i=0; i<count; i++) {
            ants.add(new Ant(ants.size(), nestX, nestY, type));
        }
    }

    public boolean isValid(int x, int y) {
        return x >= 0 && x < Constants.GRID_WIDTH && y >= 0 && y < Constants.GRID_HEIGHT;
    }

    public boolean isWall(int x, int y) { return gridType[x][y] == 1; }
    public boolean isDeadly(int x, int y) { return gridType[x][y] == 3; }
    public boolean isNest(int x, int y) { return gridType[x][y] == 2; }
    public boolean hasFood(int x, int y) { return foodGrid[x][y] > 0; }

    public int takeFood(int x, int y, int capacity) {
        int available = foodGrid[x][y];
        int taken = Math.min(available, capacity);
        foodGrid[x][y] -= taken;
        return taken;
    }

    public void dropFoodAtNest(int amount) {
        totalFoodCollected += amount;
    }

    public void killAnt(Ant a) {
        deadAnts.add(a);
    }

    public void cleanupDeadAnts() {
        ants.removeAll(deadAnts);
        deadAnts.clear();
    }

    // Helper pour les fourmis
    public boolean[] getPossibleMoves(int x, int y) {
        boolean[] moves = new boolean[4];
        int[][] dirs = {{0,-1}, {0,1}, {-1,0}, {1,0}};
        for(int i=0; i<4; i++) {
            int nx = x + dirs[i][0];
            int ny = y + dirs[i][1];
            moves[i] = isValid(nx, ny) && !isWall(nx, ny);
        }
        return moves;
    }

    public int getTotalFoodCollected() { return totalFoodCollected; }
}
