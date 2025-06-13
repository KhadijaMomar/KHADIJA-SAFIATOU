// Fichier: src/gui/MainMenuPanel.java
package gui;

import personnel.Employe;
import personnel.GestionPersonnel;
import personnel.Ligue;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent; // Importation ajoutée
import java.util.Set;
import javax.swing.border.EmptyBorder; // Importation ajoutée

/**
 * Panneau du menu principal de l'application.
 * Affiche les options disponibles en fonction du rôle de l'utilisateur connecté.
 */
public class MainMenuPanel extends JPanel {
    private PersonnelGUI mainFrame;
    private JLabel welcomeLabel;
    private JPanel optionsPanel;

    /**
     * Constructeur du panneau du menu principal.
     * @param mainFrame La fenêtre principale de l'application.
     */
    public MainMenuPanel(PersonnelGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20)); // Utilisation de BorderLayout avec espacement
        setBackground(Style.PRIMARY_BACKGROUND);
        setBorder(Style.PADDING_BORDER);

        // Panneau d'en-tête pour le message de bienvenue et le bouton de déconnexion
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false); // Rendre transparent pour voir le fond du parent

        welcomeLabel = new JLabel();
        welcomeLabel.setFont(Style.FONT_SUBTITLE);
        welcomeLabel.setForeground(Style.TEXT_COLOR);
        welcomeLabel.setHorizontalAlignment(SwingConstants.LEFT);
        headerPanel.add(welcomeLabel, BorderLayout.CENTER);

        JButton logoutButton = new JButton("Déconnexion");
        Style.styleButton(logoutButton);
        logoutButton.addActionListener(e -> logout());
        headerPanel.add(logoutButton, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Panneau pour les options du menu (sera rempli dynamiquement)
        optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS)); // Options empilées verticalement
        optionsPanel.setOpaque(false);
        optionsPanel.setBorder(new EmptyBorder(30, 0, 0, 0)); // Espacement au-dessus des options

        JScrollPane scrollPane = new JScrollPane(optionsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder()); // Supprimer la bordure du JScrollPane
        add(scrollPane, BorderLayout.CENTER);

        // Met à jour le menu lorsque le panneau est affiché
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                updateMenuOptions();
            }
        });
    }

    /**
     * Met à jour les options du menu en fonction du rôle de l'utilisateur connecté.
     */
    private void updateMenuOptions() {
        optionsPanel.removeAll(); // Supprime toutes les options précédentes
        Employe utilisateurConnecte = mainFrame.getUtilisateurConnecte();

        if (utilisateurConnecte == null) {
            welcomeLabel.setText("Bienvenue ! (Non connecté)");
            // Rediriger vers la connexion si non connecté
            mainFrame.showPanel(PersonnelGUI.LOGIN_PANEL);
            return;
        }

        welcomeLabel.setText("Bienvenue, " + utilisateurConnecte.getPrenom() + " " + utilisateurConnecte.getNom() + " !");

        // Options pour tous les utilisateurs (Annuaire)
        addMenuItem("Afficher mon profil", e -> showMyProfile(utilisateurConnecte));
        addMenuItem("Consulter l'annuaire des employés", e -> showEmployeDirectory());

        // Options spécifiques au Super-Administrateur
        if (utilisateurConnecte.estRoot()) {
            addSeparator();
            addMenuItem("Gérer les ligues", e -> manageLigues());
            addMenuItem("Gérer le compte root", e -> manageRootAccount());
        } else if (utilisateurConnecte.getLigue() != null && utilisateurConnecte.getLigue().getAdministrateur() != null && utilisateurConnecte.getLigue().getAdministrateur().equals(utilisateurConnecte)) {
            // Options spécifiques à l'Administrateur de Ligue (via l'application bureau, mais ici pour la cohérence si on étend)
            addSeparator();
            addMenuItem("Gérer les employés de ma ligue (" + utilisateurConnecte.getLigue().getNom() + ")", e -> manageLigueEmployes(utilisateurConnecte.getLigue()));
        }

        optionsPanel.revalidate();
        optionsPanel.repaint();
    }

    /**
     * Ajoute un élément de menu (bouton) au panneau des options.
     * @param text Le texte du bouton.
     * @param action L'action à exécuter lorsque le bouton est cliqué.
     */
    private void addMenuItem(String text, ActionListener action) {
        JButton button = new JButton(text);
        Style.styleButton(button);
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Centrer le bouton horizontalement
        button.setMaximumSize(new Dimension(300, 50)); // Taille maximale pour les boutons de menu
        button.addActionListener(action);
        optionsPanel.add(button);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Espacement entre les boutons
    }

    /**
     * Ajoute un séparateur visuel entre les groupes d'options.
     */
    private void addSeparator() {
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        separator.setForeground(Style.ACCENT_COLOR.darker());
        separator.setBackground(Style.SECONDARY_BACKGROUND);
        separator.setMaximumSize(new Dimension(300, 2)); // Taille du séparateur
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Espacement avant le séparateur
        optionsPanel.add(separator);
        optionsPanel.add(Box.createRigidArea(new Dimension(0, 20))); // Espacement après le séparateur
    }

    /**
     * Affiche le profil de l'utilisateur connecté.
     * @param employe L'employé dont le profil doit être affiché.
     */
    private void showMyProfile(Employe employe) {
        // Pour l'instant, affichons une boîte de dialogue simple.
        String profileInfo = String.format(
            "<html>" +
            "<body style='font-family: %s; color: #%s;'>" + // Utiliser #%s pour la couleur hex
            "<h2>Mon Profil</h2>" +
            "<p><b>Nom:</b> %s</p>" +
            "<p><b>Prénom:</b> %s</p>" +
            "<p><b>Mail:</b> %s</p>" +
            "<p><b>Ligue:</b> %s</p>" +
            "<p><b>Date d'arrivée:</b> %s</p>" +
            "<p><b>Date de départ:</b> %s</p>" +
            "<p><b>ID:</b> %d</p>" +
            "<p><b>Statut:</b> %s</p>" +
            "</body></html>",
            Style.FONT_LABEL.getFamily(), Integer.toHexString(Style.TEXT_COLOR.getRGB()).substring(2), // Convertir Color en hex string
            employe.getNom(),
            employe.getPrenom(),
            employe.getMail(),
            employe.getLigue() != null ? employe.getLigue().getNom() : "N/A",
            employe.getDateArrivee() != null ? employe.getDateArrivee().toString() : "N/A",
            employe.getDateDepart() != null ? employe.getDateDepart().toString() : "N/A",
            employe.getId(),
            employe.estRoot() ? "Super-Administrateur" : (employe.getLigue() != null && employe.getLigue().getAdministrateur() != null && employe.getLigue().getAdministrateur().equals(employe) ? "Administrateur de Ligue" : "Employé Standard")
        );

        JOptionPane.showMessageDialog(mainFrame, profileInfo, "Mon Profil", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Affiche l'annuaire de tous les employés.
     * Navigue vers le EmployeDirectoryPanel.
     */
    private void showEmployeDirectory() {
        EmployeDirectoryPanel employeDirectoryPanel = new EmployeDirectoryPanel(mainFrame);
        // Ajoute le panneau à la mainPanel de PersonnelGUI avec un nom de carte
        mainFrame.mainPanel.add(employeDirectoryPanel, PersonnelGUI.EMPLOYE_DIRECTORY_PANEL);
        // Affiche le panneau
        mainFrame.showPanel(PersonnelGUI.EMPLOYE_DIRECTORY_PANEL);
    }

    /**
     * Gère la navigation vers le panneau de gestion des ligues.
     * Accessible uniquement par le Super-Administrateur.
     */
    private void manageLigues() {
        // Initialise et affiche le LigueManagementPanel
        LigueManagementPanel ligueManagementPanel = new LigueManagementPanel(mainFrame);
        mainFrame.mainPanel.add(ligueManagementPanel, PersonnelGUI.LIGUE_MANAGEMENT_PANEL);
        mainFrame.showPanel(PersonnelGUI.LIGUE_MANAGEMENT_PANEL);
    }
    
    

    /**
     * Gère le compte root (édition des informations du super-administrateur).
     * Accessible uniquement par le Super-Administrateur.
     */
    private void manageRootAccount() {
        RootAccountPanel rootAccountPanel = new RootAccountPanel(mainFrame);
        mainFrame.mainPanel.add(rootAccountPanel, PersonnelGUI.ROOT_ACCOUNT_PANEL);
        mainFrame.showPanel(PersonnelGUI.ROOT_ACCOUNT_PANEL);
    }

    /**
     * Gère les employés d'une ligue spécifique.
     * Accessible par l'Administrateur de Ligue (via l'application bureau, mais ici pour la cohérence si on étend).
     * @param ligue La ligue dont les employés doivent être gérés.
     */
    private void manageLigueEmployes(Ligue ligue) {
        // Décommenter et utiliser EmployeManagementPanel pour l'administrateur de ligue
        EmployeManagementPanel employeManagementPanel = new EmployeManagementPanel(mainFrame, ligue);
        mainFrame.mainPanel.add(employeManagementPanel, PersonnelGUI.EMPLOYE_MANAGEMENT_PANEL);
        mainFrame.showPanel(PersonnelGUI.EMPLOYE_MANAGEMENT_PANEL);
    }

    /**
     * Gère la déconnexion de l'utilisateur.
     * Réinitialise l'utilisateur connecté et retourne au panneau de connexion.
     */
    private void logout() {
        mainFrame.setUtilisateurConnecte(null); // Déconnecte l'utilisateur
        mainFrame.showPanel(PersonnelGUI.LOGIN_PANEL); // Retourne à l'écran de connexion
        JOptionPane.showMessageDialog(mainFrame, "Vous avez été déconnecté.", "Déconnexion", JOptionPane.INFORMATION_MESSAGE);
    }
}
