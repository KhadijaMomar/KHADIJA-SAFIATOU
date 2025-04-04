package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import commandLineMenus.List;
import commandLineMenus.Menu;
import commandLineMenus.Option;

import personnel.*;

public class LigueConsole {
    private GestionPersonnel gestionPersonnel;
    private EmployeConsole employeConsole;

    public LigueConsole(GestionPersonnel gestionPersonnel, EmployeConsole employeConsole) {
        this.gestionPersonnel = gestionPersonnel;
        this.employeConsole = employeConsole;
    } 	

    // Menu principal pour gérer les ligues
    Menu menuLigues() {
        Menu menu = new Menu("Gérer les ligues", "l");
        menu.add(afficherLigues());
        menu.add(ajouterLigue());
        menu.add(selectionnerLigue());
        menu.addBack("q");
        return menu;
    }

    // Option pour afficher toutes les ligues
    private Option afficherLigues() {
        return new Option("Afficher les ligues", "l", () -> {
            System.out.println(gestionPersonnel.getLigues());
        });
    }

    // Option pour afficher les détails d'une ligue spécifique
    private Option afficher(final Ligue ligue) {
        return new Option("Afficher la ligue", "l", () -> {
            System.out.println(ligue);
            System.out.println("administrée par " + ligue.getAdministrateur());
        });
    }

    // Option pour afficher les employés d'une ligue
    private Option afficherEmployes(final Ligue ligue) {
        return new Option("Afficher les employes", "l", () -> {
            System.out.println(ligue.getEmployes());
        });
    }

    // Option pour ajouter une nouvelle ligue
    private Option ajouterLigue() {
        return new Option("Ajouter une ligue", "a", () -> {
            try {
                gestionPersonnel.addLigue(getString("nom : "));
            } catch (SauvegardeImpossible exception) {
                System.err.println("Impossible de sauvegarder cette ligue");
            }
        });
    }

    // Menu pour éditer une ligue spécifique
    private Menu editerLigue(Ligue ligue) {
        Menu menu = new Menu("Editer " + ligue.getNom());
        menu.add(afficher(ligue));
        menu.add(gererEmployes(ligue));
        menu.add(changerNom(ligue));
        menu.add(supprimer(ligue));
        menu.addBack("q");
        return menu;
    }

    // Option pour changer le nom d'une ligue
    
    private Option changerNom(final Ligue ligue) {
        return new Option("Renommer", "r", () -> {
            try {
                ligue.setNom(getString("Nouveau nom : "));
            } catch (SauvegardeImpossible e) {
                System.err.println("Erreur lors de la mise à jour du nom de la ligue : " + e.getMessage());
            }
        });
    }

    // Option pour sélectionner une ligue à partir de la liste
    private List<Ligue> selectionnerLigue() {
        return new List<Ligue>("Sélectionner une ligue", "e",
                () -> new ArrayList<>(gestionPersonnel.getLigues()),
                (element) -> editerLigue(element));
    }
  
