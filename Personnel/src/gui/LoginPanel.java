// Fichier: src/gui/LoginPanel.java
package gui;

import personnel.Employe;
import personnel.SauvegardeImpossible;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Panneau de connexion de l'application.
 * Permet aux utilisateurs de s'authentifier avec leur nom d'utilisateur et mot de passe.
 */
public class LoginPanel extends JPanel {
    private PersonnelGUI mainFrame;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel messageLabel;

    /**
     * Constructeur du panneau de connexion.
     * @param mainFrame La fenêtre principale de l'application pour la navigation et l'accès aux données.
     */
    public LoginPanel(PersonnelGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout()); // Utilisation de GridBagLayout pour un positionnement flexible
        setBackground(Style.PRIMARY_BACKGROUND);
        setBorder(Style.PADDING_BORDER); // Ajout d'un rembourrage autour du panneau

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Espacement entre les composants
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Titre
        JLabel titleLabel = new JLabel("Connexion au Système de Personnel");
        titleLabel.setFont(Style.FONT_TITLE);
        titleLabel.setForeground(Style.ACCENT_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // S'étend sur 2 colonnes
        gbc.anchor = GridBagConstraints.CENTER;
        add(titleLabel, gbc);

        // Champ Nom d'utilisateur
        JLabel usernameLabel = new JLabel("Nom d'utilisateur / Mail :");
        Style.styleLabel(usernameLabel);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(usernameLabel, gbc);

        usernameField = new JTextField(20);
        Style.styleTextField(usernameField);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(usernameField, gbc);

        // Champ Mot de passe
        JLabel passwordLabel = new JLabel("Mot de passe :");
        Style.styleLabel(passwordLabel);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        add(passwordLabel, gbc);

        passwordField = new JPasswordField(20);
        Style.stylePasswordField(passwordField);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        add(passwordField, gbc);

        // Bouton de connexion
        loginButton = new JButton("Se connecter");
        Style.styleButton(loginButton);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        // Message d'erreur/succès
        messageLabel = new JLabel("");
        messageLabel.setFont(Style.FONT_LABEL);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(messageLabel, gbc);

        // Ajout de l'écouteur d'événements pour le bouton de connexion
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                authenticateUser();
            }
        });
        // Permettre la connexion avec la touche Entrée dans les champs de texte
        usernameField.addActionListener(e -> authenticateUser());
        passwordField.addActionListener(e -> authenticateUser());
    }

    /**
     * Tente d'authentifier l'utilisateur en utilisant les informations saisies.
     */
    private void authenticateUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            displayMessage("Veuillez entrer un nom d'utilisateur/mail et un mot de passe.", Style.ERROR_COLOR);
            return;
        }

        try {
            Employe authenticatedEmploye = mainFrame.getGestionPersonnel().authentifier(username, password);
            if (authenticatedEmploye != null) {
                mainFrame.setUtilisateurConnecte(authenticatedEmploye);
                displayMessage("Authentification réussie pour " + authenticatedEmploye.getNom() + " " + authenticatedEmploye.getPrenom() + " !", Style.SUCCESS_COLOR);

                // Naviguer vers le menu principal après un court délai pour que l'utilisateur voie le message de succès
                Timer timer = new Timer(1000, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        ((Timer)e.getSource()).stop(); // Arrête le timer
                        // Initialise et ajoute le MainMenuPanel si ce n'est pas déjà fait
                        // Une approche plus robuste serait de stocker les panneaux dans un Map
                        // ou de les créer une fois et de les réinitialiser.
                        // Pour cet exemple, nous allons l'ajouter si ce n'est pas déjà fait.
                        // On vérifie si un composant avec le nom MAIN_MENU_PANEL existe déjà
                        boolean mainMenuPanelExists = false;
                        for (Component comp : mainFrame.mainPanel.getComponents()) {
                            if (mainFrame.mainPanel.getLayout() instanceof CardLayout) {
                                // Il n'y a pas de méthode directe pour obtenir le nom d'une carte
                                // On suppose que si le composant est un MainMenuPanel, il est celui que l'on cherche.
                                if (comp instanceof MainMenuPanel) {
                                    mainMenuPanelExists = true;
                                    break;
                                }
                            }
                        }

                        if (!mainMenuPanelExists) {
                            MainMenuPanel menuPanel = new MainMenuPanel(mainFrame);
                            mainFrame.mainPanel.add(menuPanel, PersonnelGUI.MAIN_MENU_PANEL);
                        }
                        mainFrame.showPanel(PersonnelGUI.MAIN_MENU_PANEL);
                        // Réinitialiser les champs pour une prochaine connexion (si déconnexion)
                        usernameField.setText("");
                        passwordField.setText("");
                        messageLabel.setText("");
                    }
                });
                timer.setRepeats(false); // S'exécute une seule fois
                timer.start();

            } else {
                displayMessage("Nom d'utilisateur ou mot de passe incorrect.", Style.ERROR_COLOR);
            }
        } catch (IllegalArgumentException ex) {
            displayMessage("Erreur d'authentification : " + ex.getMessage(), Style.ERROR_COLOR);
        } catch (SauvegardeImpossible ex) {
            displayMessage("Erreur de base de données lors de l'authentification : " + ex.getMessage(), Style.ERROR_COLOR);
            ex.printStackTrace(); // Afficher la stack trace pour le débogage
        }
    }

    /**
     * Affiche un message à l'utilisateur avec une couleur spécifique.
     * @param message Le message à afficher.
     * @param color La couleur du texte du message.
     */
    private void displayMessage(String message, Color color) {
        messageLabel.setText(message);
        messageLabel.setForeground(color);
    }
}
