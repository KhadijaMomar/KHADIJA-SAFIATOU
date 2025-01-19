package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import commandLineMenus.List;
import commandLineMenus.Menu;
import commandLineMenus.Option;

import personnel.*;

public class LigueConsole 
{
	private GestionPersonnel gestionPersonnel;
	private EmployeConsole employeConsole;

	public LigueConsole(GestionPersonnel gestionPersonnel, EmployeConsole employeConsole)
	{
		this.gestionPersonnel = gestionPersonnel;
		this.employeConsole = employeConsole;
	}

	Menu menuLigues()
	{
		Menu menu = new Menu("Gérer les ligues", "l");
		menu.add(afficherLigues());
		menu.add(ajouterLigue());
		menu.add(selectionnerLigue());
		menu.addBack("q");
		return menu;
	}

	private Option afficherLigues()
	{
		return new Option("Afficher les ligues", "l", () -> {System.out.println(gestionPersonnel.getLigues());});
	}

	private Option afficher(final Ligue ligue)
	{
		return new Option("Afficher la ligue", "l", 
				() -> 
				{
					System.out.println(ligue);
					System.out.println("administrée par " + ligue.getAdministrateur());
				}
		);
	}
	private Option afficherEmployes(final Ligue ligue)
	{
		return new Option("Afficher les employes", "l", () -> {System.out.println(ligue.getEmployes());});
	}

	private Option ajouterLigue()
	{
		return new Option("Ajouter une ligue", "a", () -> 
		{
			try
			{
				gestionPersonnel.addLigue(getString("nom : "));
			}
			catch(SauvegardeImpossible exception)
			{
				System.err.println("Impossible de sauvegarder cette ligue");
			}
		});
	}
	
	private Menu editerLigue(Ligue ligue)
	{
		Menu menu = new Menu("Editer " + ligue.getNom());
		menu.add(afficher(ligue));
		menu.add(gererEmployes(ligue));
		//menu.add(changerAdministrateur(ligue));
		menu.add(changerNom(ligue));
		menu.add(supprimer(ligue));
		menu.addBack("q");
		return menu;
	}

	private Option changerNom(final Ligue ligue)
	{
		return new Option("Renommer", "r", 
				() -> {ligue.setNom(getString("Nouveau nom : "));});
	}

	private List<Ligue> selectionnerLigue()
	{
		return new List<Ligue>("Sélectionner une ligue", "e", 
				() -> new ArrayList<>(gestionPersonnel.getLigues()),
				(element) -> editerLigue(element)
				);
	}
	
	/*private Option ajouterEmploye(final Ligue ligue)
	{
		return new Option("ajouter un employé", "a",
				() -> 
				{
					ligue.addEmploye(getString("nom : "), 
						getString("prenom : "), 
						getString("mail : "), 
						getString("password : "));
				}
		);
	}*/
	
	private Option ajouterEmploye(final Ligue ligue) {
	    return new Option("Ajouter un employé", "a", () -> {
	        // Demander les informations de base
	        String nom = getString("Nom : ");
	        String prenom = getString("Prénom : ");
	        String mail = getString("Mail : ");
	        String password = getString("Password : ");

	       
	     // Demander la date d'arrivée
	        LocalDate dateArrivee = null;
	        try {
	            // Saisie de la date et conversion en LocalDate
	            String dateArriveeStr = getString("Date d'arrivée (AAAA-MM-JJ) : ");
	            dateArrivee = LocalDate.parse(dateArriveeStr);

	            // Vérifier que la date n'est pas dans le futur
	            if (dateArrivee.isAfter(LocalDate.now())) {
	                throw new IllegalArgumentException("La date d'arrivée ne peut pas être dans le futur.");
	            }

	        } catch (DateTimeParseException e) {
	            // Gestion des erreurs de format de date
	            throw new IllegalArgumentException("Format de date invalide. Utilisez le format AAAA-MM-JJ.");
	        }

	        // Vérifier que la date d'arrivée n'est pas null
	        if (dateArrivee == null) {
	            throw new IllegalArgumentException("Veuillez saisir la date d'arrivée de l'employé.");
	        }
	        
	        
	        
	        // Demander la date de départ (optionnelle)
	        LocalDate dateDepart = null;
	        String dateDepartStr = getString("Date de départ (AAAA-MM-JJ, laissez vide si non applicable) : ");
	        if (!dateDepartStr.isEmpty()) {
	            dateDepart = LocalDate.parse(dateDepartStr);
	        }

	        // Ajouter l'employé avec les dates
	        ligue.addEmploye(nom, prenom, mail, password, dateArrivee, dateDepart);
	    });
	}
	
	
	
	
	
	
	private Menu gererEmployes(Ligue ligue)
	{
		Menu menu = new Menu("Gérer les employés de " + ligue.getNom(), "e");
		menu.add(afficherEmployes(ligue));
		menu.add(ajouterEmploye(ligue));
		menu.add(modifierEmploye(ligue));
		menu.add(supprimerEmploye(ligue));
		menu.addBack("q");
		return menu;
	}

	private List<Employe> supprimerEmploye(final Ligue ligue)
	{
		return new List<>("Supprimer un employé", "s", 
				() -> new ArrayList<>(ligue.getEmployes()),
				(index, element) -> {element.remove();}
				);
	}
	
	private List<Employe> changerAdministrateur(final Ligue ligue)
	{
		return null;
	}		

	private List<Employe> modifierEmploye(final Ligue ligue)
	{
		return new List<>("Modifier un employé", "e", 
				() -> new ArrayList<>(ligue.getEmployes()),
				employeConsole.editerEmploye()
				);
	}
	
	private Option supprimer(Ligue ligue)
	{
		return new Option("Supprimer", "d", () -> {ligue.remove();});
	}
	
}
