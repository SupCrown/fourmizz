package model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class GUI extends JPanel {
    private Environment env;
    private static final int CELL_SIZE = 20;

    // Interface
    private static JSlider speedSlider;
    private static JLabel statsLabel;

    // Contrôles de Spawn (remis !)
    private static JComboBox<Ant.Type> typeSelector;
    private static JTextField countField;

    // Outils Souris
    private enum Tool { NONE, FOOD, KILL, ERASER }
    private static Tool currentTool = Tool.NONE;

    public GUI(Environment env) {
        this.env = env;
        this.setPreferredSize(new Dimension(Constants.GRID_WIDTH * CELL_SIZE, Constants.GRID_HEIGHT * CELL_SIZE));

        MouseAdapter mouseHandler = new MouseAdapter() {
            public void mousePressed(MouseEvent e) { useTool(e); }
            public void mouseDragged(MouseEvent e) { useTool(e); }
        };
        this.addMouseListener(mouseHandler);
        this.addMouseMotionListener(mouseHandler);
    }

    private void useTool(MouseEvent e) {
        int x = e.getX() / CELL_SIZE;
        int y = e.getY() / CELL_SIZE;
        if (x >= 0 && x < Constants.GRID_WIDTH && y >= 0 && y < Constants.GRID_HEIGHT) {
            if (currentTool == Tool.FOOD) env.setCell(x, y, CellType.FOOD);
            else if (currentTool == Tool.ERASER) env.setCell(x, y, CellType.EMPTY);
            else if (currentTool == Tool.KILL) env.killAntsAt(x, y);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1. DESSINER TERRAIN
        for(int x=0; x<Constants.GRID_WIDTH; x++) {
            for(int y=0; y<Constants.GRID_HEIGHT; y++) {
                int px = x * CELL_SIZE;
                int py = y * CELL_SIZE;

                CellType type = env.grid[x][y];
                g.setColor(Color.WHITE);

                if(type == CellType.WALL) g.setColor(Color.DARK_GRAY);
                else if(type == CellType.NEST) g.setColor(new Color(100, 100, 255));
                else if(type == CellType.FOOD) {
                    // La nourriture devient plus pâle si elle est presque finie
                    int amount = env.foodAmounts[x][y];
                    int green = Math.max(50, 255 - (amount * 40));
                    g.setColor(new Color(50, 200, 50));
                }

                g.fillRect(px, py, CELL_SIZE, CELL_SIZE);

                // Afficher qté nourriture (petit texte si > 1)
                if(type == CellType.FOOD && env.foodAmounts[x][y] > 0) {
                    g.setColor(Color.BLACK);
                    g.setFont(new Font("Arial", Font.PLAIN, 10));
                    g.drawString(""+env.foodAmounts[x][y], px+5, py+14);
                }

                g.setColor(new Color(240, 240, 240)); // Grille très légère
                g.drawRect(px, py, CELL_SIZE, CELL_SIZE);

                // PHÉROMONES
                if(type == CellType.EMPTY) {
                    double val = env.qTableFood.getMaxValue(x, y);
                    if(val > 0) {
                        float alpha = Math.min(1.0f, (float)val / 100.0f);
                        g.setColor(new Color(0, 255, 0, (int)(alpha * 80)));
                        g.fillRect(px, py, CELL_SIZE, CELL_SIZE);
                    }
                }
            }
        }

        // 2. DESSINER FOURMIS
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(2)); // Trait plus épais pour le cercle vide

        Ant[] drawAnts = env.ants.toArray(new Ant[0]);
        for(Ant ant : drawAnts) {
            if(ant.isDead) continue;
            int ax = ant.x * CELL_SIZE;
            int ay = ant.y * CELL_SIZE;

            // COULEUR SELON LE TYPE
            switch(ant.getType()) {
                case HARVESTER: g2.setColor(Color.BLACK); break;
                case EXPLORER:  g2.setColor(Color.BLUE); break;
                case SOLDIER:   g2.setColor(Color.MAGENTA); break;
                default:        g2.setColor(Color.DARK_GRAY);
            }

            // VISUEL : REMPLI SI NOURRITURE, VIDE SINON
            if (ant.hasFood) {
                g2.fillOval(ax + 3, ay + 3, CELL_SIZE - 6, CELL_SIZE - 6);
            } else {
                g2.drawOval(ax + 3, ay + 3, CELL_SIZE - 6, CELL_SIZE - 6);
            }
        }
    }

    public static void createAndShowGUI(Environment env) {
        JFrame frame = new JFrame("AiFAnts - Ultimate Version");
        frame.setLayout(new BorderLayout());
        GUI drawingPanel = new GUI(env);
        frame.add(drawingPanel, BorderLayout.CENTER);

        // --- HAUT : STATS ---
        statsLabel = new JLabel("Initialisation...", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        frame.add(statsLabel, BorderLayout.NORTH);

        // --- BAS : CONTRÔLES ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        // Ligne 1 : Outils Souris
        JPanel toolLine = new JPanel();
        toolLine.add(new JLabel("Outil: "));
        ButtonGroup bg = new ButtonGroup();

        JRadioButton rbNone = new JRadioButton("Rien", true);
        rbNone.addActionListener(e -> currentTool = Tool.NONE);

        JRadioButton rbFood = new JRadioButton("Bouffe (+5)");
        rbFood.addActionListener(e -> currentTool = Tool.FOOD);

        JRadioButton rbKill = new JRadioButton("Tueur");
        rbKill.addActionListener(e -> currentTool = Tool.KILL);

        JRadioButton rbEraser = new JRadioButton("Gomme");
        rbEraser.addActionListener(e -> currentTool = Tool.ERASER);

        bg.add(rbNone); bg.add(rbFood); bg.add(rbKill); bg.add(rbEraser);
        toolLine.add(rbNone); toolLine.add(rbFood); toolLine.add(rbKill); toolLine.add(rbEraser);

        JButton btnRand = new JButton("Pop Aléatoire (x5)");
        btnRand.addActionListener(e -> { env.spawnRandomFood(5); drawingPanel.repaint(); });
        toolLine.add(btnRand);

        // Ligne 2 : Simulation & Spawn
        JPanel simLine = new JPanel();

        // Vitesse
        speedSlider = new JSlider(0, 200, 20);
        speedSlider.setInverted(true);
        speedSlider.setPreferredSize(new Dimension(100, 20));
        simLine.add(new JLabel("Vitesse:"));
        simLine.add(speedSlider);

        simLine.add(new JSeparator(SwingConstants.VERTICAL));

        // Spawn (Remis !)
        typeSelector = new JComboBox<>(Ant.Type.values());
        countField = new JTextField("10", 3);
        JButton btnSpawn = new JButton("Lancer !");

        btnSpawn.addActionListener(e -> {
            try {
                int n = Integer.parseInt(countField.getText());
                Ant.Type t = (Ant.Type) typeSelector.getSelectedItem();
                env.spawnWave(n, t);
            } catch(Exception ex) { ex.printStackTrace(); }
        });

        simLine.add(new JLabel("Ajouter:"));
        simLine.add(countField);
        simLine.add(typeSelector);
        simLine.add(btnSpawn);

        // Save/Load
        JButton btnSave = new JButton("S");
        btnSave.addActionListener(e -> env.saveState());
        JButton btnLoad = new JButton("L");
        btnLoad.addActionListener(e -> env.loadState());
        simLine.add(btnSave);
        simLine.add(btnLoad);

        bottomPanel.add(toolLine);
        bottomPanel.add(simLine);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // --- THREAD ---
        new Thread(() -> {
            long tick = 0;
            while(tick < Constants.MAX_TICKS) {
                try {
                    Ant[] currentAnts = env.ants.toArray(new Ant[0]);
                    for(Ant a : currentAnts) a.update((int)tick, env);
                    env.cleanupDeadAnts();
                    env.qTableFood.applyUpdatesAndDecay();
                    env.qTableNest.applyUpdatesAndDecay();

                    final long t = tick;
                    SwingUtilities.invokeLater(() -> {
                        statsLabel.setText(String.format("Fourmis: %d | Stock Nid: %d | Tick: %d",
                                env.ants.size(), env.foodCollected, t));
                        drawingPanel.repaint();
                    });

                    int d = speedSlider.getValue();
                    if(d > 0) Thread.sleep(d);
                    tick++;
                } catch(Exception e) { e.printStackTrace(); }
            }
        }).start();
    }
}
