package model;

public class Constants {
    // Dimensions
    public static final int GRID_WIDTH = 40;
    public static final int GRID_HEIGHT = 30;
    public static final int MAX_TICKS = 1_000_000_000;

    // Récompenses (Q-Learning)
    public static final double REWARD_GOAL = 1000.0;
    public static final double REWARD_DEATH = -100.0;
    public static final double REWARD_STEP = -1.0;

    // Paramètres Q-Learning par défaut (seront surchargés par fourmi)
    public static final double DEFAULT_ALPHA = 0.3; // Apprentissage
    public static final double DEFAULT_GAMMA = 0.9; // Actualisation
    public static final double DEFAULT_EPSILON_EXPLORER = 0.7; // Exploration
    public static final double DEFAULT_EPSILON_SOLDIER = 0.4; // Exploration
    public static final double DEFAULT_EPSILON_HARVESTER = 0.2; // Exploration
    public static final double DECAY_RATE = 0.0001; // Dissipation phéromone
}
