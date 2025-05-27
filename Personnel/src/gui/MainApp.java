package gui;

import javax.swing.*;
import personnel.Employe;
import personnel.GestionPersonnel;
import personnel.SauvegardeImpossible; // Importez SauvegardeImpossible

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GestionPersonnel gestionPersonnel = null;
            try {
                // Initialiser GestionPersonnel (qui inclut l'initialisation du root)
                gestionPersonnel = GestionPersonnel.getGestionPersonnel();
            } catch (RuntimeException e) {
                // Gérer l'erreur si l'initialisation du personnel échoue
                JOptionPane.showMessageDialog(null,
                        "Erreur critique lors du démarrage de l'application: " + e.getMessage(),
                        "Erreur de Démarrage",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1); // Quitter l'application
            }

            Employe root = gestionPersonnel.getRoot();

            // Créer et afficher le dialogue de connexion
            LoginDialog loginDialog = new LoginDialog(null, root); // null car pas de fenêtre parente encore
            loginDialog.setVisible(true);

            // Vérifier si l'authentification a réussi
            if (loginDialog.isAuthenticated()) {
                // Si authentifié, afficher la fenêtre principale
                PersonnelGUI mainFrame = new PersonnelGUI(gestionPersonnel);
                mainFrame.display();
            } else {
                // Si l'authentification échoue ou est annulée, quitter l'application
                JOptionPane.showMessageDialog(null,
                        "Authentification échouée ou annulée. L'application va se fermer.",
                        "Connexion Annulée",
                        JOptionPane.INFORMATION_MESSAGE);
                System.exit(0);
            }
        });
    }
}