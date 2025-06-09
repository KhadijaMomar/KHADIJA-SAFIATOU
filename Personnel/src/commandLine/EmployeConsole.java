package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import commandLineMenus.ListOption;
import commandLineMenus.Menu;
import commandLineMenus.Option;
import personnel.DateIncoherenteException;
import personnel.DateInvalideException;
import personnel.Employe;
import personnel.SauvegardeImpossible;
import personnel.GestionPersonnel;

public class EmployeConsole {

    private PersonnelConsole personnelConsole;

    public EmployeConsole() {
    }

    public void setPersonnelConsole(PersonnelConsole personnelConsole) {
        this.personnelConsole = personnelConsole;
    }

    
    public Option getOptionForEmploye(Employe employe) {
        Menu menu = new Menu("Gérer le compte " + employe.getNom(), "c");
        menu.add(afficher(employe));
        addEditionOptions(menu, employe, personnelConsole.getUtilisateurConnecte()); // Utilise l'utilisateur connecté pour les permissions
        menu.addBack("q");
        return menu;
    }
    

    // Supprimez ou commentez cette méthode privée
  
    private void addEditionOptions(Menu menu, Employe employe, Employe utilisateurConnecte) {
        System.out.println("Ajout des options d'édition pour l'employé : " + employe.getNom());

        // Si l'utilisateur connecté est le root
        if (utilisateurConnecte.equals(personnelConsole.getGestionPersonnel().getRoot())) {
            menu.add(changerNom(employe));
            menu.add(changerPrenom(employe));
            menu.add(changerMail(employe));
            menu.add(changerPassword(employe));
            menu.add(changerDataArrivee(employe));
            menu.add(changerDateDepart(employe));
        } else if (utilisateurConnecte.equals(employe)) { // Si l'utilisateur connecté édite son propre compte
            menu.add(changerMail(employe));
            menu.add(changerPassword(employe));
        } else {
            System.out.println("Vous n'avez pas la permission d'éditer ce compte.");
        }
    }
  
    /*
    
    public ListOption<Employe> getEditerEmployeOption() {
        return this::editerEmploye;
    }

    private Option editerEmploye(Employe employe) {
        Menu menu = new Menu("Gérer le compte " + employe.getNom(), "c");
        menu.add(afficher(employe));
        menu.add(changerNom(employe));
        menu.add(changerPrenom(employe));
        menu.add(changerMail(employe));
        menu.add(changerPassword(employe));
        menu.add(changerDataArrivee(employe));
        menu.add(changerDateDepart(employe));
        menu.addBack("q");
        return menu;
    }
     */
    
    public ListOption<Employe> getMenuEmployeOption() {
        return employe -> {
            // Log pour vérifier l'initialisation
            System.out.println("Initialisation du menu d'édition pour l'employé : " + employe.getNom());

            // Création du menu
            Menu menu = new Menu("Options pour " + employe.getNom() + " " + employe.getPrenom());

            // Ajout de toutes les options nécessaires
            menu.add(afficher(employe)); // <-- Ajout d'option

            // Récupération de l'utilisateur connecté
            Employe utilisateurConnecte = personnelConsole.getUtilisateurConnecte();

            // Ajout des options d'édition
            addEditionOptions(menu, employe, utilisateurConnecte); // <-- Potentiellement d'autres ajouts

            // Ajout de l'option de retour
            menu.addBack("q"); // <-- Ajout d'option

            // Log pour vérifier que toutes les options sont ajoutées
            System.out.println("Toutes les options ont été ajoutées pour l'employé : " + employe.getNom());

            // NE PAS DÉMARRER LE MENU ICI. La ListOption doit retourner le Menu,
            // et le List appelant le démarrera.
            return menu;
        };
    }
    
    private Option afficher(Employe employe) {
        return new Option("Afficher l'employé", "l", () -> {
            System.out.println("Affichage des détails de l'employé : " + employe.getNom());
            System.out.println(employe);
        });
    }

    private Option changerNom(Employe employe) {
        return new Option("Changer Nom", "n", () -> {
            String nouveauNom = getString("Nouveau Nom : ");
            try {
                employe.setNom(nouveauNom);
                System.out.println("Nom modifié : " + nouveauNom);
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de sauvegarder le nom : " + e.getMessage());
            }
        });
    }

    private Option changerPrenom(Employe employe) {
        return new Option("Changer Prénom", "p", () -> {
            String nouveauPrenom = getString("Nouveau Prénom : ");
            try {
                employe.setPrenom(nouveauPrenom);
                System.out.println("Prénom modifié : " + nouveauPrenom);
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de sauvegarder le prénom : " + e.getMessage());
            }
        });
    }

    private Option changerMail(Employe employe) {
        return new Option("Changer Mail", "m", () -> {
            String nouveauMail = getString("Nouveau Mail : ");
            try {
                employe.setMail(nouveauMail);
                System.out.println("Mail modifié : " + nouveauMail);
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de sauvegarder le mail : " + e.getMessage());
            }
        });
    }

    private Option changerPassword(Employe employe) {
        return new Option("Changer Password", "P", () -> {
            String nouveauPassword = getString("Nouveau Password : ");
            try {
                employe.setPassword(nouveauPassword);
                System.out.println("Password modifié.");
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de sauvegarder le mot de passe : " + e.getMessage());
            }
        });
    }

