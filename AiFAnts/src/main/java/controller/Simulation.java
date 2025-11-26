package controller;

import java.util.*;
import main.java.model.Nest;
import main.java.model.Grid;
import main.java.model.Ant;
import main.java.model.QTable;
import main.java.model.AntType;
import main.java.model.Cell;
import main.java.model.CellType;
import main.java.model.Position;





public class Simulation {
    private Grid grid;
    private main.java.model.Nest nest;
    private List<Ant> ants;
    private QTable qTableFood;
    private QTable qTableNest;
    private long time;
    private Random random;
    private double pheromoneEvaporationRate;
    private List<ScheduledWave> scheduledWaves;

    private static class ScheduledWave {
        long deployTime;
        AntType type;
        int count;
        double alpha;
        double gamma;
        double epsilon;

        ScheduledWave(long deployTime, AntType type, int count,
                      double alpha, double gamma, double epsilon) {
            this.deployTime = deployTime;
            this.type = type;
            this.count = count;
            this.alpha = alpha;
            this.gamma = gamma;
            this.epsilon = epsilon;
        }
    }

    public Simulation(Grid grid, Nest nest) {
        this.grid = grid;
        this.nest = nest;
        this.ants = new ArrayList<>();
        this.qTableFood = new QTable(grid.getWidth(), grid.getHeight());
        this.qTableNest = new QTable(grid.getWidth(), grid.getHeight());
        this.time = 0;
        this.random = new Random();
        this.pheromoneEvaporationRate = 0.0001;
        this.scheduledWaves = new ArrayList<>();
    }

    public void step() {
        time++;

        // Évaporation des phéromones
        if (time % 10 == 0) {
            grid.evaporatePheromones(pheromoneEvaporationRate);
        }

        // Vérifier les vagues programmées
        checkScheduledWaves();

        // Mettre à jour toutes les fourmis
        List<Ant> antsToRemove = new ArrayList<>();

        for (Ant ant : ants) {
            if (ant.isDead()) {
                antsToRemove.add(ant);
                continue;
            }

            updateAnt(ant);
        }

        // Retirer les fourmis mortes
        for (Ant ant : antsToRemove) {
            removeAnt(ant);
        }
    }

    private void updateAnt(Ant ant) {
        Position currentPos = ant.getPosition();

        // Consommer de l'énergie
        int energyCost = 1;
        if (grid.getCellType(currentPos) == CellType.DEADLY) {
            energyCost = (ant.getType() == AntType.SOLDIER) ? 2 : 5;
        }
        ant.consumeEnergy(energyCost);

        if (ant.isDead()) {
            return;
        }

        // Choisir une action
        QTable qTable = ant.hasFood() ? qTableNest : qTableFood;
        int action = chooseAction(ant, qTable, currentPos);

        // Exécuter l'action
        Position nextPos = getNextPosition(currentPos, action);

        if (grid.isWalkable(nextPos)) {
            // Calculer la récompense
            double reward = calculateReward(ant, currentPos, nextPos);

            // Mettre à jour Q-value
            qTable.updateQValue(currentPos, action, reward, nextPos,
                    ant.getAlpha(), ant.getGamma());

            // Déplacer la fourmi
            moveAnt(ant, nextPos);

            // Actions spéciales à la nouvelle position
            handleAntActions(ant, nextPos);
        }
    }

    private int chooseAction(Ant ant, QTable qTable, Position pos) {
        // Epsilon-greedy
        if (random.nextDouble() < ant.getEpsilon()) {
            // Exploration: action aléatoire
            List<Position> walkableNeighbors = grid.getWalkableNeighbors(pos);
            if (walkableNeighbors.isEmpty()) {
                return random.nextInt(4);
            }
            Position randomNeighbor = walkableNeighbors.get(
                    random.nextInt(walkableNeighbors.size()));
            return getActionForPosition(pos, randomNeighbor);
        } else {
            // Exploitation: meilleure action
            return qTable.getBestAction(pos);
        }
    }

