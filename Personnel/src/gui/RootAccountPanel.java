// Fichier: src/gui/RootAccountPanel.java
package gui;

import personnel.Employe;
import personnel.GestionPersonnel;
import personnel.SauvegardeImpossible;
import personnel.DateIncoherenteException;
import personnel.DateInvalideException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.awt.event.HierarchyEvent; // Importation ajoutée

/**
 * Panneau de gestion du compte Super-Administrateur (root).
 * Permet au Super-Administrateur de modifier ses propres informations.
 */
public class RootAccountPanel extends JPanel {
    private PersonnelGUI mainFrame;
    private Employe rootEmploye; // Référence à l'employé root
    private JTextField nomField;
    private JTextField prenomField;
    private JTextField mailField;
    private JPasswordField passwordField;
    private JTextField dateArriveeField;
    private JTextField dateDepartField;
    private JLabel messageLabel; // Pour afficher les messages de succès/erreur

    /**
     * Constructeur du panneau de gestion du compte root.
     * @param mainFrame La fenêtre principale de l'application.
     */
    public RootAccountPanel(PersonnelGUI mainFrame) {
        this.mainFrame = mainFrame;
        this.rootEmploye = mainFrame.getGestionPersonnel().getRoot(); // Récupère l'employé root

        setLayout(new BorderLayout(20, 20));
        setBackground(Style.PRIMARY_BACKGROUND);
        setBorder(Style.PADDING_BORDER);

        // Vérification des droits : s'assurer que l'utilisateur connecté est bien le root
        Employe utilisateurConnecte = mainFrame.getUtilisateurConnecte();
        if (utilisateurConnecte == null || !utilisateurConnecte.estRoot()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Accès refusé. Seul le Super-Administrateur peut gérer ce compte.",
                    "Droits insuffisants",
                    JOptionPane.WARNING_MESSAGE);
            mainFrame.showPanel(PersonnelGUI.MAIN_MENU_PANEL); // Retour au menu principal
            return;
        }

        // Titre du panneau
        JLabel titleLabel = new JLabel("Gérer le Compte Super-Administrateur");
        titleLabel.setFont(Style.FONT_TITLE);
        titleLabel.setForeground(Style.ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Panneau de saisie des informations
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(Style.COMPONENT_PADDING);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8); // Espacement entre les composants
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Champs de saisie
        JLabel nomLabel = new JLabel("Nom:");
        Style.styleLabel(nomLabel);
        nomField = new JTextField(25);
        Style.styleTextField(nomField);
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(nomLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(nomField, gbc);

        JLabel prenomLabel = new JLabel("Prénom:");
        Style.styleLabel(prenomLabel);
        prenomField = new JTextField(25);
        Style.styleTextField(prenomField);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(prenomLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(prenomField, gbc);

        JLabel mailLabel = new JLabel("Mail:");
        Style.styleLabel(mailLabel);
        mailField = new JTextField(25);
        Style.styleTextField(mailField);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(mailLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(mailField, gbc);

        JLabel passwordLabel = new JLabel("Mot de passe:");
        Style.styleLabel(passwordLabel);
        passwordField = new JPasswordField(25);
        Style.stylePasswordField(passwordField);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(passwordField, gbc);

        JLabel dateArriveeLabel = new JLabel("Date d'arrivée (AAAA-MM-JJ):");
        Style.styleLabel(dateArriveeLabel);
        dateArriveeField = new JTextField(25);
        Style.styleTextField(dateArriveeField);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(dateArriveeLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(dateArriveeField, gbc);

        JLabel dateDepartLabel = new JLabel("Date de départ (AAAA-MM-JJ, optionnel):");
        Style.styleLabel(dateDepartLabel);
        dateDepartField = new JTextField(25);
        Style.styleTextField(dateDepartField);
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(dateDepartLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(dateDepartField, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JButton updateButton = new JButton("Mettre à jour le profil");
        Style.styleButton(updateButton);
        updateButton.addActionListener(e -> updateRootAccount());
        buttonPanel.add(updateButton);

        JButton backButton = new JButton("Retour au Menu Principal");
        Style.styleButton(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(PersonnelGUI.MAIN_MENU_PANEL));
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Message de statut
        messageLabel = new JLabel("");
        messageLabel.setFont(Style.FONT_LABEL);
        messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // Ajout du messageLabel au bas du formPanel pour qu'il soit au-dessus des boutons
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2; // S'étend sur 2 colonnes
        formPanel.add(messageLabel, gbc);


        // Charge les informations de l'employé root lorsque le panneau est affiché
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                loadRootDetails();
            }
        });
    }

    /**
     * Charge les informations actuelles de l'employé root dans les champs de saisie.
     */
    private void loadRootDetails() {
        nomField.setText(rootEmploye.getNom());
        prenomField.setText(rootEmploye.getPrenom());
        mailField.setText(rootEmploye.getMail());
        passwordField.setText(rootEmploye.getPassword()); // Afficher le mot de passe actuel
        dateArriveeField.setText(rootEmploye.getDateArrivee() != null ? rootEmploye.getDateArrivee().toString() : "");
        dateDepartField.setText(rootEmploye.getDateDepart() != null ? rootEmploye.getDateDepart().toString() : "");
        messageLabel.setText(""); // Efface les messages précédents
    }

    /**
     * Met à jour les informations de l'employé root avec les données saisies.
     */
    private void updateRootAccount() {
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String mail = mailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String dateArriveeStr = dateArriveeField.getText().trim();
        String dateDepartStr = dateDepartField.getText().trim();

        if (nom.isEmpty() || prenom.isEmpty() || mail.isEmpty() || password.isEmpty() || dateArriveeStr.isEmpty()) {
            displayMessage("Tous les champs obligatoires doivent être remplis.", Style.ERROR_COLOR);
            return;
        }

        try {
            LocalDate dateArrivee = LocalDate.parse(dateArriveeStr);
            LocalDate dateDepart = null;
            if (!dateDepartStr.isEmpty()) {
                dateDepart = LocalDate.parse(dateDepartStr);
            }

            // Mettre à jour l'objet Employe root
            rootEmploye.setNom(nom);
            rootEmploye.setPrenom(prenom);
            rootEmploye.setMail(mail);
            rootEmploye.setPassword(password);
            rootEmploye.setDateArrivee(dateArrivee);
            rootEmploye.setDateDepart(dateDepart);

            // Appel explicite à la méthode update de GestionPersonnel pour persister les changements
            mainFrame.getGestionPersonnel().update(rootEmploye); 

            displayMessage("Profil 'root' mis à jour avec succès !", Style.SUCCESS_COLOR);
        } catch (DateTimeParseException e) {
            displayMessage("Format de date invalide. Utilisez le format AAAA-MM-JJ.", Style.ERROR_COLOR);
        } catch (DateIncoherenteException | DateInvalideException e) {
            displayMessage("Erreur de date : " + e.getMessage(), Style.ERROR_COLOR);
        } catch (SauvegardeImpossible e) {
            displayMessage("Erreur lors de la sauvegarde du profil 'root' : " + e.getMessage(), Style.ERROR_COLOR);
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            displayMessage("Erreur : " + e.getMessage(), Style.ERROR_COLOR);
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
