package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import commandLineMenus.ListOption;
import commandLineMenus.Menu;
import commandLineMenus.Option;
import personnel.DateIncoherenteException;
import personnel.DateInvalideException;
import personnel.Employe;
import personnel.SauvegardeImpossible; // Importation de SauvegardeImpossible

public class EmployeConsole {

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

    private Option afficher(final Employe employe) {
        return new Option("Afficher l'employé", "l", () -> System.out.println(employe));
    }

    private Option changerNom(final Employe employe) {
        return new Option("Changer le nom", "n", () -> {
            try {
                employe.setNom(getString("Nouveau nom : "));
                System.out.println("Nom modifié avec succès.");
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de changer le nom : " + e.getMessage());
            }
        });
    }

    private Option changerPrenom(final Employe employe) {
        return new Option("Changer le prénom", "p", () -> {
            try {
                employe.setPrenom(getString("Nouveau prénom : "));
                System.out.println("Prénom modifié avec succès.");
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de changer le prénom : " + e.getMessage());
            }
        });
    }

    private Option changerMail(final Employe employe) {
        return new Option("Changer le mail", "e", () -> {
            try {
                employe.setMail(getString("Nouveau mail : "));
                System.out.println("Mail modifié avec succès.");
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de changer le mail : " + e.getMessage());
            }
        });
    }

    private Option changerPassword(final Employe employe) {
        return new Option("Changer le password", "x", () -> {
            try {
                employe.setPassword(getString("Nouveau password : "));
                System.out.println("Mot de passe modifié avec succès.");
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de changer le mot de passe : " + e.getMessage());
            }
        });
    }

    private Option changerDataArrivee(final Employe employe) {
        return new Option("Changer Date D'arrivée", "a", () -> {
            LocalDate dateArrivee = null;
            boolean validInput = false;
            while (!validInput) {
                String dateStr = getString("Nouvelle Date Arrivée (format AAAA-MM-JJ) : ");
                try {
                    dateArrivee = LocalDate.parse(dateStr);
                    employe.setDateArrivee(dateArrivee); // Propagera DateIncoherenteException, SauvegardeImpossible
                    System.out.println("Date d'arrivée modifiée : " + dateArrivee);
                    validInput = true;
                } catch (DateTimeParseException e) {
                    System.out.println("Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                } catch (DateIncoherenteException | DateInvalideException e) {
                    System.out.println("Erreur de date : " + e.getMessage());
                } catch (SauvegardeImpossible e) {
                    System.err.println("Impossible de sauvegarder la date d'arrivée : " + e.getMessage());
                    validInput = true; // Permet de sortir de la boucle si la sauvegarde échoue
                }
            }
        });
    }

    private Option changerDateDepart(final Employe employe) {
        return new Option("Changer Date de Départ", "d", () -> {
            LocalDate dateDepart = null;
            boolean validInput = false;
            while (!validInput) {
                String dateStr = getString("Nouvelle Date Départ (format AAAA-MM-JJ) : ");
                try {
                    dateDepart = LocalDate.parse(dateStr);
                    employe.setDateDepart(dateDepart); // Propagera DateIncoherenteException, SauvegardeImpossible
                    System.out.println("Date de départ modifiée : " + dateDepart);
                    validInput = true;
                } catch (DateTimeParseException e) {
                    System.out.println("Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                } catch (DateIncoherenteException | DateInvalideException e) {
                    System.out.println("Erreur de date : " + e.getMessage());
                } catch (SauvegardeImpossible e) {
                    System.err.println("Impossible de sauvegarder la date de départ : " + e.getMessage());
                    validInput = true; // Permet de sortir de la boucle si la sauvegarde échoue
                }
            }
        });
    }
}