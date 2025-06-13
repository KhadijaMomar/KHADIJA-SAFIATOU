// Fichier: src/gui/PersonnelGUI.java
package gui;

import personnel.GestionPersonnel;
import personnel.Employe;
import personnel.SauvegardeImpossible;
import javax.swing.*;
import java.awt.*;

/**
 * Fenêtre principale de l'application de gestion du personnel.
 * Gère le basculement entre les différents panneaux (connexion, menu principal, etc.)
 * et maintient l'état de l'utilisateur connecté.
 */
public class PersonnelGUI extends JFrame {
    private GestionPersonnel gestionPersonnel;
    private Employe utilisateurConnecte;
    private CardLayout cardLayout;
    public JPanel mainPanel; // mainPanel est public pour que LoginPanel puisse l'utiliser

    public static final String LOGIN_PANEL = "LoginPanel";
    public static final String MAIN_MENU_PANEL = "MainMenuPanel";
    public static final String EMPLOYE_DETAILS_PANEL = "EmployeDetailsPanel"; // Pourrait être utilisé pour l'édition de profil
    public static final String LIGUE_MANAGEMENT_PANEL = "LigueManagementPanel";
    public static final String LIGUE_DETAILS_PANEL = "LigueDetailsPanel";
    public static final String EMPLOYE_MANAGEMENT_PANEL = "EmployeManagementPanel";
    public static final String ROOT_ACCOUNT_PANEL = "RootAccountPanel"; // Ajouté pour la gestion du compte root
    public static final String EMPLOYE_DIRECTORY_PANEL = "EmployeDirectoryPanel"; // Ajouté pour l'annuaire des employés

    /**
     * Constructeur de la fenêtre principale.
     * Initialise la gestion du personnel et configure l'interface utilisateur.
     */
    public PersonnelGUI() {
        super("Gestion du Personnel des Ligues");
        try {
            // Initialise l'instance de GestionPersonnel (Singleton)
            this.gestionPersonnel = GestionPersonnel.getGestionPersonnel();
        } catch (RuntimeException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur fatale lors du chargement des données : " + e.getMessage(),
                    "Erreur de démarrage",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1); // Quitte l'application si la gestion du personnel ne peut pas être initialisée
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700); // Taille initiale de la fenêtre ajustée pour plus d'espace
        setLocationRelativeTo(null); // Centrer la fenêtre sur l'écran

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(Style.PRIMARY_BACKGROUND); // Couleur de fond principale

        // Ajout des panneaux à la carte
        mainPanel.add(new LoginPanel(this), LOGIN_PANEL);
        // Les autres panneaux seront ajoutés dynamiquement ou initialisés plus tard
        // pour éviter de charger toutes les données au démarrage.

        add(mainPanel);
        setVisible(true);

        // Afficher le panneau de connexion au démarrage
        showPanel(LOGIN_PANEL);
    }

    /**
     * Affiche un panneau spécifique dans la fenêtre principale.
     * @param panelName Le nom de la carte (panneau) à afficher.
     */
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    /**
     * Définit l'utilisateur actuellement connecté.
     * @param employe L'employé connecté.
     */
    public void setUtilisateurConnecte(Employe employe) {
        this.utilisateurConnecte = employe;
    }

    /**
     * Récupère l'utilisateur actuellement connecté.
     * @return L'employé connecté.
     */
    public Employe getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Récupère l'instance de GestionPersonnel.
     * @return L'instance de GestionPersonnel.
     */
    public GestionPersonnel getGestionPersonnel() {
        return gestionPersonnel;
    }

    /**
     * Point d'entrée principal de l'application GUI.
     * Exécute l'application dans le thread d'événements de Swing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new PersonnelGUI();
        });
    }
}
