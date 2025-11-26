package model;

public class QTable {
    // [x][y][action] : 0=HAUT, 1=BAS, 2=GAUCHE, 3=DROITE
    private double[][][] table;
    private double[][][] pendingUpdates; // Pour gérer la somme des modifs
    private int width, height;

    public QTable(int width, int height) {
        this.width = width;
        this.height = height;
        this.table = new double[width][height][4];
        this.pendingUpdates = new double[width][height][4];
    }

    // Obtenir la meilleure action pour une case
    public int getBestAction(int x, int y, boolean[] possibleMoves) {
        double maxQ = -Double.MAX_VALUE;
        int bestAction = -1;

        for (int i = 0; i < 4; i++) {
            if (possibleMoves[i]) {
                if (table[x][y][i] > maxQ) {
                    maxQ = table[x][y][i];
                    bestAction = i;
                }
            }
        }
        return bestAction; // Retourne -1 si aucun mouvement possible (coincé)
    }

    public double getQValue(int x, int y, int action) {
        if (x < 0 || x >= width || y < 0 || y >= height) return -9999;
        return table[x][y][action];
    }

    // Ajouter une modification en attente (règle de la somme)
    public void addPendingUpdate(int x, int y, int action, double delta) {
        pendingUpdates[x][y][action] += delta;
    }

    // Appliquer les modifs et la dissipation (1%) à la fin du tour
    public void applyUpdatesAndDecay() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int a = 0; a < 4; a++) {
                    // Application des mises à jour des fourmis
                    table[x][y][a] += pendingUpdates[x][y][a];
                    pendingUpdates[x][y][a] = 0; // Reset buffer

                    // Dissipation de 1%
                    table[x][y][a] *= (1.0 - Constants.DECAY_RATE);

                    // Seuil minimal pour éviter les -0.00000001
                    if (Math.abs(table[x][y][a]) < 0.001) table[x][y][a] = 0;
                }
            }
        }
    }
}