    private double calculateReward(Ant ant, Position currentPos, Position nextPos) {
        double reward = -0.1; // Coût de base du mouvement

        CellType nextType = grid.getCellType(nextPos);

        if (ant.hasFood()) {
            // Cherche le nid
            if (nextType == CellType.NEST) {
                reward = 50.0; // Grande récompense pour ramener la nourriture
            } else {
                // Récompense basée sur la distance au nid
                int distanceBefore = grid.getManhattanDistance(currentPos, nest.getPosition());
                int distanceAfter = grid.getManhattanDistance(nextPos, nest.getPosition());
                reward += (distanceBefore - distanceAfter) * 2.0;
            }
        } else {
            // Cherche la nourriture
            if (nextType == main.java.model.CellType.FOOD) {
                Cell cell = grid.getCell(nextPos);
                if (cell.hasFood()) {
                    reward = 30.0; // Récompense pour trouver de la nourriture
                }
            } else {
                // Pénalité pour s'éloigner du nid sans nourriture
                int distanceBefore = grid.getManhattanDistance(currentPos, nest.getPosition());
                int distanceAfter = grid.getManhattanDistance(nextPos, nest.getPosition());
                if (distanceAfter > distanceBefore) {
                    reward += 0.5; // Petite récompense pour explorer
                }
            }
        }

        // Pénalités pour zones dangereuses
        if (nextType == CellType.DEADLY) {
            reward -= (ant.getType() == AntType.SOLDIER) ? 5.0 : 15.0;
        }

        return reward;
    }

    private void handleAntActions(Ant ant, Position pos) {
        CellType cellType = grid.getCellType(pos);

        if (!ant.hasFood() && cellType == CellType.FOOD) {
            // Collecter de la nourriture
            Cell cell = grid.getCell(pos);
            if (cell.hasFood()) {
                int collected = cell.collectFood(1);
                if (collected > 0) {
                    ant.pickupFood();
                }
            }
        } else if (ant.hasFood() && cellType == CellType.NEST) {
            // Déposer la nourriture
            nest.addFood(1);
            ant.dropFood();
            ant.restoreEnergy(20);
        } else if (cellType == CellType.NEST && !ant.hasFood()) {
            // Restaurer l'énergie au nid
            ant.restoreEnergy(5);
        }
    }

    private void moveAnt(Ant ant, Position newPos) {
        Position oldPos = ant.getPosition();

        // Retirer de l'ancienne cellule
        Cell oldCell = grid.getCell(oldPos);
        if (oldCell != null) {
            oldCell.removeAnt(ant);
        }

        // Ajouter à la nouvelle cellule
        ant.setPosition(newPos);
        Cell newCell = grid.getCell(newPos);
        if (newCell != null) {
            newCell.addAnt(ant);
        }
    }

    private void removeAnt(Ant ant) {
        Position pos = ant.getPosition();
        Cell cell = grid.getCell(pos);
        if (cell != null) {
            cell.removeAnt(ant);
        }
        ants.remove(ant);
        nest.returnAnt(ant.getType());
    }

    private Position getNextPosition(Position pos, int action) {
        int newX = pos.getX();
        int newY = pos.getY();

        switch (action) {
            case QTable.NORTH:
                newY--;
                break;
            case QTable.SOUTH:
                newY++;
                break;
            case QTable.EAST:
                newX++;
                break;
            case QTable.WEST:
                newX--;
                break;
        }

        return new Position(newX, newY);
    }

    private int getActionForPosition(Position from, Position to) {
        int dx = to.getX() - from.getX();
        int dy = to.getY() - from.getY();

        if (dy < 0) return QTable.NORTH;
        if (dy > 0) return QTable.SOUTH;
        if (dx > 0) return QTable.EAST;
        if (dx < 0) return QTable.WEST;

        return QTable.NORTH;
    }

    public void deployAnt(AntType type, double alpha, double gamma, double epsilon) {
        if (nest.removeAvailableAnt(type)) {
            Ant ant = new Ant(type, nest.getPosition(), alpha, gamma, epsilon);
            ants.add(ant);

            Cell nestCell = grid.getCell(nest.getPosition());
            if (nestCell != null) {
                nestCell.addAnt(ant);
            }
        }
    }

