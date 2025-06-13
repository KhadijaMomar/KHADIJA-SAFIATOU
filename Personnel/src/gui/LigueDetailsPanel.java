// Fichier: src/gui/LigueDetailsPanel.java
package gui;

import personnel.GestionPersonnel;
import personnel.Ligue;
import personnel.Employe;
import personnel.SauvegardeImpossible;
import personnel.DroitsInsuffisants;
import personnel.ImpossibleDeSupprimerRoot; 
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Set;
import java.util.Comparator; 
import java.awt.event.HierarchyEvent; 
import java.util.ArrayList; 
import java.util.List; 

/**
 * Panneau d'affichage et de gestion des détails d'une ligue spécifique.
 * Accessible par le Super-Administrateur.
 */
public class LigueDetailsPanel extends JPanel {
    private PersonnelGUI mainFrame;
    private Ligue ligue; // La ligue dont les détails sont affichés
    private JLabel ligueNameLabel;
    private JLabel adminLabel;
    private JButton renameButton;
    private JButton changeAdminButton;
    private JButton manageEmployesButton;
    private JButton deleteLigueButton;
    private JButton backButton;

    /**
     * Constructeur du panneau des détails de la ligue.
     * @param mainFrame La fenêtre principale de l'application.
     * @param ligue La ligue à afficher et gérer.
     */
    public LigueDetailsPanel(PersonnelGUI mainFrame, Ligue ligue) {
        this.mainFrame = mainFrame;
        this.ligue = ligue;
        setLayout(new BorderLayout(20, 20));
        setBackground(Style.PRIMARY_BACKGROUND);
        setBorder(Style.PADDING_BORDER);

        // Vérification des droits (double sécurité, déjà fait dans LigueManagementPanel)
        Employe utilisateurConnecte = mainFrame.getUtilisateurConnecte();
        if (utilisateurConnecte == null || !utilisateurConnecte.estRoot()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Accès refusé. Seul le Super-Administrateur peut gérer les détails des ligues.",
                    "Droits insuffisants",
                    JOptionPane.WARNING_MESSAGE);
            mainFrame.showPanel(PersonnelGUI.MAIN_MENU_PANEL);
            return;
        }

        // Titre du panneau
        ligueNameLabel = new JLabel("Détails de la Ligue : " + ligue.getNom());
        ligueNameLabel.setFont(Style.FONT_TITLE);
        ligueNameLabel.setForeground(Style.ACCENT_COLOR);
        ligueNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(ligueNameLabel, BorderLayout.NORTH);

        // Panneau d'informations de la ligue
        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setOpaque(false);
        infoPanel.setBorder(Style.COMPONENT_PADDING);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Nom de la ligue (déjà dans le titre, mais peut être répété ou détaillé)
        JLabel ligueIdLabel = new JLabel("ID de la Ligue:");
        Style.styleLabel(ligueIdLabel);
        gbc.gridx = 0; gbc.gridy = 0;
        infoPanel.add(ligueIdLabel, gbc);
        JLabel ligueIdValue = new JLabel(String.valueOf(ligue.getId()));
        Style.styleLabel(ligueIdValue);
        gbc.gridx = 1; gbc.gridy = 0;
        infoPanel.add(ligueIdValue, gbc);

        // Administrateur de la ligue
        JLabel adminTitleLabel = new JLabel("Administrateur:");
        Style.styleLabel(adminTitleLabel);
        gbc.gridx = 0; gbc.gridy = 1;
        infoPanel.add(adminTitleLabel, gbc);
        adminLabel = new JLabel();
        Style.styleLabel(adminLabel);
        gbc.gridx = 1; gbc.gridy = 1;
        infoPanel.add(adminLabel, gbc);

        add(infoPanel, BorderLayout.CENTER);

        // Panneau des boutons d'action - MODIFICATION PRINCIPALE ICI
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Espacement réduit
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Marges réduites