    // Option pour ajouter un nouvel employé à une ligue
    private Option ajouterEmploye(final Ligue ligue) {
        return new Option("Ajouter un employé", "a", () -> {
            String nom = getString("Nom de l'employé : ");
            String prenom = getString("Prénom de l'employé : ");
            String mail = getString("Mail de l'employé : ");
            String password = getString("Mot de passe de l'employé : ");

            LocalDate dateArrivee = null;
            while (dateArrivee == null) {
                String dateArriveeStr = getString("Date d'arrivée (format: YYYY-MM-DD) : ");
                try {
                    dateArrivee = LocalDate.parse(dateArriveeStr);
                } catch (DateTimeParseException e) {
                    System.out.println("Format de date incorrect. Veuillez utiliser le format YYYY-MM-DD.");
                }
            }

            LocalDate dateDepart = null;
            boolean validDepartDate = false;
            while (!validDepartDate) {
                String dateDepartStr = getString("Date de départ (format: YYYY-MM-DD) (laissez vide si pas de départ) : ");
                if (dateDepartStr.isEmpty()) {
                    validDepartDate = true; 
                } else {
                    try {
                        dateDepart = LocalDate.parse(dateDepartStr);
                        if (dateDepart.isBefore(dateArrivee)) {
                            System.out.println("La date de départ ne peut pas être antérieure à la date d'arrivée.");
                        } else {
                            validDepartDate = true;
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Format de date incorrect. Veuillez utiliser le format YYYY-MM-DD.");
                    }
                }
            }

            try {
				ligue.addEmploye(nom, prenom, mail, password, dateArrivee, dateDepart);
			} catch (SauvegardeImpossible e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        });
    }

    // Menu pour gérer les employés d'une ligue
    private Menu gererEmployes(Ligue ligue) {
        Menu menu = new Menu("Gérer les employés de " + ligue.getNom(), "e");
        menu.add(afficherEmployes(ligue));
        menu.add(ajouterEmploye(ligue));
        menu.add(selectionnerEmploye(ligue)); 
        menu.add(changerAdministrateur(ligue)); // Ajout de l'option pour changer l'administrateur
        menu.addBack("q");
        return menu;
    }

    // Option pour sélectionner un employé à partir de la liste des employés d'une ligue
    private List<Employe> selectionnerEmploye(final Ligue ligue) {
        return new List<>("Sélectionner un employé", "s",
                () -> new ArrayList<>(ligue.getEmployes()),
                (element) -> menuActionsEmploye(ligue, element));
    }

    // Menu pour les actions spécifiques à un employé (modifier ou supprimer)
    private Menu menuActionsEmploye(Ligue ligue, Employe employe) {
        Menu menu = new Menu("Actions pour " + employe.getNom() + " " + employe.getPrenom(), "a");
        menu.add(employeConsole.getEditerEmployeOption().getOption(employe));
        menu.add(supprimerEmploye(employe));
        menu.addBack("q");
        return menu;
    }

    // Option pour modifier un employe
    private Option modifierEmploye(final Employe employe) {
        return new Option("Modifier l'employé", "m", () -> {
        	 employeConsole.getEditerEmployeOption().getOption(employe);
        });
    }
   

    // Option pour supprimer un employé
    private Option supprimerEmploye(final Employe employe) {
        return new Option("Supprimer l'employé", "s", () -> {
            employe.remove();
            System.out.println("Employé supprimé : " + employe.getNom() + " " + employe.getPrenom());
        });
    }

    // Option pour supprimer une ligue
    private Option supprimer(Ligue ligue) {
        return new Option("Supprimer", "d", () -> {
            ligue.remove();
        });
    }

 // Option pour changer l'administrateur d'une ligue
 
    private Option changerAdministrateur(final Ligue ligue) {
        return new Option("Changer l'administrateur", "c", () -> {
            java.util.List<Employe> employes = new java.util.ArrayList<>(ligue.getEmployes());

            // Pour_afficher "Aucun employé" si la liste est vide
            if (employes.isEmpty()) {
                System.out.println("Aucun employé dans cette ligue.");
                return; // Quitter la méthode
            }

            // ici_pour_fficher la liste des employés
            System.out.println("Liste des employés :");
            for (int i = 0; i < employes.size(); i++) {
                System.out.println((i + 1) + ". " + employes.get(i).getNom() + " " + employes.get(i).getPrenom());
            }

            // Gestion de la saisie avec try-catch
            try {
                int choix = Integer.parseInt(getString("Choisissez le numéro de l'employé à désigner comme administrateur : ")) - 1;
                ligue.setAdministrateur(employes.get(choix)); // Définir l'administrateur
                System.out.println("L'administrateur a été changé avec succès.");
            } catch (NumberFormatException e) {
                System.out.println("Erreur : Veuillez entrer un numéro valide.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Erreur : Numéro invalide. Veuillez choisir un numéro entre 1 et " + employes.size() + ".");
            }
        });
    }

}
