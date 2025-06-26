// Fichier: src/gui/EmployeDirectoryPanel.java
package gui;

import personnel.GestionPersonnel;
import personnel.Employe;
import personnel.SauvegardeImpossible;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Comparator;
import java.util.Set;
import java.awt.event.HierarchyEvent; 

/**
 * Panneau de l'annuaire des employés.
 * Accessible à tous les utilisateurs pour consulter la liste de tous les employés.
 */
public class EmployeDirectoryPanel extends JPanel {
    private PersonnelGUI mainFrame;
    private JTable employesTable;
    private DefaultTableModel tableModel;
    private JButton backButton;

private static final long serialVersionUID = 1L;

    /**
     * Constructeur du panneau de l'annuaire des employés.
     * @param mainFrame La fenêtre principale de l'application.
     */
    public EmployeDirectoryPanel(PersonnelGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(Style.PRIMARY_BACKGROUND);
        setBorder(Style.PADDING_BORDER);

        // Titre du panneau
        JLabel titleLabel = new JLabel("Annuaire des Employés");
        titleLabel.setFont(Style.FONT_TITLE);
        titleLabel.setForeground(Style.ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        // Tableau des employés
        String[] columnNames = {"ID", "Nom", "Prénom", "Mail", "Date Arrivée", "Date Départ"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Les cellules ne sont pas éditables
            }
        };
        employesTable = new JTable(tableModel);
        // Stylisation du tableau
        employesTable.setBackground(Style.SECONDARY_BACKGROUND);
        employesTable.setForeground(Style.TEXT_COLOR);
        employesTable.setFont(Style.FONT_LABEL);
        employesTable.getTableHeader().setBackground(Style.ACCENT_COLOR);
        employesTable.getTableHeader().setForeground(Style.PRIMARY_BACKGROUND);
        employesTable.getTableHeader().setFont(Style.FONT_BUTTON);
        employesTable.setSelectionBackground(Style.ACCENT_COLOR.darker());
        employesTable.setSelectionForeground(Style.TEXT_COLOR);
        employesTable.setRowHeight(25);
        employesTable.setShowVerticalLines(false);
        employesTable.setGridColor(Style.PRIMARY_BACKGROUND);

        JScrollPane scrollPane = new JScrollPane(employesTable);
        scrollPane.setBackground(Style.PRIMARY_BACKGROUND);
        scrollPane.getViewport().setBackground(Style.SECONDARY_BACKGROUND);
        add(scrollPane, BorderLayout.CENTER);

        // Panneau des boutons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        backButton = new JButton("Retour au Menu Principal");
        Style.styleButton(backButton);
        backButton.addActionListener(e -> mainFrame.showPanel(PersonnelGUI.MAIN_MENU_PANEL));
        buttonPanel.add(backButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Charge les informations des employés lorsque le panneau est affiché
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                loadEmployeDirectory();
            }
        });
    }

    /**
     * Charge et affiche la liste de tous les employés dans le tableau.
     */
    private void loadEmployeDirectory() {
        tableModel.setRowCount(0); // Efface les lignes existantes

        GestionPersonnel gp = mainFrame.getGestionPersonnel();
        Set<Employe> allEmployes = gp.getEmployes(); // Récupère tous les employés

        // Trie les employés par nom et prénom
        allEmployes.stream()
                .sorted(Comparator.comparing(Employe::getNom).thenComparing(Employe::getPrenom))
                .forEach(employe -> {
                    tableModel.addRow(new Object[]{
                        employe.getId(),
                        employe.getNom(),
                        employe.getPrenom(),
                        employe.getMail(),
                        employe.getDateArrivee() != null ? employe.getDateArrivee().toString() : "N/A",
                        employe.getDateDepart() != null ? employe.getDateDepart().toString() : "N/A"
                    });
                });
    }
}
