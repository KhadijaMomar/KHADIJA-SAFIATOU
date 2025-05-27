package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import personnel.Employe; // Assurez-vous que cette importation est correcte

public class LoginDialog extends JDialog {

    private JPasswordField passwordField;
    private JButton loginButton;
    private boolean authenticated = false;
    private Employe rootUser; // Pour stocker l'utilisateur root authentifié

    public LoginDialog(JFrame parent, Employe rootUser) {
        super(parent, "Connexion Root", true); // true pour modal
        this.rootUser = rootUser;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Marge interne

        // Label pour le mot de passe
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Mot de passe Root :"), gbc);

        // Champ de mot de passe
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.gridy = 0;
        add(passwordField, gbc);

        // Bouton de connexion
        loginButton = new JButton("Se connecter");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2; // Span across two columns
        gbc.anchor = GridBagConstraints.CENTER;
        add(loginButton, gbc);

        // Action du bouton de connexion
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String password = new String(passwordField.getPassword());
                if (rootUser.checkPassword(password)) {
                    authenticated = true;
                    dispose(); // Ferme le dialogue
                } else {
                    JOptionPane.showMessageDialog(LoginDialog.this,
                            "Mot de passe incorrect.",
                            "Erreur de Connexion",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        pack(); // Ajuste la taille de la fenêtre en fonction des composants
        setLocationRelativeTo(parent); // Centre le dialogue par rapport à la fenêtre parente
    }

    public boolean isAuthenticated() {
        return authenticated;
    }
}