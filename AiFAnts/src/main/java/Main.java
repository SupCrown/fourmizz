import model.Ant;
import model.Constants;
import model.Environment;
import model.GUI;

import javax.swing.*;


public class Main {
    public static void main(String[] args) {
        System.out.println("Démarrage AiFAnts...");
        Environment env = new Environment();

        SwingUtilities.invokeLater(() -> {
            GUI.createAndShowGUI(env);
        });
    }
}

