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
            } catch (SauvegardeImpossible exception) {
                System.err.println("Impossible de sauvegarder cette ligue");
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
            ligue.setNom(getString("Nouveau nom : "));
        });
    }

    private List<Ligue> selectionnerLigue() {
        return new List<Ligue>("Sélectionner une ligue", "e",
                () -> new ArrayList<>(gestionPersonnel.getLigues()),
                (element) -> editerLigue(element));
    }

    private Option ajouterEmploye(final Ligue ligue) {
        return new Option("Ajouter un employé", "a", () -> {
            // Demander les informations de base
            String nom = getString("Nom : ");
            String prenom = getString("Prénom : ");
            String mail = getString("Mail : ");
            String password = getString("Password : ");

            // Demander la date d'arrivée
            LocalDate dateArrivee = null;
            boolean dateArriveeValide = false;
            while (!dateArriveeValide) {
                try {
                    String dateArriveeStr = getString("Date d'arrivée (AAAA-MM-JJ) : ");

                    // Vérifier si la date est vide
                    if (dateArriveeStr.isEmpty()) {
                        System.err.println("Erreur : La date d'arrivée ne peut pas être vide.");
                        continue; // Redemander la date
                    }

                    dateArrivee = LocalDate.parse(dateArriveeStr);

                    // Vérifier que la date n'est pas dans le futur
                    if (dateArrivee.isAfter(LocalDate.now())) {
                        System.err.println("Erreur : La date d'arrivée ne peut pas être dans le futur.");
                    } else {
                        dateArriveeValide = true; // La date est valide
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Erreur : Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                }
            }

            // Demander la date de départ (optionnelle)
            LocalDate dateDepart = null;
            boolean dateDepartValide = false;
            while (!dateDepartValide) {
                String dateDepartStr = getString("Date de départ (AAAA-MM-JJ, laissez vide si non applicable) : ");
                if (dateDepartStr.isEmpty()) {
                    dateDepartValide = true; // Pas de date de départ
                } else {
                    try {
                        dateDepart = LocalDate.parse(dateDepartStr);
                        if (dateDepart.isBefore(dateArrivee)) {
                            System.err.println("Erreur : La date de départ ne peut pas être avant la date d'arrivée.");
                        } else {
                            dateDepartValide = true; // La date est valide
                        }
                    } catch (DateTimeParseException e) {
                        System.err.println("Erreur : Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                    }
                }
            }

            // Ajouter l'employé avec les dates
            try {
                ligue.addEmploye(nom, prenom, mail, password, dateArrivee, dateDepart);
                System.out.println("Employé ajouté avec succès !");
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de l'employé : " + e.getMessage());
            }
        });
    }

    private Menu gererEmployes(Ligue ligue) {
        Menu menu = new Menu("Gérer les employés de " + ligue.getNom(), "e");
        menu.add(afficherEmployes(ligue));
        menu.add(ajouterEmploye(ligue));
        menu.add(modifierEmploye(ligue));
        menu.add(supprimerEmploye(ligue));
        menu.addBack("q");
        return menu;
    }

    private List<Employe> supprimerEmploye(final Ligue ligue) {
        return new List<>("Supprimer un employé", "s",
                () -> new ArrayList<>(ligue.getEmployes()),
                (index, element) -> {
                    element.remove();
                });
    }

    private List<Employe> changerAdministrateur(final Ligue ligue) {
        return null;
    }

    private List<Employe> modifierEmploye(final Ligue ligue) {
        return new List<>("Modifier un employé", "e",
                () -> new ArrayList<>(ligue.getEmployes()),
                employeConsole.editerEmploye());
    }

    private Option supprimer(Ligue ligue) {
        return new Option("Supprimer", "d", () -> {
            ligue.remove();
        });
    }
}