        // Boutons avec textes plus courts
        backButton = new JButton("Retour");
        Style.styleCompactButton(backButton); // Nouvelle méthode de style pour boutons compacts
        backButton.addActionListener(e -> mainFrame.showPanel(PersonnelGUI.LIGUE_MANAGEMENT_PANEL));
        buttonPanel.add(backButton);

        renameButton = new JButton("Renommer");
        Style.styleCompactButton(renameButton);
        renameButton.addActionListener(e -> renameLigue());
        buttonPanel.add(renameButton);

        changeAdminButton = new JButton("Admin");
        Style.styleCompactButton(changeAdminButton);
        changeAdminButton.addActionListener(e -> changeLigueAdministrator());
        buttonPanel.add(changeAdminButton);

        manageEmployesButton = new JButton("Employés");
        Style.styleCompactButton(manageEmployesButton);
        manageEmployesButton.addActionListener(e -> manageEmployes());
        buttonPanel.add(manageEmployesButton);

        deleteLigueButton = new JButton("Supprimer");
        Style.styleCompactButton(deleteLigueButton);
        deleteLigueButton.addActionListener(e -> deleteLigue());
        buttonPanel.add(deleteLigueButton);

        add(buttonPanel, BorderLayout.SOUTH);


        // Met à jour les informations lorsque le panneau est affiché
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                updateLigueDetails();
            }
        });
    }

    /**
     * Met à jour les informations affichées de la ligue.
     */
    private void updateLigueDetails() {
        ligueNameLabel.setText("Détails de la Ligue : " + ligue.getNom());
        if (ligue.getAdministrateur() != null) {
            adminLabel.setText(ligue.getAdministrateur().getNom() + " " + ligue.getAdministrateur().getPrenom() + " (ID: " + ligue.getAdministrateur().getId() + ")");
        } else {
            adminLabel.setText("Aucun administrateur défini");
        }
    }

    /**
     * Renomme la ligue.
     */
    private void renameLigue() {
        String newName = JOptionPane.showInputDialog(mainFrame,
                "Entrez le nouveau nom pour la ligue '" + ligue.getNom() + "' :",
                "Renommer la Ligue",
                JOptionPane.PLAIN_MESSAGE);

        if (newName != null && !newName.trim().isEmpty()) {
            try {
                ligue.setNom(newName.trim());
                JOptionPane.showMessageDialog(mainFrame,
                        "Ligue renommée en '" + ligue.getNom() + "' avec succès !",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                updateLigueDetails();
            } catch (SauvegardeImpossible e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Erreur lors du renommage de la ligue : " + e.getMessage(),
                        "Erreur de sauvegarde",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Erreur : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else if (newName != null) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Le nom de la ligue ne peut pas être vide.",
                    "Nom invalide",
                    JOptionPane.WARNING_MESSAGE);
        }
    }



 private void changeLigueAdmin() {
     // Récupérer la liste des employés de la ligue (y compris l'administrateur actuel)
     // et l'employé root (s'il n'est pas déjà l'administrateur et n'appartient pas à cette ligue)
     Set<Employe> employesInLigue = ligue.getEmployes();
     Employe currentAdmin = ligue.getAdministrateur();
     Employe rootEmploye = mainFrame.getGestionPersonnel().getRoot();

     List<Employe> potentialAdmins = new ArrayList<>();
     potentialAdmins.addAll(employesInLigue);

     // Ajouter l'employé root à la liste des potentiels administrateurs s'il n'est pas déjà admin de cette ligue
     // et s'il n'est pas déjà un employé de cette ligue (pour éviter les doublons dans la combobox)
     if (rootEmploye != null && !rootEmploye.equals(currentAdmin) && !potentialAdmins.contains(rootEmploye)) {
          // S'assurer que root n'est pas déjà dans la liste des employés de la ligue
          boolean rootIsInLigue = false;
          for (Employe emp : potentialAdmins) {
              if (emp.getId() != -1 && rootEmploye.getId() != -1 && emp.getId() == rootEmploye.getId()) {
                  rootIsInLigue = true;
                  break;
              }
          }
          if (!rootIsInLigue) {
             potentialAdmins.add(rootEmploye);
          }
     }

     // Trier les employés par nom et prénom
     potentialAdmins.sort(Comparator.comparing(Employe::getNom).thenComparing(Employe::getPrenom));


     // Créer un tableau d'Objets pour le JComboBox, pour pouvoir afficher "Nom Prénom (Ligue)"
     // ou "Root" pour l'employé root
     Object[] choices = potentialAdmins.stream()
         .map(e -> {
             if (e.equals(rootEmploye)) {
                 return "Root";
             } else {
                 return e.getNom() + " " + e.getPrenom() + " (" + e.getMail() + ")";
             }
         })
         .toArray();

     JComboBox<Object> adminComboBox = new JComboBox<>(choices);
     adminComboBox.setSelectedItem(currentAdmin != null ? (currentAdmin.equals(rootEmploye) ? "Root" : currentAdmin.getNom() + " " + currentAdmin.getPrenom() + " (" + currentAdmin.getMail() + ")") : null);
     Style.styleComboBox(adminComboBox); // Assure-toi d'avoir une méthode styleComboBox dans ta classe Style

     JPanel panel = new JPanel(new BorderLayout(10, 10));
     panel.setOpaque(false);
     panel.add(Style.createStyledLabel("Sélectionner le nouvel administrateur :"), BorderLayout.NORTH);
     panel.add(adminComboBox, BorderLayout.CENTER);
     panel.setBorder(new EmptyBorder(10, 10, 10, 10));

     int result = JOptionPane.showConfirmDialog(mainFrame, panel, "Changer l'administrateur de la ligue",
             JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

     if (result == JOptionPane.OK_OPTION) {
         int selectedIndex = adminComboBox.getSelectedIndex();
         if (selectedIndex == -1) {
             JOptionPane.showMessageDialog(mainFrame, "Veuillez sélectionner un administrateur.", "Aucune sélection", JOptionPane.WARNING_MESSAGE);
             return;
         }

         Employe selectedEmploye = potentialAdmins.get(selectedIndex);

         if (selectedEmploye.equals(currentAdmin)) {
             JOptionPane.showMessageDialog(mainFrame, "L'employé sélectionné est déjà l'administrateur de cette ligue.", "Information", JOptionPane.INFORMATION_MESSAGE);
             return;
         }

         try {
             // Définit le nouvel administrateur
             ligue.setAdministrateur(selectedEmploye);
             JOptionPane.showMessageDialog(mainFrame,
                     "L'administrateur de la ligue a été changé en : " + selectedEmploye.getNom() + " " + selectedEmploye.getPrenom(),
                     "Administrateur changé",
                     JOptionPane.INFORMATION_MESSAGE);
             updateLigueDetails(); // Rafraîchir l'affichage
         } catch (SauvegardeImpossible e) {
             JOptionPane.showMessageDialog(mainFrame,
                     "Erreur lors de la sauvegarde du nouvel administrateur : " + e.getMessage(),
                     "Erreur de sauvegarde",
                     JOptionPane.ERROR_MESSAGE);
         } catch (DroitsInsuffisants e) {
             JOptionPane.showMessageDialog(mainFrame,
                     "Droits insuffisants pour changer l'administrateur : " + e.getMessage(),
                     "Accès refusé",
                     JOptionPane.ERROR_MESSAGE);
         } catch (IllegalArgumentException e) {
              JOptionPane.showMessageDialog(mainFrame,
                     "Erreur : " + e.getMessage(),
                     "Erreur",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
 }
    
    /**
     * Permet de changer l'administrateur de la ligue.
     * Propose une liste des employés de la ligue ou de tous les employés (si root).
     */
    private void changeLigueAdministrator() {
        GestionPersonnel gp = mainFrame.getGestionPersonnel();
        Set<Employe> allEmployes = gp.getEmployes();

        java.util.List<Employe> eligibleEmployes = new java.util.ArrayList<>();
        for (Employe emp : allEmployes) {
            if (!emp.estRoot()) {
                 eligibleEmployes.add(emp);
            }
        }
        
        eligibleEmployes.sort(Comparator.comparing(Employe::getNom).thenComparing(Employe::getPrenom));

        if (eligibleEmployes.isEmpty()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Aucun employé éligible pour devenir administrateur de ligue.",
                    "Impossible de changer d'administrateur",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] employeNames = new String[eligibleEmployes.size()];
        for (int i = 0; i < eligibleEmployes.size(); i++) {
            employeNames[i] = eligibleEmployes.get(i).getNom() + " " + eligibleEmployes.get(i).getPrenom() + " (" + eligibleEmployes.get(i).getMail() + ")";
        }

        JComboBox<String> employeComboBox = new JComboBox<>(employeNames);
        employeComboBox.setBackground(Style.SECONDARY_BACKGROUND);
        employeComboBox.setForeground(Style.TEXT_COLOR);
        employeComboBox.setFont(Style.FONT_TEXTFIELD);

        int result = JOptionPane.showConfirmDialog(mainFrame,
                employeComboBox,
                "Sélectionnez le nouvel administrateur pour " + ligue.getNom() + " :",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            int selectedIndex = employeComboBox.getSelectedIndex();
            if (selectedIndex != -1) {
                Employe nouvelAdmin = eligibleEmployes.get(selectedIndex);
                try {
                    ligue.setAdministrateur(nouvelAdmin);
                    JOptionPane.showMessageDialog(mainFrame,
                            nouvelAdmin.getNom() + " " + nouvelAdmin.getPrenom() + " est maintenant l'administrateur de " + ligue.getNom() + ".",
                            "Administrateur changé",
                            JOptionPane.INFORMATION_MESSAGE);
                    updateLigueDetails();
                } catch (SauvegardeImpossible e) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Erreur lors de la sauvegarde du nouvel administrateur : " + e.getMessage(),
                            "Erreur de sauvegarde",
                            JOptionPane.ERROR_MESSAGE);
                } catch (IllegalArgumentException e) {
                    JOptionPane.showMessageDialog(mainFrame,
                            "Erreur : " + e.getMessage(),
                            "Erreur",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }

    /**
     * Navigue vers le panneau de gestion des employés pour cette ligue.
     */
    private void manageEmployes() {
        EmployeManagementPanel employeManagementPanel = new EmployeManagementPanel(mainFrame, ligue);
        mainFrame.mainPanel.add(employeManagementPanel, PersonnelGUI.EMPLOYE_MANAGEMENT_PANEL);
        mainFrame.showPanel(PersonnelGUI.EMPLOYE_MANAGEMENT_PANEL);
    }

    /**
     * Supprime la ligue après confirmation.
     */
    private void deleteLigue() {
        int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Êtes-vous sûr de vouloir supprimer la ligue '" + ligue.getNom() + "' ?\n" +
                "Cette action est irréversible et supprimera tous les employés de cette ligue !",
                "Confirmer la suppression",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                mainFrame.getGestionPersonnel().remove(ligue);
                JOptionPane.showMessageDialog(mainFrame,
                        "Ligue '" + ligue.getNom() + "' supprimée avec succès.",
                        "Ligue supprimée",
                        JOptionPane.INFORMATION_MESSAGE);
                mainFrame.showPanel(PersonnelGUI.LIGUE_MANAGEMENT_PANEL);
            } catch (SauvegardeImpossible e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Erreur lors de la suppression de la ligue : " + e.getMessage(),
                        "Erreur de sauvegarde",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Erreur : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.WARNING_MESSAGE);
            } catch (DroitsInsuffisants e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Droits insuffisants pour supprimer cette ligue : " + e.getMessage(),
                        "Accès refusé",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}