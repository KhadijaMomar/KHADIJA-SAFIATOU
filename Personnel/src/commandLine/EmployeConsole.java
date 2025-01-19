package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;

import java.time.LocalDate;

import commandLineMenus.ListOption;
import commandLineMenus.Menu;
import commandLineMenus.Option;
import personnel.Employe;

public class EmployeConsole 
{
	private Option afficher(final Employe employe)
	{
		return new Option("Afficher l'employé", "l", () -> {System.out.println(employe);});
	}

	ListOption<Employe> editerEmploye()
	{
		return (employe) -> editerEmploye(employe);		
	}

	Option editerEmploye(Employe employe)
	{
			Menu menu = new Menu("Gérer le compte " + employe.getNom(), "c");
			menu.add(afficher(employe));
			menu.add(changerNom(employe));
			menu.add(changerPrenom(employe));
			menu.add(changerMail(employe));
			menu.add(changerPassword(employe));
			menu.add(changerDateArrivee(employe)); // Ajout Arri
			menu.add(changerDateDepart(employe));  // Ajout Dep
			menu.addBack("q");
			return menu;
	}

	private Option changerNom(final Employe employe)
	{
		return new Option("Changer le nom", "n", 
				() -> {employe.setNom(getString("Nouveau nom : "));}
			);
	}
	
	private Option changerPrenom(final Employe employe)
	{
		return new Option("Changer le prénom", "p", () -> {employe.setPrenom(getString("Nouveau prénom : "));});
	}
	
	private Option changerMail(final Employe employe)
	{
		return new Option("Changer le mail", "e", () -> {employe.setMail(getString("Nouveau mail : "));});
	}
	
	private Option changerPassword(final Employe employe)
	{
		return new Option("Changer le password", "x", () -> {employe.setPassword(getString("Nouveau password : "));});
	}
	
	 // ***************Ajout des Dates*********
	
	private Option changerDateArrivee(final Employe employe) {
	    return new Option("Changer la Date d'arrivée", "k", () -> {
	        // Demander la date sous forme de String
	        String dateStr = getString("Nouvelle Date Arrivee (AAAA-MM-JJ) : ");

	        // Convertir la String en LocalDate
	        LocalDate nouvelleDateArrivee = LocalDate.parse(dateStr);

	        // Valider la cohérence des dates
	        if (employe.getDateDepart() != null && nouvelleDateArrivee.isAfter(employe.getDateDepart())) {
	            throw new IllegalArgumentException("La date d'arrivée ne peut pas être après la date de départ.");
	        }

	        if (nouvelleDateArrivee.isAfter(LocalDate.now())) {
	            throw new IllegalArgumentException("La date d'arrivée ne peut pas être dans le futur.");
	        }

	        // Utiliser la LocalDate
	        employe.setDateArrivee(nouvelleDateArrivee);

	        System.out.println("Date d'arrivée modifiée avec succès !");
	    });
	}
	
	private Option changerDateDepart(final Employe employe) {
	    return new Option("Changer la Date de Depart", "s", () -> {
	        // Demander la date sous forme de String
	        String dateStr = getString("Nouvelle Date Depart (AAAA-MM-JJ) : ");

	        // Convertir la String en LocalDate
	        LocalDate nouvelleDateDepart = LocalDate.parse(dateStr);

	        // Valider la cohérence des dates
	        if (employe.getDateDepart() != null && nouvelleDateDepart.isBefore(employe.getDateDepart())) {
	            throw new IllegalArgumentException("La date de depart ne peut pas être avant la date d'arriveé.");
	        }

	        if (nouvelleDateDepart.isBefore(LocalDate.now())) {
	            throw new IllegalArgumentException("La date de depart ne peut pas être dans le futur.");
	        }

	        // Utiliser la LocalDate
	        employe.setDateDepart(nouvelleDateDepart);

	        System.out.println("Date de depart modifiée avec succès !");
	    });
	}
	
	
	
	
	
	

}
