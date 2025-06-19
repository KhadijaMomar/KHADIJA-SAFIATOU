package gui;

import personnel.Employe;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent; // Pour la mise à jour quand le panneau est visible

public class EmployeDetailsPanel extends JPanel {
    private PersonnelGUI mainFrame;
    private JLabel nomLabel;
    private JLabel prenomLabel;
    private JLabel mailLabel;
    private JLabel ligueLabel;
    private JLabel dateArriveeLabel;
    private JLabel dateDepartLabel;
    private JButton backButton;

    public EmployeDetailsPanel(PersonnelGUI mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(20, 20));
        setBackground(Style.PRIMARY_BACKGROUND);
        setBorder(Style.PADDING_BORDER);

        JLabel titleLabel = new JLabel("Mon Profil");
        titleLabel.setFont(Style.FONT_TITLE);
        titleLabel.setForeground(Style.ACCENT_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(titleLabel, BorderLayout.NORTH);

        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        infoPanel.setOpaque(false);
        infoPanel.setBorder(new EmptyBorder(20, 50, 20, 50));

        infoPanel.add(Style.createStyledLabel("Nom :"));
        nomLabel = Style.createStyledLabel("");
        infoPanel.add(nomLabel);

        infoPanel.add(Style.createStyledLabel("Prénom :"));
        prenomLabel = Style.createStyledLabel("");
        infoPanel.add(prenomLabel);

        infoPanel.add(Style.createStyledLabel("Mail :"));
        mailLabel = Style.createStyledLabel("");
        infoPanel.add(mailLabel);

        infoPanel.add(Style.createStyledLabel("Ligue :"));
        ligueLabel = Style.createStyledLabel("");
        infoPanel.add(ligueLabel);

        infoPanel.add(Style.createStyledLabel("Date d'arrivée :"));
        dateArriveeLabel = Style.createStyledLabel("");
        infoPanel.add(dateArriveeLabel);

        infoPanel.add(Style.createStyledLabel("Date de départ :"));
        dateDepartLabel = Style.createStyledLabel("");
        infoPanel.add(dateDepartLabel);

        add(infoPanel, BorderLayout.CENTER);

        backButton = Style.createStyledButton("Retour");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showPanel(PersonnelGUI.MAIN_MENU_PANEL);
            }
        });
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);

        // Le HierarchyListener est crucial pour charger les données quand le panneau est visible
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                loadEmployeDetails();
            }
        });
    }

    private void loadEmployeDetails() {
        Employe employe = mainFrame.getUtilisateurConnecte();
        if (employe != null) {
            nomLabel.setText(employe.getNom());
            prenomLabel.setText(employe.getPrenom());
            mailLabel.setText(employe.getMail());
            ligueLabel.setText(employe.getLigue() != null ? employe.getLigue().getNom() : "N/A");
            dateArriveeLabel.setText(employe.getDateArrivee() != null ? employe.getDateArrivee().toString() : "N/A");
            dateDepartLabel.setText(employe.getDateDepart() != null ? employe.getDateDepart().toString() : "N/A");
        } else {
            // Afficher des valeurs par défaut ou un message d'erreur si aucun employé n'est connecté
            nomLabel.setText("N/A");
            prenomLabel.setText("N/A");
            mailLabel.setText("N/A");
            ligueLabel.setText("N/A");
            dateArriveeLabel.setText("N/A");
            dateDepartLabel.setText("N/A");
        }
    }
}