package model;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Environment {
    public CellType[][] grid;
    public int[][] foodAmounts; // Nouveau tableau
    // Liste synchronisée pour éviter les conflits entre le dessin (GUI) et le mouvement (Thread)
    public List<Ant> ants = Collections.synchronizedList(new ArrayList<>());

    public QTable qTableFood;
    public QTable qTableNest;

    public int nestX, nestY;
    public int foodCollected = 0;

    public Environment() {
        grid = new CellType[Constants.GRID_WIDTH][Constants.GRID_HEIGHT];
        foodAmounts = new int[Constants.GRID_WIDTH][Constants.GRID_HEIGHT];
        qTableFood = new QTable();
        qTableNest = new QTable();
        initMap();
    }

    private void initMap() {
        // Initialisation vide
        for (int x = 0; x < Constants.GRID_WIDTH; x++) {
            for (int y = 0; y < Constants.GRID_HEIGHT; y++) {
                grid[x][y] = CellType.EMPTY;
            }
        }
        // Murs extérieurs
        for (int x = 0; x < Constants.GRID_WIDTH; x++) {
            grid[x][0] = CellType.WALL;
            grid[x][Constants.GRID_HEIGHT - 1] = CellType.WALL;
        }
        for (int y = 0; y < Constants.GRID_HEIGHT; y++) {
            grid[0][y] = CellType.WALL;
            grid[Constants.GRID_WIDTH - 1][y] = CellType.WALL;
        }

        // Nid
        nestX = 5; nestY = 5;
        grid[nestX][nestY] = CellType.NEST;

        // Nourriture
        spawnFoodCluster(Constants.GRID_WIDTH - 10, Constants.GRID_HEIGHT - 10);
        addRandomObstacles();
    }

    private void spawnFoodCluster(int centerX, int centerY) {
        for (int x = centerX ; x <= centerX ; x++) {
            for (int y = centerY ; y <= centerY ; y++) {
                if (isValid(x, y)) grid[x][y] = CellType.FOOD;
            }
        }
    }

    private void addRandomObstacles() {
        Random rand = new Random();
        for(int i=0; i<30; i++) {
            int x = rand.nextInt(Constants.GRID_WIDTH - 10) + 5;
            int y = rand.nextInt(Constants.GRID_HEIGHT - 10) + 5;
            if(grid[x][y] == CellType.EMPTY) grid[x][y] = CellType.WALL;
        }
    }

    // --- Gestion des Fourmis ---

    public void spawnWave(int count, Ant.Type type) {
        for (int i = 0; i < count; i++) {
            ants.add(new Ant(nestX, nestY, type));
        }
        System.out.println("Vague lancée : " + count + " " + type);
    }

    public void cleanupDeadAnts() {
        ants.removeIf(ant -> ant.isDead);
    }

    // --- Helpers de Navigation ---

    public boolean isValid(int x, int y) {
        return x >= 0 && x < Constants.GRID_WIDTH && y >= 0 && y < Constants.GRID_HEIGHT;
    }



    public boolean[] getPossibleActions(int x, int y) {
        boolean[] possible = new boolean[4];
        possible[0] = isWalkable(x, y - 1); // Haut
        possible[1] = isWalkable(x, y + 1); // Bas
        possible[2] = isWalkable(x - 1, y); // Gauche
        possible[3] = isWalkable(x + 1, y); // Droite
        return possible;
    }

    private boolean isWalkable(int x, int y) {
        return isValid(x, y) && grid[x][y] != CellType.WALL;
    }

    // --- SAUVEGARDE ET CHARGEMENT (Appelés par GUI) ---

    public void saveState() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("brain.dat"))) {
            out.writeObject(qTableFood);
            out.writeObject(qTableNest);
            System.out.println("Sauvegarde effectuée dans brain.dat");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadState() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("brain.dat"))) {
            this.qTableFood = (QTable) in.readObject();
            this.qTableNest = (QTable) in.readObject();
            System.out.println("Chargement réussi !");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Erreur de chargement (fichier inexistant ?) : " + e.getMessage());
        }
    }

    // 1. Ajouter de la nourriture à la souris
    public void setCell(int x, int y, CellType type) {
        if (isValid(x, y) && grid[x][y] != CellType.NEST) {
            grid[x][y] = type;
            if (type == CellType.FOOD) {
                foodAmounts[x][y] = 100000; // <--- Chaque clic pose 5 unités de nourriture
            } else {
                foodAmounts[x][y] = 0;
            }
        }
    }

    // 2. Tuer les fourmis sur une case (Mode Tueur)
    public void killAntsAt(int x, int y) {
        synchronized (ants) {
            for (Ant a : ants) {
                // Si la fourmi est sur la case (avec une petite marge pour la souris)
                if (Math.abs(a.x - x) <= 1 && Math.abs(a.y - y) <= 1) {
                    a.isDead = true;
                }
            }
        }
    }

    // 3. Faire apparaître de la nourriture aléatoirement (Bouton Magic)
    public void spawnRandomFood(int amount) {
        java.util.Random rand = new java.util.Random();
        int placed = 0;
        while (placed < amount) {
            int x = rand.nextInt(Constants.GRID_WIDTH);
            int y = rand.nextInt(Constants.GRID_HEIGHT);
            if (grid[x][y] == CellType.EMPTY) {
                setCell(x, y, CellType.FOOD); // Utilise setCell pour mettre la qté à 5
                placed++;
            }
        }
    }

}
