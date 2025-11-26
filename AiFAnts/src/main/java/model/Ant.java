package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Ant implements Serializable {
    private static final long serialVersionUID = 1L;

    public int x, y;
    public boolean hasFood;
    public boolean isDead;
    private int lastX, lastY; // Pour éviter de faire demi-tour immédiatement

    public enum Type { EXPLORER, HARVESTER, SOLDIER }
    private Type type;

    public Ant(int startX, int startY, Type type) {
        this.x = startX;
        this.y = startY;
        this.lastX = startX;
        this.lastY = startY;
        this.type = type;
        this.hasFood = false;
        this.isDead = false;
    }

    // Ajout du Getter pour le GUI
    public Type getType() {
        return this.type;
    }

    public void update(int tick, Environment env) {
        if (isDead) return;

        // 1. Logique de mouvement
        move(env);

        // 2. Interaction avec l'environnement
        CellType currentCell = env.grid[x][y];

        // Ramasser nourriture
        if (!hasFood && currentCell == CellType.FOOD) {
            env.foodAmounts[x][y]--;
            hasFood = true;
            // Récompense immédiate pour avoir trouvé à manger
            env.qTableFood.update(lastX, lastY, getActionFromMove(lastX, lastY, x, y), Constants.REWARD_GOAL);
            if (env.foodAmounts[x][y] <= 0) {
                env.grid[x][y] = CellType.EMPTY;
            }
        }

        // Déposer nourriture
        if (hasFood && currentCell == CellType.NEST) {
            hasFood = false;
            env.foodCollected++;
            env.qTableNest.update(lastX, lastY, getActionFromMove(lastX, lastY, x, y), Constants.REWARD_GOAL);
        }

        // Danger / Mort
        if (currentCell == CellType.DANGER || currentCell == CellType.WALL) { // Wall ne devrait pas arriver mais sécu
            isDead = true;
            // Pénalité massive
            QTable activeQT = hasFood ? env.qTableNest : env.qTableFood;
            activeQT.update(lastX, lastY, getActionFromMove(lastX, lastY, x, y), Constants.REWARD_DEATH);
        }
    }

    private void move(Environment env) {
        // Déterminer quelle table utiliser (si j'ai faim -> je cherche FOOD, si j'ai mangé -> je cherche NEST)
        QTable targetTable = hasFood ? env.qTableNest : env.qTableFood;

        // Définir le taux d'exploration (Epsilon) selon le type de fourmi
        double epsilon = 0.1; // Valeur par défaut
        switch (type) {
            case EXPLORER: epsilon = Constants.DEFAULT_EPSILON_EXPLORER; break; // Très curieuse
            case SOLDIER:  epsilon = Constants.DEFAULT_EPSILON_SOLDIER; break; // Un peu curieuse
            case HARVESTER: epsilon = Constants.DEFAULT_EPSILON_HARVESTER; break; // Très disciplinée (suit les ordres)
        }

        // Identifier les coups possibles
        boolean[] possibleActions = env.getPossibleActions(x, y);
        int action = -1;

        // --- STRATÉGIE EPSILON-GREEDY ---
        // Si un nombre aléatoire est < epsilon, on EXPLORE (hasard)
        // Sinon, on EXPLOITE (on demande à la QTable le meilleur chemin)
        if (Math.random() < epsilon) {
            action = getRandomAction(possibleActions);
        } else {
            action = targetTable.getBestAction(x, y, possibleActions);
        }

        // Si l'action renvoyée est invalide (bloqué), on force un random
        if (action == -1) {
            return; // Bloqué, on passe son tour
        }

        // Exécuter le mouvement
        int nextX = x, nextY = y;
        if (action == 0) nextY--; // Haut
        else if (action == 1) nextY++; // Bas
        else if (action == 2) nextX--; // Gauche
        else if (action == 3) nextX++; // Droite

        // Mise à jour Q-Learning (Apprentissage)
        // On apprend de la transition : État actuel -> Action -> Nouvel État
        double reward = Constants.REWARD_STEP; // Légère pénalité pour encourager la vitesse

        // On met à jour la Q-Table AVANT de bouger physiquement (basé sur là où on va atterrir)
        targetTable.update(x, y, action, reward + Constants.DEFAULT_GAMMA * targetTable.getMaxValue(nextX, nextY));

        // Déplacement physique
        this.lastX = x;
        this.lastY = y;
        this.x = nextX;
        this.y = nextY;
    }

    // Choisit une action valide au hasard
    private int getRandomAction(boolean[] possible) {
        List<Integer> actions = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            if (possible[i]) actions.add(i);
        }
        if (actions.isEmpty()) return -1;
        return actions.get(new Random().nextInt(actions.size()));
    }

    // Helper pour retrouver l'index de l'action à partir des coords (utile pour les updates rewards)
    private int getActionFromMove(int lx, int ly, int nx, int ny) {
        if (ny < ly) return 0; // Haut
        if (ny > ly) return 1; // Bas
        if (nx < lx) return 2; // Gauche
        if (nx > lx) return 3; // Droite
        return 0;
    }
}
