package gui;

import personnel.GestionPersonnel;
import personnel.Ligue;
import personnel.Employe;
import personnel.SauvegardeImpossible;
import personnel.ImpossibleDeSupprimerRoot;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class LigueConsoleGUI {

    private GestionPersonnel gestionPersonnel;
    private EmployeConsoleGUI employeConsoleGUI; // Pour gérer les détails des employés
    private JPanel liguesPanel; // Le panneau principal pour la gestion des ligues
    private DefaultListModel<Ligue> liguesListModel;
    private JList<Ligue> liguesJList;
    private JButton addLigueButton, editLigueButton, deleteLigueButton;

    public LigueConsoleGUI(GestionPersonnel gestionPersonnel, EmployeConsoleGUI employeConsoleGUI) {
        this.gestionPersonnel = gestionPersonnel;
        this.employeConsoleGUI = employeConsoleGUI;
        initComponents();
        loadLigues();
    }

    private void initComponents() {
        liguesPanel = new JPanel(new BorderLayout());

        // Zone de la liste des ligues
        liguesListModel = new DefaultListModel<>();
        liguesJList = new JList<>(liguesListModel);
        liguesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane liguesScrollPane = new JScrollPane(liguesJList);
        liguesPanel.add(liguesScrollPane, BorderLayout.CENTER);

        // Panneau des boutons pour les ligues
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        addLigueButton = new JButton("Ajouter une Ligue");
        editLigueButton = new JButton("Éditer la Ligue sélectionnée");
        deleteLigueButton = new JButton("Supprimer la Ligue sélectionnée");

        buttonPanel.add(addLigueButton);
        buttonPanel.add(editLigueButton);
        buttonPanel.add(deleteLigueButton);
        liguesPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        addLigueButton.addActionListener(e -> addLigue());
        editLigueButton.addActionListener(e -> editSelectedLigue());
        deleteLigueButton.addActionListener(e -> deleteSelectedLigue());

        // Activer/Désactiver les boutons d'édition/suppression en fonction de la sélection
        liguesJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean selected = liguesJList.getSelectedIndex() != -1;
                    editLigueButton.setEnabled(selected);
                    deleteLigueButton.setEnabled(selected);
                }
            }
        });
        editLigueButton.setEnabled(false); // Désactivé par défaut
        deleteLigueButton.setEnabled(false); // Désactivé par défaut
    }

    private void loadLigues() {
        liguesListModel.clear();
        SortedSet<Ligue> ligues = gestionPersonnel.getLigues();
        for (Ligue ligue : ligues) {
            liguesListModel.addElement(ligue);
        }
    }

    public JPanel getLiguesPanel() {
        return liguesPanel;
    }

    private void addLigue() {
        String nomLigue = JOptionPane.showInputDialog(liguesPanel, "Nom de la nouvelle ligue :");
        if (nomLigue != null && !nomLigue.trim().isEmpty()) {
            try {
                gestionPersonnel.addLigue(nomLigue.trim());
                loadLigues(); // Recharger la liste après ajout
                JOptionPane.showMessageDialog(liguesPanel, "Ligue ajoutée avec succès.");
            } catch (SauvegardeImpossible e) {
                JOptionPane.showMessageDialog(liguesPanel, "Erreur lors de l'ajout de la ligue : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else if (nomLigue != null) { // Si l'utilisateur a entré une chaîne vide après avoir cliqué sur OK
            JOptionPane.showMessageDialog(liguesPanel, "Le nom de la ligue ne peut pas être vide.", "Erreur de saisie", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void editSelectedLigue() {
        Ligue selectedLigue = liguesJList.getSelectedValue();
        if (selectedLigue != null) {
            showLigueDetailsDialog(selectedLigue);
        }
    }

    private void deleteSelectedLigue() {
        Ligue selectedLigue = liguesJList.getSelectedValue();
        if (selectedLigue != null) {
            int response = JOptionPane.showConfirmDialog(liguesPanel,
                    "Êtes-vous sûr de vouloir supprimer la ligue '" + selectedLigue.getNom() + "' et tous ses employés ?",
                    "Confirmer la suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (response == JOptionPane.YES_OPTION) {
                try {
                    selectedLigue.remove();
                    loadLigues(); // Recharger la liste après suppression
                    JOptionPane.showMessageDialog(liguesPanel, "Ligue supprimée avec succès.");
                } catch (SauvegardeImpossible | ImpossibleDeSupprimerRoot e) {
                    JOptionPane.showMessageDialog(liguesPanel, "Erreur lors de la suppression de la ligue : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    // Méthode pour afficher le dialogue d'édition/détails d'une ligue
    private void showLigueDetailsDialog(Ligue ligue) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(liguesPanel), "Détails de la Ligue : " + ligue.getNom(), true);
        dialog.setLayout(new BorderLayout());

        // Onglets pour la gestion des détails de la ligue et des employés
        JTabbedPane ligueTabbedPane = new JTabbedPane();
        dialog.add(ligueTabbedPane, BorderLayout.CENTER);

        // --- Onglet "Détails de la Ligue" ---
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomLigueField = new JTextField(ligue.getNom(), 20);
        JLabel adminLabel = new JLabel("Administrateur actuel : " + (ligue.getAdministrateur() != null ? ligue.getAdministrateur().getNom() + " " + ligue.getAdministrateur().getPrenom() : "Aucun"));

        int row = 0;
        addLabeledField(detailsPanel, gbc, "Nom de la Ligue :", nomLigueField, row++);
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        detailsPanel.add(adminLabel, gbc);

        JButton saveLigueDetailsButton = new JButton("Enregistrer les détails");
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        detailsPanel.add(saveLigueDetailsButton, gbc);

        saveLigueDetailsButton.addActionListener(e -> {
            try {
                String newNom = nomLigueField.getText().trim();
                ligue.setNom(newNom); // Peut lancer IllegalArgumentException ou SauvegardeImpossible
                JOptionPane.showMessageDialog(dialog, "Nom de la ligue mis à jour.");
                loadLigues(); // Recharger la liste principale des ligues
                dialog.setTitle("Détails de la Ligue : " + ligue.getNom()); // Mettre à jour le titre du dialogue
            } catch (SauvegardeImpossible | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur lors de la mise à jour du nom : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        ligueTabbedPane.addTab("Détails", detailsPanel);

        // --- Onglet "Employés de la Ligue" ---
        JPanel employesPanel = new JPanel(new BorderLayout());
        DefaultListModel<Employe> employesListModel = new DefaultListModel<>();
        JList<Employe> employesJList = new JList<>(employesListModel);
        employesJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane employesScrollPane = new JScrollPane(employesJList);
        employesPanel.add(employesScrollPane, BorderLayout.CENTER);

        // Boutons pour les employés de cette ligue
        JPanel employeButtonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addEmployeButton = new JButton("Ajouter un Employé");
        JButton editEmployeButton = new JButton("Éditer l'Employé");
        JButton deleteEmployeButton = new JButton("Supprimer l'Employé");
        JButton setAdminButton = new JButton("Définir comme Admin");

        employeButtonsPanel.add(addEmployeButton);
        employeButtonsPanel.add(editEmployeButton);
        employeButtonsPanel.add(deleteEmployeButton);
        employeButtonsPanel.add(setAdminButton);
        employesPanel.add(employeButtonsPanel, BorderLayout.SOUTH);

        ligueTabbedPane.addTab("Employés", employesPanel);

        // Listeners pour les employés
        addEmployeButton.addActionListener(e -> addEmployeToLigue(ligue, employesListModel));
        editEmployeButton.addActionListener(e -> {
            Employe selectedEmploye = employesJList.getSelectedValue();
            if (selectedEmploye != null) {
                employeConsoleGUI.showEmployeDetails(selectedEmploye, (JFrame) SwingUtilities.getWindowAncestor(liguesPanel));
                loadEmployesForLigue(ligue, employesListModel); // Recharger après modification
            } else {
                JOptionPane.showMessageDialog(dialog, "Veuillez sélectionner un employé à éditer.", "Aucun employé sélectionné", JOptionPane.WARNING_MESSAGE);
            }
        });
        deleteEmployeButton.addActionListener(e -> deleteEmployeFromLigue(ligue, employesListModel, employesJList.getSelectedValue()));
        setAdminButton.addActionListener(e -> setLigueAdministrator(ligue, employesJList.getSelectedValue(), adminLabel));

        // Activer/Désactiver les boutons d'édition/suppression d'employé
        employesJList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    boolean selected = employesJList.getSelectedIndex() != -1;
                    editEmployeButton.setEnabled(selected);
                    deleteEmployeButton.setEnabled(selected);
                    setAdminButton.setEnabled(selected);
                }
            }
        });
        editEmployeButton.setEnabled(false);
        deleteEmployeButton.setEnabled(false);
        setAdminButton.setEnabled(false);


        // Charger les employés de la ligue au démarrage de l'onglet
        ligueTabbedPane.addChangeListener(e -> {
            if (ligueTabbedPane.getSelectedComponent() == employesPanel) {
                loadEmployesForLigue(ligue, employesListModel);
            }
        });

        // Charger les employés la première fois que l'onglet est sélectionné (ou si c'est le premier onglet)
        loadEmployesForLigue(ligue, employesListModel);


        dialog.pack();
        dialog.setLocationRelativeTo((Frame) SwingUtilities.getWindowAncestor(liguesPanel));
        dialog.setVisible(true);

        // Recharger la liste principale des ligues lorsque le dialogue se ferme
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent windowEvent) {
                loadLigues();
            }
        });
    }


    private void addLabeledField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int row) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(field, gbc);
    }

    private void loadEmployesForLigue(Ligue ligue, DefaultListModel<Employe> model) {
        model.clear();
        for (Employe employe : ligue.getEmployes()) {
            model.addElement(employe);
        }
    }

    private void addEmployeToLigue(Ligue ligue, DefaultListModel<Employe> employesListModel) {
        // Dialogue pour ajouter un employé
        JDialog addEmployeDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(liguesPanel), "Ajouter un Employé à " + ligue.getNom(), true);
        
        // Créez un JPanel pour contenir les champs de texte
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        addEmployeDialog.add(fieldsPanel, BorderLayout.CENTER); // Ajoutez ce panel au dialogue

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomField = new JTextField(20);
        JTextField prenomField = new JTextField(20);
        JTextField mailField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);
        JTextField dateArriveeField = new JTextField(20);
        JTextField dateDepartField = new JTextField(20);

        int row = 0;
        // Appelez addLabeledField avec le nouveau JPanel (fieldsPanel)
        addLabeledField(fieldsPanel, gbc, "Nom :", nomField, row++);
        addLabeledField(fieldsPanel, gbc, "Prénom :", prenomField, row++);
        addLabeledField(fieldsPanel, gbc, "Mail :", mailField, row++);
        addLabeledField(fieldsPanel, gbc, "Mot de passe :", passwordField, row++);
        addLabeledField(fieldsPanel, gbc, "Date d'arrivée (AAAA-MM-JJ) :", dateArriveeField, row++);
        addLabeledField(fieldsPanel, gbc, "Date de départ (AAAA-MM-JJ) :", dateDepartField, row++);

        JButton saveButton = new JButton("Ajouter");
        // Placez le bouton en bas du dialogue
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(saveButton);
        addEmployeDialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            try {
                String nom = nomField.getText().trim();
                String prenom = prenomField.getText().trim();
                String mail = mailField.getText().trim();
                String password = new String(passwordField.getPassword());
                LocalDate dateArrivee = null;
                LocalDate dateDepart = null;

                if (!dateArriveeField.getText().trim().isEmpty()) {
                    dateArrivee = LocalDate.parse(dateArriveeField.getText().trim());
                }
                if (!dateDepartField.getText().trim().isEmpty()) {
                    dateDepart = LocalDate.parse(dateDepartField.getText().trim());
                }

                if (nom.isEmpty() || prenom.isEmpty() || mail.isEmpty() || password.isEmpty()) {
                    throw new IllegalArgumentException("Tous les champs (sauf dates) sont obligatoires.");
                }

                Employe newEmploye = ligue.addEmploye(nom, prenom, mail, password, dateArrivee, dateDepart);
                employesListModel.addElement(newEmploye); // Ajoute à la liste affichée
                JOptionPane.showMessageDialog(addEmployeDialog, "Employé ajouté avec succès.");
                addEmployeDialog.dispose();
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(addEmployeDialog, "Format de date invalide. Utilisez AAAA-MM-JJ.", "Erreur de Date", JOptionPane.ERROR_MESSAGE);
            } catch (SauvegardeImpossible | IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(addEmployeDialog, "Erreur lors de l'ajout de l'employé : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        addEmployeDialog.pack();
        addEmployeDialog.setLocationRelativeTo((Frame) SwingUtilities.getWindowAncestor(liguesPanel));
        addEmployeDialog.setVisible(true);
    }

    private void deleteEmployeFromLigue(Ligue ligue, DefaultListModel<Employe> employesListModel, Employe employeToDelete) {
        if (employeToDelete == null) {
            JOptionPane.showMessageDialog(liguesPanel, "Veuillez sélectionner un employé à supprimer.", "Aucun employé sélectionné", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int response = JOptionPane.showConfirmDialog(liguesPanel,
                "Êtes-vous sûr de vouloir supprimer l'employé '" + employeToDelete.getNom() + " " + employeToDelete.getPrenom() + "' ?",
                "Confirmer la suppression", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            try {
                employeToDelete.remove();
                employesListModel.removeElement(employeToDelete); // Supprime de la liste affichée
                JOptionPane.showMessageDialog(liguesPanel, "Employé supprimé avec succès.");
            } catch (ImpossibleDeSupprimerRoot e) {
                JOptionPane.showMessageDialog(liguesPanel, "Erreur : " + e.getMessage(), "Erreur de Suppression", JOptionPane.ERROR_MESSAGE);
            } catch (SauvegardeImpossible e) {
                JOptionPane.showMessageDialog(liguesPanel, "Erreur lors de la suppression de l'employé : " + e.getMessage(), "Erreur de Sauvegarde", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void setLigueAdministrator(Ligue ligue, Employe employeToSetAsAdmin, JLabel adminLabel) {
        if (employeToSetAsAdmin == null) {
            JOptionPane.showMessageDialog(liguesPanel, "Veuillez sélectionner un employé pour définir comme administrateur.", "Aucun employé sélectionné", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            ligue.setAdministrateur(employeToSetAsAdmin);
            adminLabel.setText("Administrateur actuel : " + employeToSetAsAdmin.getNom() + " " + employeToSetAsAdmin.getPrenom());
            JOptionPane.showMessageDialog(liguesPanel, "Administrateur de la ligue mis à jour avec succès.");
        } catch (IllegalArgumentException | SauvegardeImpossible e) {
            JOptionPane.showMessageDialog(liguesPanel, "Erreur lors de la définition de l'administrateur : " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}