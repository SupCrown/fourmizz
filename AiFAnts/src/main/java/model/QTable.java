package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class QTable implements Serializable {
    private static final long serialVersionUID = 1L;

    // Table principale : [x][y][action]
    private double[][][] table;

    // Table temporaire pour cumuler les modifications pendant un tour (optimisation)
    private transient double[][][] pendingUpdates;

    public QTable() {
        table = new double[Constants.GRID_WIDTH][Constants.GRID_HEIGHT][4];
        resetPending();
    }

    // Initialise la table temporaire (appelé au chargement ou création)
    private void resetPending() {
        pendingUpdates = new double[Constants.GRID_WIDTH][Constants.GRID_HEIGHT][4];
    }

    // Si on charge une sauvegarde, on réinitialise la table temporaire
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        resetPending();
    }

    /**
     * Retourne la meilleure action (0,1,2,3) pour une position donnée.
     * Si plusieurs actions ont la même valeur, en choisit une au hasard parmi elles.
     */
    public int getBestAction(int x, int y, boolean[] possibleActions) {
        double maxVal = -Double.MAX_VALUE;
        List<Integer> bestActions = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            if (possibleActions[i]) {
                double val = table[x][y][i];
                if (val > maxVal) {
                    maxVal = val;
                    bestActions.clear();
                    bestActions.add(i);
                } else if (val == maxVal) {
                    bestActions.add(i);
                }
            }
        }

        if (bestActions.isEmpty()) return -1;
        // Choix aléatoire parmi les meilleures égalités pour éviter l'effet "robot bloqué"
        return bestActions.get(new Random().nextInt(bestActions.size()));
    }

    /**
     * Retourne la valeur maximale atteignable depuis cette case (pour le calcul du futur)
     */
    public double getMaxValue(int x, int y) {
        double max = -Double.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            if (table[x][y][i] > max) {
                max = table[x][y][i];
            }
        }
        return (max == -Double.MAX_VALUE) ? 0 : max;
    }

    /**
     * Retourne la valeur actuelle d'une action précise
     */
    public double getValue(int x, int y, int action) {
        return table[x][y][action];
    }

    /**
     * Enregistre une mise à jour.
     * targetValue = la nouvelle valeur calculée par la fourmi.
     * On ne l'applique pas tout de suite, on la stocke dans pendingUpdates.
     */
    public void update(int x, int y, int action, double targetValue) {
        // Formule Q-Learning : Q(s,a) = (1-alpha)*Q(s,a) + alpha*target
        double oldValue = table[x][y][action];
        double newValue = (1 - Constants.DEFAULT_ALPHA) * oldValue + Constants.DEFAULT_ALPHA * targetValue;

        // On stocke la différence (delta)
        pendingUpdates[x][y][action] += (newValue - oldValue);
    }

    /**
     * Applique toutes les modifications en attente et fait diminuer les phéromones (Decay).
     * À appeler une fois par tick global.
     */
    public void applyUpdatesAndDecay() {
        for (int x = 0; x < Constants.GRID_WIDTH; x++) {
            for (int y = 0; y < Constants.GRID_HEIGHT; y++) {
                for (int i = 0; i < 4; i++) {
                    // 1. Appliquer les mises à jour des fourmis
                    table[x][y][i] += pendingUpdates[x][y][i];
                    pendingUpdates[x][y][i] = 0; // Reset pour le prochain tour

                    // 2. Évaporation (Decay) : La valeur tend doucement vers 0
                    table[x][y][i] *= (1.0 - Constants.DECAY_RATE);
                }
            }
        }
    }
}
