package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;


import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import commandLineMenus.ListOption;
import commandLineMenus.Menu;
import commandLineMenus.Option;
import personnel.Employe;

public class EmployeConsole {
    private Option afficher(final Employe employe) {
        return new Option("Afficher l'employé", "l", () -> {
            System.out.println(employe);
        });
    }

    ListOption<Employe> editerEmploye() {
        return (employe) -> editerEmploye(employe);
    }

    Option editerEmploye(Employe employe) {
        Menu menu = new Menu("Gérer le compte " + employe.getNom(), "c");
        menu.add(afficher(employe));
        menu.add(changerNom(employe));
        menu.add(changerPrenom(employe));
        menu.add(changerMail(employe));
        menu.add(changerPassword(employe));
        menu.add(changerDateArrivee(employe)); // Ajout Date Arrivée
        menu.add(changerDateDepart(employe));  // Ajout Date Départ
        menu.addBack("q");
        return menu;
    }

    private Option changerNom(final Employe employe) {
        return new Option("Changer le nom", "n", () -> {
            employe.setNom(getString("Nouveau nom : "));
        });
    }

    private Option changerPrenom(final Employe employe) {
        return new Option("Changer le prénom", "p", () -> {
            employe.setPrenom(getString("Nouveau prénom : "));
        });
    }

    private Option changerMail(final Employe employe) {
        return new Option("Changer le mail", "e", () -> {
            employe.setMail(getString("Nouveau mail : "));
        });
    }

    private Option changerPassword(final Employe employe) {
        return new Option("Changer le password", "x", () -> {
            employe.setPassword(getString("Nouveau password : "));
        });
    }

    // *************** Pour modifier les Dates *********

    private Option changerDateArrivee(final Employe employe) {
        return new Option("Changer la Date d'arrivée", "k", () -> {
            boolean dateValide = false;
            while (!dateValide) {
                try {
                    // Demander la date sous forme de String
                    String dateStr = getString("Nouvelle Date Arrivee (AAAA-MM-JJ) : ");

                    // Vérifier si la date est vide
                    if (dateStr.isEmpty()) {
                        System.err.println("Erreur : La date d'arrivée ne peut pas être vide.");
                        continue; // Redemander la date
                    }

                    // Convertir la String en LocalDate
                    LocalDate nouvelleDateArrivee = LocalDate.parse(dateStr);

                    // Valider la cohérence des dates
                    if (employe.getDateDepart() != null && nouvelleDateArrivee.isAfter(employe.getDateDepart())) {
                        System.err.println("Erreur : La date d'arrivée ne peut pas être après la date de départ.");
                        continue; // Redemander la date
                    }

                    if (nouvelleDateArrivee.isAfter(LocalDate.now())) {
                        System.err.println("Erreur : La date d'arrivée ne peut pas être dans le futur.");
                        continue; // Redemander la date
                    }

                    // Utiliser la LocalDate
                    employe.setDateArrivee(nouvelleDateArrivee);
                    System.out.println("Date d'arrivée modifiée avec succès !");
                    dateValide = true; // La date est valide

                } catch (DateTimeParseException e) {
                    System.err.println("Erreur : Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                }
            }
        });
    }

    private Option changerDateDepart(final Employe employe) {
        return new Option("Changer la Date de Depart", "s", () -> {
            boolean dateValide = false;
            while (!dateValide) {
                try {
                    // Demander la date sous forme de String
                    String dateStr = getString("Nouvelle Date Depart (AAAA-MM-JJ) : ");

                    // Si l'utilisateur laisse le champ vide, on considère qu'il n'y a pas de date de départ
                    if (dateStr.isEmpty()) {
                        employe.setDateDepart(null);
                        System.out.println("Date de départ supprimée avec succès !");
                        dateValide = true;
                        continue;
                    }

                    // Convertir la String en LocalDate
                    LocalDate nouvelleDateDepart = LocalDate.parse(dateStr);

                    // Valider la cohérence des dates
                    if (nouvelleDateDepart.isBefore(employe.getDateArrive())) {
                        System.err.println("Erreur : La date de départ ne peut pas être avant la date d'arrivée.");
                        continue; // Redemander la date
                    }

                    if (nouvelleDateDepart.isBefore(LocalDate.now())) {
                        System.err.println("Erreur : La date de départ ne peut pas être dans le passé.");
                        continue; // Redemander la date
                    }

                    // Utiliser la LocalDate
                    employe.setDateDepart(nouvelleDateDepart);
                    System.out.println("Date de départ modifiée avec succès !");
                    dateValide = true; // La date est valide

                } catch (DateTimeParseException e) {
                    System.err.println("Erreur : Format de date invalide. Utilisez le format AAAA-MM-JJ.");
                }
            }
        });
    }
}