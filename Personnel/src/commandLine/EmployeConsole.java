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
        return new Option("Changer le nom", "n", () -> employe.setNom(getString("Nouveau nom : ")));
    }

    private Option changerPrenom(final Employe employe) {
        return new Option("Changer le prénom", "p", () -> employe.setPrenom(getString("Nouveau prénom : ")));
    }

    private Option changerMail(final Employe employe) {
        return new Option("Changer le mail", "e", () -> employe.setMail(getString("Nouveau mail : ")));
    }

    private Option changerPassword(final Employe employe) {
        return new Option("Changer le password", "x", () -> employe.setPassword(getString("Nouveau password : ")));
    }

    
    
    private Option changerDataArrivee(final Employe employe) {
        return new Option("Changer Date D'arrivée", "a", () -> {
            try {
                String dateStr = getString("Nouvelle Date Arrivée (format AAAA-MM-JJ) : ");
                LocalDate dateArrivee = LocalDate.parse(dateStr); // Conversion de la chaîne en LocalDate
                employe.setDateArrivee(dateArrivee); // Mise à jour de la date d'arrivée
                System.out.println("Date d'arrivée modifiée : " + dateArrivee);
            } catch (DateTimeParseException e) {
                System.out.println("Format de date invalide. Utilisez le format AAAA-MM-JJ.");
            } catch (DateInvalideException | DateIncoherenteException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Une erreur inattendue s'est produite.");
                e.printStackTrace(); // Debugging
            }
        });
    }

    private Option changerDateDepart(final Employe employe) {
        return new Option("Changer Date de Départ", "d", () -> {
            try {
                String dateStr = getString("Nouvelle Date Départ (format AAAA-MM-JJ) : ");
                LocalDate dateDepart = LocalDate.parse(dateStr); // Conversion de la chaîne en LocalDate
                employe.setDateDepart(dateDepart); // Mise à jour de la date de départ
                System.out.println("Date de départ modifiée : " + dateDepart);
            } catch (DateTimeParseException e) {
                System.out.println("Format de date invalide. Utilisez le format AAAA-MM-JJ.");
            } catch (DateInvalideException | DateIncoherenteException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Une erreur inattendue s'est produite.");
                e.printStackTrace(); // Debugging
            }
        });
    }
    }