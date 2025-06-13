// Fichier: src/gui/LigueManagementPanel.java
package gui;

import personnel.GestionPersonnel;
import personnel.Ligue;
import personnel.SauvegardeImpossible;
import personnel.DroitsInsuffisants;
import personnel.Employe; // Importation ajoutée pour Employe
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Comparator;
import java.awt.event.HierarchyEvent; // Importation ajoutée pour HierarchyEvent

/**
 * Panneau de gestion des ligues.
 * Accessible par le Super-Administrateur pour voir, ajouter, sélectionner et gérer les ligues.
 */
public class LigueManagementPanel extends JPanel {
    private PersonnelGUI mainFrame;
    private JTable liguesTable;
    private DefaultTableModel tableModel;
    private JButton addLigueButton;
    private JButton selectLigueButton;
    private JButton backButton;

    /**
     * Constructeur du panneau de gestion des ligues.
     * @param mainFrame La fenêtre principale de l'application.
     */
    public LigueManagementPanel(PersonnelGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(Style.PRIMARY_BACKGROUND);
        setBorder(Style.PADDING_BORDER);

        // Titre du panneau
        JLabel titleLabel = new JLabel("Gestion des Ligues");
        titleLabel.setFont(Style.FONT_TITLE);
        titleLabel.setForeground(Style.ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Tableau des ligues
        String[] columnNames = {"ID", "Nom de la Ligue", "Administrateur"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Les cellules ne sont pas éditables directement dans le tableau
            }
        };
        liguesTable = new JTable(tableModel);
        // Stylisation du tableau
        liguesTable.setBackground(Style.SECONDARY_BACKGROUND);
        liguesTable.setForeground(Style.TEXT_COLOR);
        liguesTable.setFont(Style.FONT_LABEL);
        liguesTable.getTableHeader().setBackground(Style.ACCENT_COLOR);
        liguesTable.getTableHeader().setForeground(Style.PRIMARY_BACKGROUND);
        liguesTable.getTableHeader().setFont(Style.FONT_BUTTON);
        liguesTable.setSelectionBackground(Style.ACCENT_COLOR.darker());
        liguesTable.setSelectionForeground(Style.TEXT_COLOR);
        liguesTable.setRowHeight(25);
        liguesTable.setShowVerticalLines(false); // Pas de lignes verticales
        liguesTable.setGridColor(Style.PRIMARY_BACKGROUND); // Couleur des lignes horizontales

        JScrollPane scrollPane = new JScrollPane(liguesTable);
        scrollPane.setBackground(Style.PRIMARY_BACKGROUND);
        scrollPane.getViewport().setBackground(Style.SECONDARY_BACKGROUND); // Fond du viewport du scroll
        add(scrollPane, BorderLayout.CENTER);

        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Espacement entre les boutons
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        addLigueButton = new JButton("Ajouter une Ligue");
        Style.styleButton(addLigueButton);
        addLigueButton.addActionListener(e -> addLigue());
        buttonPanel.add(addLigueButton);

        selectLigueButton = new JButton("Sélectionner une Ligue");
        Style.styleButton(selectLigueButton);
        selectLigueButton.addActionListener(e -> selectLigue());
        buttonPanel.add(selectLigueButton);
        
        backButton = new JButton("Retour au Menu Principal");
        Style.styleButton(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(PersonnelGUI.MAIN_MENU_PANEL));
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Met à jour la liste des ligues lorsque le panneau est affiché
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                loadLigues();
            }
        });
    }

    /**
     * Charge et affiche la liste des ligues dans le tableau.
     * Accessible uniquement si l'utilisateur est le Super-Administrateur.
     */
    private void loadLigues() {
        // Vérifier les droits de l'utilisateur avant d'afficher
        Employe utilisateurConnecte = mainFrame.getUtilisateurConnecte();
        if (utilisateurConnecte == null || !utilisateurConnecte.estRoot()) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Accès refusé. Seul le Super-Administrateur peut gérer les ligues.",
                    "Droits insuffisants",
                    JOptionPane.WARNING_MESSAGE);
            mainFrame.showPanel(PersonnelGUI.MAIN_MENU_PANEL); // Retour au menu principal
            return;
        }

        tableModel.setRowCount(0); // Efface les lignes existantes

        // Récupère les ligues et les trie par nom
        mainFrame.getGestionPersonnel().getLigues().stream()
                .sorted(Comparator.comparing(Ligue::getNom))
                .forEach(ligue -> {
                    String adminName = "Non défini";
                    if (ligue.getAdministrateur() != null) {
                        adminName = ligue.getAdministrateur().getNom() + " " + ligue.getAdministrateur().getPrenom();
                    }
                    tableModel.addRow(new Object[]{ligue.getId(), ligue.getNom(), adminName});
                });
    }

    /**
     * Demande à l'utilisateur le nom d'une nouvelle ligue et l'ajoute.
     */
    private void addLigue() {
        String nomLigue = JOptionPane.showInputDialog(mainFrame,
                "Entrez le nom de la nouvelle ligue :",
                "Ajouter une Ligue",
                JOptionPane.PLAIN_MESSAGE);

        if (nomLigue != null && !nomLigue.trim().isEmpty()) {
            try {
                // Créer la ligue en utilisant l'instance de GestionPersonnel
                mainFrame.getGestionPersonnel().addLigue(new Ligue(mainFrame.getGestionPersonnel(), nomLigue.trim()));
                JOptionPane.showMessageDialog(mainFrame,
                        "Ligue '" + nomLigue.trim() + "' ajoutée avec succès !",
                        "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                loadLigues(); // Recharger le tableau
            } catch (SauvegardeImpossible e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Erreur lors de l'ajout de la ligue : " + e.getMessage(),
                        "Erreur de sauvegarde",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(mainFrame,
                        "Erreur : " + e.getMessage(),
                        "Erreur",
                        JOptionPane.WARNING_MESSAGE);
            }
        } else if (nomLigue != null) { // Si l'utilisateur a cliqué sur OK mais a laissé le champ vide
            JOptionPane.showMessageDialog(mainFrame,
                    "Le nom de la ligue ne peut pas être vide.",
                    "Nom invalide",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Permet de sélectionner une ligue dans le tableau et de naviguer vers son panneau de détails.
     */
    private void selectLigue() {
        int selectedRow = liguesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Veuillez sélectionner une ligue dans le tableau.",
                    "Aucune sélection",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int ligueId = (int) tableModel.getValueAt(selectedRow, 0); // Récupère l'ID de la ligue
            Ligue selectedLigue = mainFrame.getGestionPersonnel().getLigue(ligueId);

            if (selectedLigue != null) {
                // Créer ou réutiliser le LigueDetailsPanel
                LigueDetailsPanel ligueDetailsPanel = new LigueDetailsPanel(mainFrame, selectedLigue);
                mainFrame.mainPanel.add(ligueDetailsPanel, PersonnelGUI.LIGUE_DETAILS_PANEL);
                mainFrame.showPanel(PersonnelGUI.LIGUE_DETAILS_PANEL);
            } else {
                JOptionPane.showMessageDialog(mainFrame,
                        "Ligue introuvable (ID: " + ligueId + ").",
                        "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame,
                    "Erreur lors de la sélection de la ligue : " + e.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
