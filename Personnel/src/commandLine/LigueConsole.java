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

    Menu menuLigues() {
        Menu menu = new Menu("Gérer les ligues", "l");
        menu.add(afficherLigues());
        menu.add(ajouterLigue());
        menu.add(selectionnerLigue());
        menu.addBack("q");
        return menu;
    }

    private Option afficherLigues() {
        return new Option("Afficher les ligues", "l", () -> {
            System.out.println(gestionPersonnel.getLigues());
        });
    }

    private Option afficher(final Ligue ligue) {
        return new Option("Afficher la ligue", "l", () -> {
            System.out.println(ligue);
            System.out.println("administrée par " + ligue.getAdministrateur());
        });
    }

    private Option afficherEmployes(final Ligue ligue) {
        return new Option("Afficher les employes", "l", () -> {
            System.out.println(ligue.getEmployes());
        });
    }

    private Option ajouterLigue() {
        return new Option("Ajouter une ligue", "a", () -> {
            try {
                gestionPersonnel.addLigue(getString("nom : "));
                System.out.println("Ligue ajoutée avec succès.");
            } catch (SauvegardeImpossible exception) {
                System.err.println("Impossible d'ajouter cette ligue : " + exception.getMessage());
            }
        });
    }

    private Menu editerLigue(Ligue ligue) {
        Menu menu = new Menu("Editer " + ligue.getNom());
        menu.add(afficher(ligue));
        menu.add(gererEmployes(ligue));
        menu.add(changerNom(ligue));
        menu.add(supprimer(ligue));
        menu.addBack("q");
        return menu;
    }

    private Option changerNom(final Ligue ligue) {
        return new Option("Renommer", "r", () -> {
            try {
                ligue.setNom(getString("Nouveau nom : "));
                System.out.println("Nom de la ligue modifié avec succès.");
            } catch (SauvegardeImpossible e) {
                System.err.println("Erreur lors de la mise à jour du nom de la ligue : " + e.getMessage());
            } catch (IllegalArgumentException e) { // Capture de l'exception si le nom est vide
                System.err.println("Erreur : " + e.getMessage());
            }
        });
    }

    private List<Ligue> selectionnerLigue() {
        return new List<Ligue>("Sélectionner une ligue", "e",
                () -> new ArrayList<>(gestionPersonnel.getLigues()),
                (element) -> editerLigue(element));
    }

    private Option ajouterEmploye(final Ligue ligue) {
        return new Option("Ajouter un employé", "a", () -> {
            String nom = getString("Nom de l'employé : ");
            String prenom = getString("Prénom de l'employé : ");
            String mail = getString("Mail de l'employé : ");
            String password = getString("Mot de passe de l'employé : ");

            LocalDate dateArrivee = null;
            while (dateArrivee == null) {
                String dateArriveeStr = getString("Date d'arrivée (format:AAAA-MM-JJ) : ");
                try {
                    dateArrivee = LocalDate.parse(dateArriveeStr);
                } catch (DateTimeParseException e) {
                    System.out.println("Format de date incorrect. Veuillez utiliser le format AAAA-MM-JJ.");
                }
            }

            LocalDate dateDepart = null;
            boolean validDepartDate = false;
            while (!validDepartDate) {
                String dateDepartStr = getString("Date de départ (format:AAAA-MM-JJ) (laissez vide si pas de départ) : ");
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
                        System.out.println("Format de date incorrect. Veuillez utiliser le format AAAA-MM-JJ.");
                    }
                }
            }

            try {
                ligue.addEmploye(nom, prenom, mail, password, dateArrivee, dateDepart);
                System.out.println("Employé ajouté avec succès.");
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible d'ajouter l'employé : " + e.getMessage());
            }
        });
    }

    private Menu gererEmployes(Ligue ligue) {
        Menu menu = new Menu("Gérer les employés de " + ligue.getNom(), "e");
        menu.add(afficherEmployes(ligue));
        menu.add(ajouterEmploye(ligue));
        menu.add(selectionnerEmploye(ligue));
        menu.add(changerAdministrateur(ligue));
        menu.addBack("q");
        return menu;
    }

    private List<Employe> selectionnerEmploye(final Ligue ligue) {
        return new List<>("Sélectionner un employé", "s",
                () -> new ArrayList<>(ligue.getEmployes()),
                (element) -> menuActionsEmploye(ligue, element));
    }

    private Menu menuActionsEmploye(Ligue ligue, Employe employe) {
        Menu menu = new Menu("Actions pour " + employe.getNom() + " " + employe.getPrenom(), "a");
        menu.add(employeConsole.getEditerEmployeOption().getOption(employe));
        menu.add(supprimerEmploye(employe));
        menu.addBack("q");
        return menu;
    }

    private Option modifierEmploye(final Employe employe) {
        return new Option("Modifier l'employé", "m", () -> {
            employeConsole.getEditerEmployeOption().getOption(employe);
        });
    }

    private Option supprimerEmploye(final Employe employe) {
        return new Option("Supprimer l'employé", "s", () -> {
            try {
                String nomEmploye = employe.getNom() + " " + employe.getPrenom(); // Garder le nom avant suppression
                employe.remove();
                System.out.println("Employé supprimé : " + nomEmploye);
            } catch (ImpossibleDeSupprimerRoot e) {
                System.err.println("Erreur : " + e.getMessage());
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de supprimer l'employé : " + e.getMessage());
            }
        });
    }

    private Option supprimer(Ligue ligue) {
        return new Option("Supprimer", "d", () -> {
            try {
                String nomLigue = ligue.getNom(); // Garder le nom avant suppression
                ligue.remove();
                System.out.println("Ligue '" + nomLigue + "' supprimée.");
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de supprimer la ligue : " + e.getMessage());
            } catch (ImpossibleDeSupprimerRoot e) {
                System.err.println("Erreur lors de la suppression de la ligue : " + e.getMessage());
            }
        });
    }

    private Option changerAdministrateur(final Ligue ligue) {
        return new Option("Changer l'administrateur", "c", () -> {
            java.util.List<Employe> employes = new java.util.ArrayList<>(ligue.getEmployes());

            if (employes.isEmpty()) {
                System.out.println("Aucun employé dans cette ligue.");
                return;
            }

            System.out.println("Liste des employés :");
            for (int i = 0; i < employes.size(); i++) {
                System.out.println((i + 1) + ". " + employes.get(i).getNom() + " " + employes.get(i).getPrenom());
            }

            try {
                int choix = Integer.parseInt(getString("Choisissez le numéro de l'employé à désigner comme administrateur : ")) - 1;
                Employe nouvelAdmin = employes.get(choix);
                ligue.setAdministrateur(nouvelAdmin);
                System.out.println("L'administrateur a été changé avec succès en " + nouvelAdmin.getNom() + " " + nouvelAdmin.getPrenom() + ".");
            } catch (NumberFormatException e) {
                System.out.println("Erreur : Veuillez entrer un numéro valide.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Erreur : Numéro invalide. Veuillez choisir un numéro entre 1 et " + employes.size() + ".");
            } catch (IllegalArgumentException e) {
                System.err.println("Erreur lors du changement d'administrateur : " + e.getMessage());
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de sauvegarder le changement d'administrateur : " + e.getMessage());
            }
        });
    }
}