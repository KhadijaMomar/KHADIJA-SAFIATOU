package commandLine;

import static commandLineMenus.rendering.examples.util.InOut.getString;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import commandLineMenus.List;
import commandLineMenus.Menu;
import commandLineMenus.Option;
import commandLineMenus.ListAction; // Importation ajoutée pour ListAction

import personnel.*;

public class LigueConsole {
    private GestionPersonnel gestionPersonnel;
    private EmployeConsole employeConsole;
    private PersonnelConsole personnelConsole; // Référence à la console principale pour l'utilisateur connecté

    /**
     * Constructeur de LigueConsole.
     * @param gestionPersonnel L'instance de GestionPersonnel.
     * @param employeConsole L'instance de EmployeConsole pour la gestion des employés.
     */
    public LigueConsole(GestionPersonnel gestionPersonnel, EmployeConsole employeConsole) {
        this.gestionPersonnel = gestionPersonnel;
        this.employeConsole = employeConsole;
    }

    /**
     * Définit la référence à PersonnelConsole.
     * @param personnelConsole La PersonnelConsole parente.
     */
    public void setPersonnelConsole(PersonnelConsole personnelConsole) {
        this.personnelConsole = personnelConsole;
    }

    /**
     * Crée le menu principal de gestion des ligues.
     * Les options disponibles dépendent du rôle de l'utilisateur connecté.
     * @return Le menu de gestion des ligues.
     */
    Menu menuLigues() {
        Menu menu = new Menu("Gérer les ligues", "l");
        menu.add(afficherLigues());

        // Seul le root peut ajouter une ligue
        if (personnelConsole.getUtilisateurConnecte().estRoot()) {
            menu.add(ajouterLigue());
        }

        menu.add(selectionnerLigue());
        menu.addBack("q");
        return menu;
    }

    /**
     * Option pour afficher toutes les ligues.
     * @return L'option de menu.
     */
    private Option afficherLigues() {
        return new Option("Afficher les ligues", "l", () -> {
            System.out.println(gestionPersonnel.getLigues());
        });
    }

    /**
     * Option pour afficher les détails d'une ligue spécifique.
     * @param ligue La ligue à afficher.
     * @return L'option de menu.
     */
    private Option afficher(final Ligue ligue) {
        return new Option("Afficher la ligue", "l", () -> {
            System.out.println(ligue);
            if (ligue.getAdministrateur() != null) {
                System.out.println("administrée par " + ligue.getAdministrateur().getNom() + " " + ligue.getAdministrateur().getPrenom());
            } else {
                System.out.println("administrée par personne (ou root si non assigné)");
            }
        });
    }

    /**
     * Option pour ajouter une nouvelle ligue.
     * Disponible uniquement pour l'utilisateur 'root'.
     * @return L'option de menu.
     */
    private Option ajouterLigue() {
        return new Option("Ajouter une ligue", "a", () -> {
            String nom = getString("Nom de la nouvelle ligue : ");
            try {
                // La logique de définition de l'administrateur par défaut (root) est dans GestionPersonnel.addLigue
                gestionPersonnel.addLigue(nom);
                System.out.println("Ligue " + nom + " ajoutée avec succès.");
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible d'ajouter la ligue : " + e.getMessage());
            }
        });
    }

    /**
     * Option pour sélectionner une ligue existante et accéder à son menu d'édition.
     * @return L'option de menu.
     */
    private Option selectionnerLigue() {
        return new List<Ligue>("Sélectionner une ligue", "e",
            () -> new ArrayList<>(gestionPersonnel.getLigues()), // Fournit la liste des ligues
            this::menuLigue); // Méthode appelée pour le menu de la ligue sélectionnée
    }

    /**
     * Crée le menu d'édition pour une ligue spécifique.
     * Les options disponibles dépendent du rôle de l'utilisateur connecté par rapport à cette ligue.
     * @param ligue La ligue à éditer.
     * @return Le menu d'édition de la ligue.
     */
    private Menu menuLigue(final Ligue ligue) {
        Menu menu = new Menu("Editer " + ligue.getNom());
        menu.add(afficher(ligue));
        menu.add(gererEmployes(ligue));

        Employe utilisateur = personnelConsole.getUtilisateurConnecte();
        // Seul le root ou l'administrateur de la ligue peut renommer ou supprimer la ligue
        if (utilisateur.estAdmin(ligue) || utilisateur.estRoot()) {
            menu.add(renommerLigue(ligue));
            menu.add(supprimerLigue(ligue));
        }

        menu.addBack("q");
        return menu;
    }