    private Option changerLigue(Employe employe) {
        return new Option("Changer Ligue", "f", () -> {
            System.out.println("Ligues disponibles :");
            List<personnel.Ligue> liguesList = new ArrayList<>(personnelConsole.getGestionPersonnel().getLigues());
            if (liguesList.isEmpty()) {
                System.out.println("Aucune ligue disponible.");
                return;
            }
            for (int i = 0; i < liguesList.size(); i++) {
                System.out.println((i + 1) + " : " + liguesList.get(i).getNom());
            }
            System.out.println((liguesList.size() + 1) + " : Pas de ligue");

            int choix = -1;
            boolean validInput = false;
            while (!validInput) {
                try {
                    choix = Integer.parseInt(getString("Sélectionner la ligue par numéro : "));
                    if (choix >= 1 && choix <= liguesList.size() + 1) {
                        validInput = true;
                    } else {
                        System.out.println("Numéro de ligue invalide.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Veuillez entrer un numéro valide.");
                }
            }

            personnel.Ligue nouvelleLigue = null;
            if (choix <= liguesList.size()) {
                nouvelleLigue = liguesList.get(choix - 1);
            }

            try {
                employe.setLigue(nouvelleLigue);
                if (nouvelleLigue != null) {
                    System.out.println("Ligue de l'employé modifiée : " + nouvelleLigue.getNom());
                } else {
                    System.out.println("L'employé a été retiré de sa ligue.");
                }
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de sauvegarder le changement de ligue : " + e.getMessage());
            }
        });
    }

    private Option changerRoot(Employe employe) {
        return new Option("Changer statut Root", "r", () -> {
            boolean estActuellementRoot = employe.equals(personnelConsole.getGestionPersonnel().getRoot());
            String confirmation = getString("L'employé " + employe.getNom() + " " + employe.getPrenom() +
                (estActuellementRoot ? " est actuellement ROOT. Voulez-vous le déclasser ? (oui/non)" :
                " n'est pas ROOT. Voulez-vous le promouvoir ROOT ? (oui/non)"));
            if (confirmation.equalsIgnoreCase("oui")) {
                try {
                    if (estActuellementRoot) {
                        if (personnelConsole.getGestionPersonnel().getEmployes().stream().filter(Employe::estRoot).count() == 1) {
                            System.out.println("Opération impossible : il doit toujours y avoir au moins un utilisateur ROOT.");
                            return;
                        }
                        employe.setEstRoot(false);
                        System.out.println(employe.getNom() + " " + employe.getPrenom() + " n'est plus ROOT.");
                    } else {
                        Employe ancienRoot = personnelConsole.getGestionPersonnel().getRoot();
                        if (ancienRoot != null) {
                            ancienRoot.setEstRoot(false);
                        }
                        employe.setEstRoot(true);
                        System.out.println(employe.getNom() + " " + employe.getPrenom() + " est maintenant ROOT.");
                    }
                } catch (SauvegardeImpossible e) {
                    System.err.println("Impossible de sauvegarder le statut ROOT : " + e.getMessage());
                }
            }
        });
    }

    private Option supprimer(Employe employe) {
        return new Option("Supprimer l'employé", "d", () -> {
            String confirmation = getString("Êtes-vous sûr de vouloir supprimer " + employe.getNom() + " " + employe.getPrenom() + " ? (oui/non)");
            if (confirmation.equalsIgnoreCase("oui")) {
                try {
                    employe.remove();
                    System.out.println(employe.getNom() + " " + employe.getPrenom() + " supprimé.");
                } catch (personnel.ImpossibleDeSupprimerRoot e) {
                    System.out.println("Erreur : " + e.getMessage());
                } catch (SauvegardeImpossible e) {
                    System.err.println("Impossible de supprimer l'employé : " + e.getMessage());
                }
            }
        });
    }

    private Option changerDataArrivee(Employe employe) {
        return new Option("Changer Date Arrivée", "a", () -> {
            LocalDate dateArrivee = null;
            boolean validInput = false;
            while (!validInput) {
                String dateStr = getString("Nouvelle Date Arrivée (format AAAA-MM-JJ) : ");
                try {
                    dateArrivee = LocalDate.parse(dateStr);
                    employe.setDateArrivee(dateArrivee);
                    System.out.println("Date d'arrivée modifiée : " + dateArrivee);
                    validInput = true;
                } catch (DateTimeParseException e) {
                    System.out.println("Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                } catch (DateIncoherenteException | DateInvalideException e) {
                    System.out.println("Erreur de date : " + e.getMessage());
                } catch (SauvegardeImpossible e) {
                    System.err.println("Impossible de sauvegarder la date d'arrivée : " + e.getMessage());
                    validInput = true;
                }
            }
        });
    }

    private Option changerDateDepart(Employe employe) {
        return new Option("Changer Date Départ", "D", () -> {
            LocalDate dateDepart = null;
            boolean validInput = false;
            while (!validInput) {
                String dateStr = getString("Nouvelle Date Départ (format AAAA-MM-JJ) : ");
                try {
                    dateDepart = LocalDate.parse(dateStr);
                    employe.setDateDepart(dateDepart);
                    System.out.println("Date de départ modifiée : " + dateDepart);
                    validInput = true;
                } catch (DateTimeParseException e) {
                    System.out.println("Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                } catch (DateIncoherenteException | DateInvalideException e) {
                    System.out.println("Erreur de date : " + e.getMessage());
                } catch (SauvegardeImpossible e) {
                    System.err.println("Impossible de sauvegarder la date de départ : " + e.getMessage());
                    validInput = true;
                }
            }
        });
    }
}