    public void scheduleWave(long deployTime, AntType type, int count,
                             double alpha, double gamma, double epsilon) {
        scheduledWaves.add(new ScheduledWave(deployTime, type, count, alpha, gamma, epsilon));
        scheduledWaves.sort(Comparator.comparingLong(w -> w.deployTime));
    }

    private void checkScheduledWaves() {
        Iterator<ScheduledWave> iterator = scheduledWaves.iterator();
        while (iterator.hasNext()) {
            ScheduledWave wave = iterator.next();
            if (wave.deployTime <= time) {
                for (int i = 0; i < wave.count; i++) {
                    deployAnt(wave.type, wave.alpha, wave.gamma, wave.epsilon);
                }
                iterator.remove();
            } else {
                break; // Liste triée, on peut arrêter
            }
        }
    }

    public void clearScheduledWaves() {
        scheduledWaves.clear();
    }

    public void scheduleProgressiveWaves() {
        // Vagues progressives d'explorateurs puis de workers
        scheduleWave(1000, AntType.EXPLORER, 3, 0.2, 0.9, 0.3);
        scheduleWave(3000, AntType.EXPLORER, 5, 0.15, 0.9, 0.2);
        scheduleWave(5000, AntType.WORKER, 5, 0.1, 0.95, 0.15);
        scheduleWave(8000, AntType.WORKER, 10, 0.1, 0.95, 0.1);
        scheduleWave(12000, AntType.SOLDIER, 3, 0.1, 0.9, 0.2);
    }

    public void saveQTables(String basePath) {
        qTableFood.save(basePath + "_food.qtable");
        qTableNest.save(basePath + "_nest.qtable");
    }

    public void loadQTables(String basePath) {
        qTableFood = QTable.load(basePath + "_food.qtable",
                grid.getWidth(), grid.getHeight());
        qTableNest = QTable.load(basePath + "_nest.qtable",
                grid.getWidth(), grid.getHeight());
    }

    public void exportQTablesToCSV(String basePath) {
        qTableFood.exportToCSV(basePath + "_food.csv");
        qTableNest.exportToCSV(basePath + "_nest.csv");
    }

    public void resetQLearning() {
        qTableFood.reset();
        qTableNest.reset();
    }

    public static Simulation generateRandom(int width, int height,
                                            double wallDensity, double deadlyDensity,
                                            int foodSources, int minFood, int maxFood) {
        Grid grid = new Grid(width, height);
        Random random = new Random();

        // Placer le nid
        Position nestPos = new Position(width / 2, height / 2);
        grid.setCellType(nestPos, CellType.NEST);
        Nest nest = new Nest(nestPos);

        // Placer des murs
        int wallCount = (int) (width * height * wallDensity);
        for (int i = 0; i < wallCount; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Position pos = new Position(x, y);
            if (grid.getCellType(pos) == CellType.EMPTY &&
                    grid.getManhattanDistance(pos, nestPos) > 3) {
                grid.setCellType(pos, CellType.WALL);
            }
        }

        // Placer des zones mortelles
        int deadlyCount = (int) (width * height * deadlyDensity);
        for (int i = 0; i < deadlyCount; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Position pos = new Position(x, y);
            if (grid.getCellType(pos) == CellType.EMPTY &&
                    grid.getManhattanDistance(pos, nestPos) > 5) {
                grid.setCellType(pos, CellType.DEADLY);
            }
        }

        // Placer la nourriture
        for (int i = 0; i < foodSources; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            Position pos = new Position(x, y);
            if (grid.getCellType(pos) == CellType.EMPTY &&
                    grid.getManhattanDistance(pos, nestPos) > 5) {
                grid.setCellType(pos, CellType.FOOD);
                int amount = minFood + random.nextInt(maxFood - minFood + 1);
                grid.setFoodAmount(pos, amount);
            }
        }

        return new Simulation(grid, nest);
    }

    // Getters
    public Grid getGrid() {
        return grid;
    }

    public Nest getNest() {
        return nest;
    }

    public List<Ant> getAnts() {
        return new ArrayList<>(ants);
    }

    public long getTime() {
        return time;
    }

    public QTable getQTableFood() {
        return qTableFood;
    }

    public QTable getQTableNest() {
        return qTableNest;
    }
}
