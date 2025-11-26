package model;

import java.util.Random;

public class Ant {
    public enum Type { EXPLORER, SOLDIER, HARVESTER }
    public enum State { FORAGING, RETURNING } // Cherche manger ou cherche nid

    private int id;
    private int x, y;
    private Type type;
    private State state;
    private int foodCarried = 0;

    // Paramètres spécifiques
    private int maxLoad;
    private int speed; // 1 mvt tous les X ticks
    private int vision;

    // Q-Learning perso
    private double alpha = Constants.DEFAULT_ALPHA;
    private double gamma = Constants.DEFAULT_GAMMA;
    private double epsilon = Constants.DEFAULT_EPSILON;

    private Random random = new Random();

    public Ant(int id, int startX, int startY, Type type) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        this.type = type;
        this.state = State.FORAGING;

        // Init selon le sujet
        switch (type) {
            case EXPLORER:
                this.maxLoad = 10; this.speed = 5; this.vision = 1; break;
            case SOLDIER:
                this.maxLoad = 10; this.speed = 5; this.vision = 1; break;
            case HARVESTER:
                this.maxLoad = 100; this.speed = 10; this.vision = 0; break;
        }
    }

    // Logique principale exécutée à chaque tick
    public void update(int currentTick, Environment env) {
        // Gestion vitesse : si ce n'est pas le tour de bouger, on ne fait rien
        if (currentTick % speed != 0) return;

        // Choix de la table selon l'état (je cherche Manger OU je cherche Nid)
        QTable currentTable = (state == State.FORAGING) ? env.qTableFood : env.qTableNest;
        QTable targetTable = (state == State.FORAGING) ? env.qTableNest : env.qTableFood; // Table opposée (là où on veut laisser trace)

        // 1. Déterminer les mouvements possibles
        // Actions: 0:Haut, 1:Bas, 2:Gauche, 3:Droite
        boolean[] possibleMoves = new boolean[4];
        int[][] dirs = {{0,-1}, {0,1}, {-1,0}, {1,0}}; // H, B, G, D

        for(int i=0; i<4; i++) {
            int nx = x + dirs[i][0];
            int ny = y + dirs[i][1];

            if (!env.isValid(nx, ny)) {
                possibleMoves[i] = false;
            } else if (env.isWall(nx, ny)) {
                possibleMoves[i] = false;
            } else if (vision > 0 && env.isDeadly(nx, ny)) {
                // Vision 1 : on voit le danger, on n'y va pas (sauf si on veut se suicider, mais le sujet dit "éviter")
                possibleMoves[i] = false;

                // Sujet : "Une vision de 1 permet [...] de mettre à jour la récompense comme si elle l'avait fait"
                // On met à jour la QTable pour dire "Danger ici !" sans y aller
                double currentQ = currentTable.getQValue(x, y, i);
                double targetQ = Constants.REWARD_DEATH; // Pas de maxQ futur car on est mort
                double delta = alpha * (targetQ - currentQ); // Formule simplifiée car état terminal
                currentTable.addPendingUpdate(x, y, i, delta);
            } else {
                possibleMoves[i] = true;
            }
        }

        // 2. Choix Action (Epsilon-Greedy)
        int action = -1;
        if (random.nextDouble() < epsilon) {
            // Hasard parmi les possibles
            int count = 0;
            for(boolean b : possibleMoves) if(b) count++;
            if(count > 0) {
                int r = random.nextInt(count);
                for(int i=0; i<4; i++) {
                    if(possibleMoves[i]) {
                        if(r==0) { action = i; break; }
                        r--;
                    }
                }
            }
        } else {
            // Meilleure action connue
            action = currentTable.getBestAction(x, y, possibleMoves);
        }

        // Si bloqué, on passe
        if (action == -1) return;

        // 3. Exécution
        int oldX = x, oldY = y;
        int nextX = x + dirs[action][0];
        int nextY = y + dirs[action][1];

        // Vérification Mortelle (si vision 0)
        if (env.isDeadly(nextX, nextY)) {
            env.killAnt(this); // La fourmi meurt
            // Mise à jour Q-Table (Punition Mortelle)
            double qVal = currentTable.getQValue(oldX, oldY, action);
            double delta = alpha * (Constants.REWARD_DEATH - qVal);
            currentTable.addPendingUpdate(oldX, oldY, action, delta);
            return;
        }

        // Déplacement
        this.x = nextX;
        this.y = nextY;

        // 4. Calcul Récompense et Q-Learning
        double reward = Constants.REWARD_STEP;
        boolean objectiveReached = false;

        if (state == State.FORAGING && env.hasFood(x, y)) {
            // A trouvé nourriture
            reward = Constants.REWARD_GOAL;
            int taken = env.takeFood(x, y, maxLoad);
            this.foodCarried = taken;
            this.state = State.RETURNING; // Changement de mode
            objectiveReached = true;
        } else if (state == State.RETURNING && env.isNest(x, y)) {
            // A trouvé nid
            reward = Constants.REWARD_GOAL;
            env.dropFoodAtNest(this.foodCarried);
            this.foodCarried = 0;
            this.state = State.FORAGING; // Changement de mode
            objectiveReached = true;
        }

        // Formule Q-Learning : Q(s,a) = Q(s,a) + alpha * [R + gamma * maxQ(s', all) - Q(s,a)]
        double oldQ = currentTable.getQValue(oldX, oldY, action);
        // Pour s' (état futur), on regarde toutes les actions possibles depuis la nouvelle case
        boolean[] nextMoves = env.getPossibleMoves(x, y);
        double maxFutureQ = (objectiveReached) ? 0 : currentTable.getQValue(x, y, currentTable.getBestAction(x, y, nextMoves));

        double delta = alpha * (reward + (gamma * maxFutureQ) - oldQ);

        // On enregistre la modification (sera appliquée par l'env)
        currentTable.addPendingUpdate(oldX, oldY, action, delta);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Type getType() { return type; }
    public State getState() { return state; }
}
