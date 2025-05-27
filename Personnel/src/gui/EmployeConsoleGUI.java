package gui;

import personnel.Employe;
import personnel.GestionPersonnel;
import personnel.SauvegardeImpossible;
// import personnel.DateIncoherenteException; // Cette importation peut être supprimée si elle n'est plus utilisée directement
                                            // dans un bloc catch spécifique et distinct.
import personnel.DateIncoherenteException; // Garder l'importation car elle est utilisée pour le type de la variable employe.

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class EmployeConsoleGUI {

    private GestionPersonnel gestionPersonnel;

    public EmployeConsoleGUI(GestionPersonnel gestionPersonnel) {
        this.gestionPersonnel = gestionPersonnel;
    }

    public void showEmployeDetails(Employe employe, JFrame parentFrame) {
        JDialog dialog = new JDialog(parentFrame, "Détails de l'Employé : " + employe.getNom() + " " + employe.getPrenom(), true); // Modal
        
        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        dialog.add(fieldsPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nomField = new JTextField(employe.getNom(), 20);
        JTextField prenomField = new JTextField(employe.getPrenom(), 20);
        JTextField mailField = new JTextField(employe.getMail(), 20);
        JPasswordField passwordField = new JPasswordField(employe.getPassword(), 20);
        JTextField dateArriveeField = new JTextField(employe.getDateArrivee() != null ? employe.getDateArrivee().toString() : "", 20);
        JTextField dateDepartField = new JTextField(employe.getDateDepart() != null ? employe.getDateDepart().toString() : "", 20);

        int row = 0;
        addLabeledField(fieldsPanel, gbc, "Nom :", nomField, row++);
        addLabeledField(fieldsPanel, gbc, "Prénom :", prenomField, row++);
        addLabeledField(fieldsPanel, gbc, "Mail :", mailField, row++);
        addLabeledField(fieldsPanel, gbc, "Mot de passe :", passwordField, row++);
        addLabeledField(fieldsPanel, gbc, "Date d'arrivée (AAAA-MM-JJ) :", dateArriveeField, row++);
        addLabeledField(fieldsPanel, gbc, "Date de départ (AAAA-MM-JJ) :", dateDepartField, row++);


        JButton saveButton = new JButton("Enregistrer les modifications");
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(saveButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            try {
                employe.setNom(nomField.getText());
                employe.setPrenom(prenomField.getText());
                employe.setMail(mailField.getText());
                employe.setPassword(new String(passwordField.getPassword()));

                LocalDate newDateArrivee = null;
                if (!dateArriveeField.getText().trim().isEmpty()) {
                    newDateArrivee = LocalDate.parse(dateArriveeField.getText());
                }
                employe.setDateArrivee(newDateArrivee);

                LocalDate newDateDepart = null;
                if (!dateDepartField.getText().trim().isEmpty()) {
                    newDateDepart = LocalDate.parse(dateDepartField.getText());
                }
                employe.setDateDepart(newDateDepart);

                JOptionPane.showMessageDialog(dialog, "Employé mis à jour avec succès.");
                dialog.dispose();
            // MODIFICATION ICI : Suppression de DateIncoherenteException du catch block
            } catch (SauvegardeImpossible | IllegalArgumentException | DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Erreur de mise à jour : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.pack();
        dialog.setLocationRelativeTo(parentFrame);
        dialog.setVisible(true);
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
}