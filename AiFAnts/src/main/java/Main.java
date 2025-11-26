import model.Ant;
import model.Constants;
import model.Environment;
import model.GUI;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        // Initialisation
        Environment env = new Environment();

        // Fenêtre Graphique
        JFrame frame = new JFrame("AiFAnts 2026 - QLearning");
        GUI gui = new GUI(env);
        frame.add(gui);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Boucle de Simulation (Thread séparé pour ne pas figer l'interface)
        new Thread(() -> {
            long tick = 0;

            while (tick < Constants.MAX_TICKS) {
                try {
                    // Update des fourmis
                    for (Ant ant : env.ants) {
                        ant.update((int)tick, env);
                    }

                    // Nettoyage morts
                    env.cleanupDeadAnts();

                    // Mise à jour Q-Tables (Somme des modifs + Decay)
                    env.qTableFood.applyUpdatesAndDecay();
                    env.qTableNest.applyUpdatesAndDecay();

                    // Rafraichir écran
                    gui.repaint();

                    // Pause pour visualiser (retirer pour vitesse max)
                    Thread.sleep(50);

                    tick++;

                    // Vérification fin (si plus de nourriture sur la carte)
                    // (Code simplifié, à implémenter : vérifier si somme foodGrid == 0)

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Simulation terminée au temps : " + tick);
        }).start();
    }
}