    /**
     * Option pour renommer une ligue.
     * Disponible uniquement pour le root ou l'administrateur de la ligue.
     * @param ligue La ligue à renommer.
     * @return L'option de menu.
     */
    private Option renommerLigue(final Ligue ligue) {
        return new Option("Renommer", "r", () -> {
            String nouveauNom = getString("Nouveau nom de la ligue : ");
            try {
                ligue.setNom(nouveauNom);
                System.out.println("Ligue renommée en " + nouveauNom + ".");
            } catch (IllegalArgumentException e) {
                System.out.println("Erreur : " + e.getMessage());
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de renommer la ligue : " + e.getMessage());
            }
        });
    }

    /**
     * Option pour supprimer une ligue.
     * Disponible uniquement pour le root ou l'administrateur de la ligue.
     * @param ligue La ligue à supprimer.
     * @return L'option de menu.
     */
    private Option supprimerLigue(final Ligue ligue) {
        return new Option("Supprimer", "d", () -> {
            try {
                ligue.remove(); // La méthode remove de Ligue gère la suppression des employés associés
                System.out.println("Ligue " + ligue.getNom() + " supprimée.");
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de supprimer la ligue : " + e.getMessage());
            } catch (IllegalArgumentException e) { // Capture IllegalArgumentException pour la suppression du root
                System.err.println("Impossible de supprimer la ligue : " + e.getMessage());
            }
        });
    }

    /**
     * Crée le menu de gestion des employés pour une ligue spécifique.
     * Les options d'ajout, sélection et changement d'administrateur dépendent du rôle de l'utilisateur.
     * @param ligue La ligue dont les employés sont à gérer.
     * @return Le menu de gestion des employés.
     */
    private Menu gererEmployes(final Ligue ligue) {
        Menu menu = new Menu("Gérer les employés de " + ligue.getNom(), "g");
        menu.add(afficherEmployes(ligue));

        Employe utilisateur = personnelConsole.getUtilisateurConnecte();
        // Seul le root ou l'administrateur de la ligue peut ajouter, sélectionner pour éditer ou changer l'administrateur
        if (utilisateur.estAdmin(ligue) || utilisateur.estRoot()) {
            menu.add(ajouterEmploye(ligue));
            // CORRECTION: Passe la ListOption directement à la List
            menu.add(new List<Employe>("Sélectionner un employé", "s", () -> {
                ArrayList<Employe> employesList = new ArrayList<>(ligue.getEmployes());
                // Retire l'employé root de la liste si présent, car il est géré séparément
                employesList.removeIf(e -> e.equals(gestionPersonnel.getRoot())); 
                return employesList;
            }, employeConsole.getMenuEmployeOption())); // Passe la ListOption
            
            menu.add(changerAdministrateur(ligue));
        }

        menu.addBack("q");
        return menu;
    }

    /**
     * Option pour afficher les employés d'une ligue.
     * @param ligue La ligue dont les employés sont à afficher.
     * @return L'option de menu.
     */
    private Option afficherEmployes(final Ligue ligue) {
        return new Option("Afficher les employés", "l", () -> {
            System.out.println(ligue.getEmployes());
        });
    }

    /**
     * Option pour ajouter un nouvel employé à une ligue.
     * Disponible uniquement pour le root ou l'administrateur de la ligue.
     * @param ligue La ligue à laquelle ajouter l'employé.
     * @return L'option de menu.
     */
    private Option ajouterEmploye(final Ligue ligue) {
        return new Option("Ajouter un employé", "a", () -> {
            try {
                String nom = getString("Nom de l'employé : ");
                String prenom = getString("Prénom de l'employé : ");
                String mail = getString("Mail de l'employé : ");
                String password = "";
                while (password.trim().isEmpty()) {
                    password = getString("Mot de passe de l'employé (ne peut être vide) : ");
                    if (password.trim().isEmpty()) {
                        System.out.println("Erreur : Le mot de passe ne peut pas être vide. Veuillez saisir un mot de passe.");
                    }
                }

                LocalDate dateArrivee = null;
                boolean dateArriveeValide = false;
                while (!dateArriveeValide) {
                    try {
                        String dateArriveeStr = getString("Date d'arrivée (format:AAAA-MM-JJ) : ");
                        dateArrivee = LocalDate.parse(dateArriveeStr);
                        dateArriveeValide = true;
                    } catch (DateTimeParseException e) {
                        System.out.println("Erreur : Format de date d'arrivée invalide. Utilisez le format AAAA-MM-JJ.");
                    }
                }

                LocalDate dateDepart = null;
                boolean dateDepartValide = false;
                while (!dateDepartValide) {
                    try {
                        String dateDepartStr = getString("Date de départ (format:AAAA-MM-JJ) (laissez vide si pas de départ) : ");
                        if (dateDepartStr.isEmpty()) {
                            dateDepartValide = true;
                        } else {
                            dateDepart = LocalDate.parse(dateDepartStr);
                            if (dateDepart.isBefore(dateArrivee)) {
                                System.out.println("Erreur : La date de départ ne peut pas être antérieure à la date d'arrivée.");
                            } else {
                                dateDepartValide = true;
                            }
                        }
                    } catch (DateTimeParseException e) {
                        System.out.println("Erreur : Format de date de départ invalide. Utilisez le format AAAA-MM-JJ.");
                    }
                }

                // Utilise la méthode addEmploye de Ligue qui délègue à GestionPersonnel
                ligue.addEmploye(nom, prenom, mail, password, dateArrivee, dateDepart);
                System.out.println("Employé " + nom + " " + prenom + " ajouté à la ligue " + ligue.getNom() + ".");

            } catch (IllegalArgumentException e) {
                System.err.println("Erreur lors de l'ajout de l'employé : " + e.getMessage());
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de sauvegarder le nouvel employé : " + e.getMessage());
            }
        });
    }

