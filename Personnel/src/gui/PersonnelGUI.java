package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import personnel.GestionPersonnel;
import personnel.SauvegardeImpossible; // Pour la sauvegarde
import personnel.Employe; // Pour l'accès au root

public class PersonnelGUI extends JFrame {

    private GestionPersonnel gestionPersonnel;
    private EmployeConsoleGUI employeConsoleGUI; // Nouvelle classe pour l'employé GUI
    private LigueConsoleGUI ligueConsoleGUI;     // Nouvelle classe pour la ligue GUI

    private JTabbedPane mainTabbedPane; // Pour organiser les différentes vues

    public PersonnelGUI(GestionPersonnel gestionPersonnel) {
        this.gestionPersonnel = gestionPersonnel;
        setTitle("Gestion du Personnel des Ligues");
        setSize(1000, 700); // Taille légèrement plus grande
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Gérer la fermeture manuellement pour la sauvegarde
        setLocationRelativeTo(null); // Centre la fenêtre sur l'écran

        // Initialisation des consoles GUI
        employeConsoleGUI = new EmployeConsoleGUI(gestionPersonnel);
        ligueConsoleGUI = new LigueConsoleGUI(gestionPersonnel, employeConsoleGUI);

        // Ajout d'un WindowListener pour gérer la fermeture (sauvegarde)
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                confirmAndExit();
            }
        });

        // Créer la barre de menu
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        // Menu "Fichier"
        JMenu fileMenu = new JMenu("Fichier");
        menuBar.add(fileMenu);

        JMenuItem saveAndQuitItem = new JMenuItem("Quitter et Enregistrer");
        saveAndQuitItem.addActionListener(e -> confirmAndExit()); // Appelle la méthode de confirmation
        fileMenu.add(saveAndQuitItem);

        JMenuItem quitWithoutSavingItem = new JMenuItem("Quitter sans Enregistrer");
        quitWithoutSavingItem.addActionListener(e -> {
            int response = JOptionPane.showConfirmDialog(
                    this,
                    "Êtes-vous sûr de vouloir quitter sans enregistrer les modifications ?",
                    "Confirmation de Quitter",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        });
        fileMenu.add(quitWithoutSavingItem);

        // Menu "Gérer"
        JMenu manageMenu = new JMenu("Gérer");
        menuBar.add(manageMenu);

        JMenuItem manageRootItem = new JMenuItem("Mon Compte (Root)");
        manageRootItem.addActionListener(e -> employeConsoleGUI.showEmployeDetails(gestionPersonnel.getRoot(), this));
        manageMenu.add(manageRootItem);

        JMenuItem manageLiguesItem = new JMenuItem("Gérer les Ligues");
        manageLiguesItem.addActionListener(e -> showLiguesManagement());
        manageMenu.add(manageLiguesItem);

        // JTabbedPane pour afficher différentes vues
        mainTabbedPane = new JTabbedPane();
        add(mainTabbedPane, BorderLayout.CENTER);

        // Initialisation de la vue par défaut ( un panneau d'accueil)
        JPanel welcomePanel = new JPanel();
        welcomePanel.add(new JLabel("Bienvenue dans l'application de gestion du personnel."));
        mainTabbedPane.addTab("Accueil", welcomePanel);
    }

    private void confirmAndExit() {
        int response = JOptionPane.showConfirmDialog(
                this,
                "Voulez-vous enregistrer les modifications avant de quitter ?",
                "Enregistrer et Quitter",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            try {
                gestionPersonnel.sauvegarder();
                JOptionPane.showMessageDialog(this, "Données sauvegardées avec succès.");
                System.exit(0);
            } catch (SauvegardeImpossible e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la sauvegarde : " + e.getMessage(),
                        "Erreur de Sauvegarde",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else if (response == JOptionPane.NO_OPTION) {
            System.exit(0);
        }
        // Si l'utilisateur clique sur CANCEL, l'application ne se ferme pas
    }

    private void showLiguesManagement() {
        // Supprime tous les onglets sauf le premier (Accueil)
        for (int i = mainTabbedPane.getTabCount() - 1; i > 0; i--) {
            mainTabbedPane.removeTabAt(i);
        }

        // Ajoute le panneau de gestion des ligues
        mainTabbedPane.addTab("Gestion des Ligues", ligueConsoleGUI.getLiguesPanel());
        mainTabbedPane.setSelectedComponent(ligueConsoleGUI.getLiguesPanel()); // Sélectionne l'onglet
    }

    public void display() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}