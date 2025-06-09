package commandLine;

import commandLineMenus.Menu;
import static commandLineMenus.rendering.examples.util.InOut.getString;
import personnel.Employe;
import personnel.GestionPersonnel;
import personnel.SauvegardeImpossible;

public class PersonnelConsole {
    private GestionPersonnel gestionPersonnel;
    LigueConsole ligueConsole;
    EmployeConsole employeConsole;
    private Employe utilisateurConnecte; // Utilisateur actuellement connecté

    /**
     * Constructeur de PersonnelConsole.
     * @param gestionPersonnel L'instance de GestionPersonnel.
     */
    public PersonnelConsole(GestionPersonnel gestionPersonnel) {
        this.gestionPersonnel = gestionPersonnel;
        this.employeConsole = new EmployeConsole();
        // Correction du constructeur de LigueConsole pour passer l'instance de EmployeConsole
        this.ligueConsole = new LigueConsole(gestionPersonnel, employeConsole); 
    }

    /**
     * Démarre la console principale.
     */
    public void start() {
        menuPrincipal().start();
    }

    /**
     * Crée le menu principal de l'application.
     * @return Le menu principal.
     */
    private Menu menuPrincipal() {
        Menu menu = new Menu("Gestion du personnel des ligues");
        menu.add(employeConsole.getOptionForEmploye(gestionPersonnel.getRoot()));
        //menu.add(employeConsole.getEditerEmployeOption().getOption(gestionPersonnel.getRoot()));        
        menu.add(ligueConsole.menuLigues()); // le menu de gestion des ligues
        menu.addBack("q", "Quitter l'application");
        return menu;
    }

    /**
     * Retourne l'employé actuellement connecté.
     * @return L'employé connecté.
     */
    public Employe getUtilisateurConnecte() {
        return utilisateurConnecte;
    }

    /**
     * Définit l'employé actuellement connecté.
     * @param utilisateurConnecte L'employé à définir comme connecté.
     */
    public void setUtilisateurConnecte(Employe utilisateurConnecte) {
        this.utilisateurConnecte = utilisateurConnecte;
    }
    
    /**
     * Retourne l'instance de GestionPersonnel.
     * @return L'instance de GestionPersonnel.
     */
    public GestionPersonnel getGestionPersonnel() {
        return gestionPersonnel;
    }

    /**
     * Méthode principale de l'application.
     * Gère l'initialisation de GestionPersonnel et l'authentification de l'utilisateur.
     * @param args Arguments de la ligne de commande (non utilisés).
     */
    public static void main(String[] args) {
        GestionPersonnel gestionPersonnel = null;
        try {
            // Tente d'obtenir l'instance de GestionPersonnel (initialisation et chargement des données)
            gestionPersonnel = GestionPersonnel.getGestionPersonnel();
            if (gestionPersonnel == null) {
                System.err.println("Erreur: Impossible d'initialiser GestionPersonnel.");
                System.exit(1); // Quitte l'application en cas d'échec critique
            }
        } catch (RuntimeException e) {
            System.err.println("Erreur fatale lors du chargement de l'application: " + e.getMessage());
            e.printStackTrace();
            System.exit(1); // Quitte l'application en cas d'erreur fatale
        }

        PersonnelConsole console = new PersonnelConsole(gestionPersonnel);

        // Boucle d'authentification de l'utilisateur
        Employe utilisateurCourant = null;
        boolean authentifie = false;
        while (!authentifie) {
            System.out.print("Mail ou Nom : ");
            String nomUtilisateur = getString("");
            System.out.print("Password : ");
            String password = getString("");

            try {
                // Tente d'authentifier l'utilisateur
                utilisateurCourant = gestionPersonnel.authentifier(nomUtilisateur, password);
                if (utilisateurCourant != null) {
                    System.out.println("Authentification réussie pour " + utilisateurCourant.getNom() + " " + utilisateurCourant.getPrenom());
                    console.setUtilisateurConnecte(utilisateurCourant); // Définit l'utilisateur connecté
                    // Passe la référence de la console principale aux sous-consoles
                    console.ligueConsole.setPersonnelConsole(console); 
                    console.employeConsole.setPersonnelConsole(console);
                    authentifie = true; // L'authentification a réussi
                } else {
                    System.out.println("Nom d'utilisateur ou mot de passe incorrect.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur d'authentification (argument invalide) : " + e.getMessage());
            } catch (SauvegardeImpossible e) {
                System.err.println("Erreur grave lors de l'authentification (problème de base de données) : " + e.getMessage());
            }
        }

        console.start(); // Démarre la console principale après authentification
    }
}
