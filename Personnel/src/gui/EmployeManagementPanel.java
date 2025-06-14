
package gui;

import personnel.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Panneau de gestion des employés d'une ligue spécifique.
 * Accessible par le Super-Administrateur ou l'Administrateur de la ligue.
 */
public class EmployeManagementPanel extends JPanel {
    private static final long serialVersionUID = 1L; // Ajout du serialVersionUID
    private PersonnelGUI mainFrame;
    private Ligue ligue;
    private JTable employeTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton backButton;
    private JLabel panelTitleLabel;

    public EmployeManagementPanel(PersonnelGUI mainFrame, Ligue ligue) {
        this.mainFrame = mainFrame;
        this.ligue = ligue;
        setLayout(new BorderLayout(20, 20));
        setBackground(Style.PRIMARY_BACKGROUND);
        setBorder(Style.PADDING_BORDER);

        // Titre du panneau
        String title = (ligue != null) ? "Gestion des employés de la ligue : " + ligue.getNom() : "Gestion de tous les employés (Root)";
        panelTitleLabel = new JLabel(title);
        panelTitleLabel.setFont(Style.FONT_TITLE);
        panelTitleLabel.setForeground(Style.ACCENT_COLOR);
        panelTitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(panelTitleLabel, BorderLayout.NORTH);

        // Initialisation du tableau des employés
        String[] columnNames = {"ID", "Nom", "Prénom", "Email", "Ligue", "Date Arrivée", "Date Départ", "Root"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        employeTable = new JTable(tableModel);
        employeTable.setFont(Style.FONT_TEXT);
        employeTable.setBackground(Style.SECONDARY_BACKGROUND);
        employeTable.setForeground(Style.TEXT_COLOR);
        employeTable.setSelectionBackground(Style.ACCENT_COLOR_LIGHT);
        employeTable.setSelectionForeground(Color.WHITE);
        employeTable.setRowHeight(25);
        employeTable.getTableHeader().setFont(Style.FONT_SUBTITLE);
        employeTable.getTableHeader().setBackground(Style.PRIMARY_COLOR);
        employeTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(employeTable);
        scrollPane.getViewport().setBackground(Style.SECONDARY_BACKGROUND);
        add(scrollPane, BorderLayout.CENTER);

        // Panneau des boutons d'action
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        addButton = new JButton("Ajouter Employé");
        Style.styleButton(addButton);
        addButton.addActionListener(e -> addEmploye());
        buttonPanel.add(addButton);

        editButton = new JButton("Modifier Employé");
        Style.styleButton(editButton);
        editButton.addActionListener(e -> editSelectedEmploye());
        buttonPanel.add(editButton);

        deleteButton = new JButton("Supprimer Employé");
        Style.styleButton(deleteButton);
        deleteButton.addActionListener(e -> deleteSelectedEmploye());
        buttonPanel.add(deleteButton);

        backButton = new JButton("Retour");
        Style.styleButton(backButton);
        backButton.addActionListener(e -> {
            if (ligue != null) {
                mainFrame.showPanel(PersonnelGUI.LIGUE_DETAILS_PANEL);
            } else {
                mainFrame.showPanel(PersonnelGUI.MAIN_MENU_PANEL);
            }
        });
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Met à jour le tableau lorsque le panneau est affiché
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                updateEmployeTable();
            }
        });
    }

    private void updateEmployeTable() {
        tableModel.setRowCount(0);

        Set<Employe> employes = (ligue != null) ? ligue.getEmployes() : mainFrame.getGestionPersonnel().getEmployes();

        List<Employe> sortedEmployes = employes.stream()
                .sorted(Comparator.comparing(Employe::getNom).thenComparing(Employe::getPrenom))
                .collect(Collectors.toList());

        for (Employe emp : sortedEmployes) {
            Object[] rowData = {
                    emp.getId(),
                    emp.getNom(),
                    emp.getPrenom(),
                    emp.getMail(),
                    (emp.getLigue() != null) ? emp.getLigue().getNom() : "N/A",
                    (emp.getDateArrivee() != null) ? emp.getDateArrivee().toString() : "N/A",
                    (emp.getDateDepart() != null) ? emp.getDateDepart().toString() : "N/A",
                    emp.estRoot() ? "Oui" : "Non"
            };
            tableModel.addRow(rowData);
        }
    }

    private void addEmploye() {
        // Implémentation pour ajouter un employé
        JOptionPane.showMessageDialog(mainFrame, "Fonctionnalité Ajouter Employé non implémentée.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }


 private void editSelectedEmploye() {
     int selectedRow = employeTable.getSelectedRow();
     if (selectedRow == -1) {
         JOptionPane.showMessageDialog(this, "Veuillez sélectionner un employé à modifier.", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
         return;
     }

     int employeId = (int) tableModel.getValueAt(selectedRow, 0);
	 Employe employeToEdit = mainFrame.getGestionPersonnel().getEmploye(employeId);

	 if (employeToEdit == null) {
	     JOptionPane.showMessageDialog(this, "Employé introuvable.", "Erreur", JOptionPane.ERROR_MESSAGE);
	     return;
	 }

	 // Créer une boîte de dialogue pour l'édition
	 JDialog editDialog = new JDialog(mainFrame, "Modifier l'employé", true);
	 editDialog.setLayout(new BorderLayout(10, 10));
	 editDialog.setBackground(Style.PRIMARY_BACKGROUND);
	 editDialog.getRootPane().setBorder(Style.PADDING_BORDER);

	 JPanel formPanel = new JPanel(new GridBagLayout());
	 formPanel.setOpaque(false);
	 GridBagConstraints gbc = new GridBagConstraints();
	 gbc.insets = new Insets(5, 5, 5, 5);
	 gbc.fill = GridBagConstraints.HORIZONTAL;

	 // Champs de saisie, pré-remplis avec les données de l'employé
	 JTextField nomField = new JTextField(employeToEdit.getNom());
	 JTextField prenomField = new JTextField(employeToEdit.getPrenom());
	 JTextField mailField = new JTextField(employeToEdit.getMail());
	 JPasswordField passwordField = new JPasswordField(""); // Ne pas afficher le mot de passe actuel
	 JTextField dateArriveeField = new JTextField(employeToEdit.getDateArrivee() != null ? employeToEdit.getDateArrivee().toString() : "");
	 JTextField dateDepartField = new JTextField(employeToEdit.getDateDepart() != null ? employeToEdit.getDateDepart().toString() : "");

	 // Appliquer les styles
	 Style.styleTextField(nomField);
	 Style.styleTextField(prenomField);
	 Style.styleTextField(mailField);
	 Style.stylePasswordField(passwordField);
	 Style.styleTextField(dateArriveeField);
	 Style.styleTextField(dateDepartField);

	 // Labels et ajout au panel
	 gbc.gridx = 0; gbc.gridy = 0; formPanel.add(Style.createStyledLabel("Nom:"), gbc);
	 gbc.gridx = 1; formPanel.add(nomField, gbc);
	 gbc.gridx = 0; gbc.gridy = 1; formPanel.add(Style.createStyledLabel("Prénom:"), gbc);
	 gbc.gridx = 1; formPanel.add(prenomField, gbc);
	 gbc.gridx = 0; gbc.gridy = 2; formPanel.add(Style.createStyledLabel("Mail:"), gbc);
	 gbc.gridx = 1; formPanel.add(mailField, gbc);
	 gbc.gridx = 0; gbc.gridy = 3; formPanel.add(Style.createStyledLabel("Nouveau Mot de passe (laisser vide pour ne pas changer):"), gbc);
	 gbc.gridx = 1; formPanel.add(passwordField, gbc);
	 gbc.gridx = 0; gbc.gridy = 4; formPanel.add(Style.createStyledLabel("Date d'arrivée (AAAA-MM-JJ):"), gbc);
	 gbc.gridx = 1; formPanel.add(dateArriveeField, gbc);
	 gbc.gridx = 0; gbc.gridy = 5; formPanel.add(Style.createStyledLabel("Date de départ (AAAA-MM-JJ, laisser vide si N/A):"), gbc);
	 gbc.gridx = 1; formPanel.add(dateDepartField, gbc);

	 // Boutons d'action
	 JButton updateButton = Style.createStyledButton("Mettre à jour");
	 JButton cancelButton = Style.createStyledButton("Annuler");

	 JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
	 buttonPanel.setOpaque(false);
	 buttonPanel.add(updateButton);
	 buttonPanel.add(cancelButton);

	 // Ajout des panneaux à la boîte de dialogue
	 editDialog.add(formPanel, BorderLayout.CENTER);
	 editDialog.add(buttonPanel, BorderLayout.SOUTH);

	 // Logique des boutons
	 updateButton.addActionListener(e -> {
	     try {
	         String nom = nomField.getText();
	         String prenom = prenomField.getText();
	         String mail = mailField.getText();
	         String password = new String(passwordField.getPassword());
	         LocalDate dateArrivee = dateArriveeField.getText().isEmpty() ? null : LocalDate.parse(dateArriveeField.getText());
	         LocalDate dateDepart = dateDepartField.getText().isEmpty() ? null : LocalDate.parse(dateDepartField.getText());

	         // Valider les champs
	         if (nom.trim().isEmpty() || prenom.trim().isEmpty() || mail.trim().isEmpty()) {
	             JOptionPane.showMessageDialog(editDialog, "Le nom, prénom et mail ne peuvent pas être vides.", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
	             return;
	         }

	         // Mettre à jour l'objet Employe
	         employeToEdit.setNom(nom);
	         employeToEdit.setPrenom(prenom);
	         employeToEdit.setMail(mail);
	         if (!password.isEmpty()) {
	             employeToEdit.setPassword(password); // Mettre à jour le mot de passe seulement s'il est saisi
	         }
	         employeToEdit.setDateArrivee(dateArrivee);
	         employeToEdit.setDateDepart(dateDepart);

	         mainFrame.getGestionPersonnel().update(employeToEdit); // Persister les changements
	         JOptionPane.showMessageDialog(editDialog, "Employé mis à jour avec succès !", "Succès", JOptionPane.INFORMATION_MESSAGE);
	         updateEmployeTable(); // Rafraîchir le tableau
	         editDialog.dispose(); // Fermer la boîte de dialogue
	     } catch (DateTimeParseException ex) {
	         JOptionPane.showMessageDialog(editDialog, "Format de date invalide. Utilisez le format AAAA-MM-JJ.", "Erreur de date", JOptionPane.ERROR_MESSAGE);
	     } catch (DateIncoherenteException | DateInvalideException ex) {
	         JOptionPane.showMessageDialog(editDialog, "Erreur de date : " + ex.getMessage(), "Erreur de date", JOptionPane.ERROR_MESSAGE);
	     } catch (SauvegardeImpossible ex) {
	         JOptionPane.showMessageDialog(editDialog, "Erreur lors de la sauvegarde de l'employé : " + ex.getMessage(), "Erreur de sauvegarde", JOptionPane.ERROR_MESSAGE);
	         ex.printStackTrace();
	     } catch (IllegalArgumentException ex) {
	         JOptionPane.showMessageDialog(editDialog, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
	     }
	 });

	 cancelButton.addActionListener(e -> editDialog.dispose()); // Fermer la boîte de dialogue sans sauvegarder

	 editDialog.pack();
	 editDialog.setLocationRelativeTo(this); // Centrer par rapport au panneau parent
	 editDialog.setVisible(true);
 }

    private void deleteSelectedEmploye() {
        int selectedRow = employeTable.getSelectedRow();
        if (selectedRow != -1) {
            JOptionPane.showMessageDialog(mainFrame, "Fonctionnalité Supprimer Employé non implémentée.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Veuillez sélectionner un employé à supprimer.", "Sélection requise", JOptionPane.WARNING_MESSAGE);
        }
    }
}