    /**
     * Option pour changer l'administrateur d'une ligue.
     * Disponible uniquement pour le root ou l'administrateur de la ligue.
     * @param ligue La ligue dont l'administrateur est à changer.
     * @return L'option de menu.
     */
    private Option changerAdministrateur(final Ligue ligue) {
        return new Option("Changer l'administrateur", "c", () -> {
            java.util.List<Employe> employes = new ArrayList<>(ligue.getEmployes());

            if (employes.isEmpty()) {
                System.out.println("Aucun employé dans cette ligue pour être désigné administrateur.");
                // Offrir de désigner 'root' si la ligue est vide et root existe
                if (gestionPersonnel.getRoot() != null) {
                    System.out.println("Voulez-vous désigner 'root' comme administrateur de cette ligue ? (o/n)");
                    String reponse = getString("").toLowerCase();
                    if (reponse.equals("o")) {
                        try {
                            ligue.setAdministrateur(gestionPersonnel.getRoot());
                            System.out.println("L'administrateur a été changé avec succès en 'root'.");
                        } catch (SauvegardeImpossible e) {
                            System.err.println("Impossible de sauvegarder le changement d'administrateur : " + e.getMessage());
                        }
                    }
                }
                return;
            }

            System.out.println("Liste des employés pouvant devenir administrateur :");
            for (int i = 0; i < employes.size(); i++) {
                System.out.println((i + 1) + ". " + employes.get(i).getNom() + " " + employes.get(i).getPrenom() + " (" + employes.get(i).getMail() + ")");
            }

            Employe currentAdmin = ligue.getAdministrateur();
            // Ajoute l'option 'root' si root n'est pas déjà l'administrateur actuel de la ligue
            if (gestionPersonnel.getRoot() != null && !gestionPersonnel.getRoot().equals(currentAdmin)) {
                System.out.println((employes.size() + 1) + ". root (super-utilisateur)");
            }

            try {
                int choixNum = Integer.parseInt(getString("Choisissez le numéro de l'employé à désigner comme administrateur : "));
                Employe nouvelAdmin = null;

                if (choixNum > 0 && choixNum <= employes.size()) {
                    nouvelAdmin = employes.get(choixNum - 1);
                } else if (gestionPersonnel.getRoot() != null && choixNum == employes.size() + 1 && !gestionPersonnel.getRoot().equals(currentAdmin)) {
                    nouvelAdmin = gestionPersonnel.getRoot();
                } else {
                    System.out.println("Erreur : Numéro invalide. Veuillez choisir un numéro valide.");
                    return;
                }

                ligue.setAdministrateur(nouvelAdmin); // Tente de définir le nouvel administrateur
                System.out.println("L'administrateur a été changé avec succès en " + nouvelAdmin.getNom() + " " + nouvelAdmin.getPrenom() + ".");

            } catch (NumberFormatException e) {
                System.out.println("Erreur : Veuillez entrer un numéro valide.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("Erreur : Numéro invalide. Veuillez choisir un numéro valide.");
            } catch (IllegalArgumentException e) {
                System.err.println("Erreur lors du changement d'administrateur : " + e.getMessage());
            } catch (SauvegardeImpossible e) {
                System.err.println("Impossible de sauvegarder le changement d'administrateur : " + e.getMessage());
            }
        });
    }
}