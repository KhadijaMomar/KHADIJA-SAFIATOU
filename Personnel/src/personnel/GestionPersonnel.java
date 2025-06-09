package personnel;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.HashSet; // Pour collecter tous les employés
import java.util.Set; // Pour collecter tous les employés

public class GestionPersonnel implements Serializable {
    private static final long serialVersionUID = -105283113987886425L;
    private static GestionPersonnel gestionPersonnel = null;
    private SortedSet<Ligue> ligues;
    private Employe root; // Garde une référence à l'employé root

    public final static int SERIALIZATION = 1, JDBC = 2;
    public final static int TYPE_PASSERELLE = JDBC; // Utilise JDBC par défaut

    private static Passerelle passerelle;
    

    /**
     * Retourne l'instance unique de GestionPersonnel (Singleton).
     * Initialise la passerelle et charge les données si c'est la première fois.
     * @return L'instance de GestionPersonnel.
     * @throws RuntimeException Si une erreur fatale se produit lors du chargement des données.
     */
    public static GestionPersonnel getGestionPersonnel() {
        if (gestionPersonnel == null) {
            gestionPersonnel = new GestionPersonnel();
            try {
                // Initialise la passerelle (JDBC dans ce cas)
                if (TYPE_PASSERELLE == JDBC) {
                    passerelle = new jdbc.JDBC(gestionPersonnel); // Passe l'instance de GestionPersonnel
                }
                // Charge toutes les ligues et leurs employés, y compris le root si existant
                // Cette méthode va peupler les collections internes de gestionPersonnel
                passerelle.getGestionPersonnel();

                // S'assure que root est bien défini, le crée si inexistant dans la BD
                gestionPersonnel.initialiserRoot(); 
            } catch (SauvegardeImpossible e) {
                // Gère les erreurs de chargement des données
                throw new RuntimeException("Impossible de charger les données : " + e.getMessage(), e); 
            }
        }
        return gestionPersonnel;
    }
    
    /**
     * Constructeur privé pour le pattern Singleton.
     * Initialise la collection de ligues.
     */
    private GestionPersonnel() {
        ligues = new TreeSet<>();
    }

    /**
     * Retourne une vue non modifiable de la collection des ligues.
     * @return Un SortedSet non modifiable de Ligue.
     */
    public SortedSet<Ligue> getLigues() {
        return Collections.unmodifiableSortedSet(ligues);
    }

    /**
     * Retourne une collection de tous les employés gérés par cette instance de GestionPersonnel.
     * Inclut le root et tous les employés de toutes les ligues.
     * @return Un SortedSet non modifiable de tous les employés.
     */
    public SortedSet<Employe> getEmployes() {
        SortedSet<Employe> allEmployes = new TreeSet<>();
        if (root != null) {
            allEmployes.add(root);
        }
        for (Ligue ligue : ligues) {
            allEmployes.addAll(ligue.getEmployes());
        }
        return Collections.unmodifiableSortedSet(allEmployes);
    }

    /**
     * Ajoute une nouvelle ligue à la gestion du personnel et la persiste en base de données.
     * @param nom Le nom de la nouvelle ligue.
     * @return La ligue nouvellement créée.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     * @throws IllegalArgumentException Si le nom est vide ou si une ligue avec ce nom existe déjà.
     */
    public Ligue addLigue(String nom) throws SauvegardeImpossible, IllegalArgumentException {
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la ligue ne peut pas être vide.");
        }
        if (getLigue(nom) != null) {
            throw new IllegalArgumentException("Une ligue avec ce nom existe déjà.");
        }
        Ligue ligue = new Ligue(this, nom); // Le constructeur insère la ligue dans la BD
        ligues.add(ligue); // Ajoute la ligue à la collection interne
        // Définit le root comme administrateur par défaut si c'est la première ligue et si root existe
        if (root != null && ligue.getAdministrateur() == null) {
            ligue.setAdministrateur(root); // Ceci appellera update(Ligue) via la passerelle
        }
        return ligue;
    }

    /**
     * Ajoute un nouvel employé à une ligue et le persiste en base de données.
     * @param ligue La ligue à laquelle l'employé appartient.
     * @param nom Le nom de l'employé.
     * @param prenom Le prénom de l'employé.
     * @param mail L'adresse mail de l'employé.
     * @param password Le mot de passe de l'employé.
     * @param dateArrivee La date d'arrivée de l'employé.
     * @param dateDepart La date de départ de l'employé.
     * @return L'employé nouvellement créé.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     * @throws IllegalArgumentException Si les données fournies sont invalides.
     */
    public Employe addEmploye(Ligue ligue, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart) throws SauvegardeImpossible, IllegalArgumentException {
        if (ligue == null) {
            throw new IllegalArgumentException("Un employé doit être associé à une ligue.");
        }
        if (mail == null || !mail.contains("@")) {
            throw new IllegalArgumentException("Adresse mail invalide.");
        }
        if (getEmploye(mail) != null) {
            throw new IllegalArgumentException("Un employé avec cet email existe déjà.");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }

        // Le constructeur d'Employe gère l'insertion en base de données et l'ajout à la ligue
        Employe employe = new Employe(this, ligue, nom, prenom, mail, password, dateArrivee, dateDepart);
        return employe;
    }

    /**
     * Récupère une ligue par son nom.
     * @param nom Le nom de la ligue.
     * @return La ligue correspondante, ou null si non trouvée.
     */
    public Ligue getLigue(String nom) {
        for (Ligue ligue : ligues) {
            if (ligue.getNom().equals(nom)) {
                return ligue;
            }
        }
        return null;
    }
    
    /**
     * Récupère une ligue par son ID.
     * @param id L'ID de la ligue.
     * @return La ligue correspondante, ou null si non trouvée.
     */
    public Ligue getLigue(int id) {
        for (Ligue ligue : ligues) {
            if (ligue.getId() == id) {
                return ligue;
            }
        }
        return null;
    }

    /**
     * Récupère un employé par son adresse mail.
     * @param mail L'adresse mail de l'employé.
     * @return L'employé correspondant, ou null si non trouvé.
     */
    public Employe getEmploye(String mail) {
        // Cherche le root en premier
        if (root != null && root.getMail().equalsIgnoreCase(mail)) {
            return root;
        }
        // Parcourt les ligues pour trouver l'employé
        for (Ligue ligue : ligues) {
            for (Employe employe : ligue.getEmployes()) {
                if (employe.getMail().equalsIgnoreCase(mail)) {
                    return employe;
                }
            }
        }
        return null;
    }
    
    /**
     * Récupère un employé par son ID.
     * @param id L'ID de l'employé.
     * @return L'employé correspondant, ou null si non trouvé.
     */
    public Employe getEmploye(int id) {
        // Cherche le root en premier
        if (root != null && root.getId() == id) {
            return root;
        }
        // Parcourt les ligues pour trouver l'employé par ID
        for (Ligue ligue : ligues) {
            for (Employe employe : ligue.getEmployes()) {
                if (employe.getId() == id) {
                    return employe;
                }
            }
        }
        return null;
    }

    /**
     * Authentifie un employé en utilisant son nom d'utilisateur (mail ou nom) et son mot de passe.
     * @param nomUtilisateur Le mail ou le nom de l'employé.
     * @param password Le mot de passe de l'employé.
     * @return L'employé authentifié, ou null si l'authentification échoue.
     * @throws SauvegardeImpossible Si une erreur se produit lors de l'accès à la base de données.
     */
    public Employe authentifier(String nomUtilisateur, String password) throws SauvegardeImpossible {
        Employe employe = null;
        
        // Tente de trouver l'employé par mail d'abord
        employe = getEmploye(nomUtilisateur); 
        
        // Si non trouvé par mail, tente de trouver par nom via la passerelle
        if (employe == null) {
            if (passerelle != null) {
                employe = passerelle.getEmployeByNom(nomUtilisateur);
            }
        }

        if (employe != null) {
            if (employe.getPassword().equals(password)) {
                return employe;
            }
        }
        return null; // Authentification échouée
    }

    // Méthodes d'insertion, mise à jour et suppression (qui délèguent à la passerelle)
    public int insert(Ligue ligue) throws SauvegardeImpossible {
        return passerelle.insert(ligue);
    }

    public int insert(Employe employe) throws SauvegardeImpossible {
        return passerelle.insert(employe);
    }

    public void update(Ligue ligue) throws SauvegardeImpossible {
        passerelle.update(ligue);
    }

    public void update(Employe employe) throws SauvegardeImpossible {
        passerelle.update(employe);
    }

    public void remove(Ligue ligue) throws SauvegardeImpossible {
        // Supprime la ligue de la collection locale et de la BD
        ligues.remove(ligue);
        passerelle.delete(ligue);
    }

    public void remove(Employe employe) throws SauvegardeImpossible {
        // Supprime l'employé de sa ligue si elle existe
        if (employe.getLigue() != null) {
            employe.getLigue().removeEmploye(employe); 
        }
        // Supprime l'employé de la BD
        passerelle.delete(employe);
    }
    
    /**
     * Initialise l'employé root. Le crée s'il n'existe pas dans la base de données.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     */
    void initialiserRoot() throws SauvegardeImpossible {
        // Cherche le root par son mail/nom connu
        Employe loadedRoot = getEmploye("root"); // Assumons que le mail du root est "root"
        if (loadedRoot == null) {
            loadedRoot = passerelle.getEmployeByNom("root"); // Tente de le charger directement de la BD si pas déjà en mémoire
        }

        if (loadedRoot == null) {
            // Si le root n'existe pas dans la BD, le créer
            // Le constructeur d'Employe gérera l'insertion en BD et l'affectation de l'ID
            root = new Employe(this, null, "root", "root", "root", "toor", LocalDate.now(), null);
            // Marque explicitement le root comme tel après sa création
            // La méthode setEstRoot va persister ce statut
            if (root != null) { // S'assurer que l'objet root a bien été créé
                root.setEstRoot(true); 
            }
            System.out.println("Utilisateur 'root' créé dans la base de données.");
        } else {
            root = loadedRoot;
            // Assurez-vous que le statut root est bien défini pour l'objet en mémoire
            // La méthode setEstRoot va persister ce statut
            if (root != null) { // S'assurer que l'objet root a bien été chargé
                root.setEstRoot(true); 
            }
            System.out.println("Utilisateur 'root' chargé depuis la base de données.");
        }
    }

    /**
     * Retourne l'employé root.
     * @return L'employé root.
     */
    public Employe getRoot() {
        return root;
    }

    /**
     * Retourne l'instance de la passerelle de persistance.
     * @return La passerelle.
     */
    public Passerelle getPasserelle() {
        return passerelle;
    }

    /**
     * Ajoute une ligue existante à la collection interne (utilisé lors du chargement depuis la BD).
     * @param ligue La ligue à ajouter.
     */
    public void add(Ligue ligue) {
        ligues.add(ligue);
    }

    /**
     * Définit l'employé root. Utilisé principalement lors du chargement depuis la base de données.
     * @param root L'employé à définir comme root.
     */
	public void setRoot(Employe root) {
		this.root = root;
	}

	/**
	 * Ajoute une ligue à la collection interne.
	 * @param ligue La ligue à ajouter.
	 */
	public void addLigue(Ligue ligue) {
	    if (ligues == null) {
	        ligues = new TreeSet<>(); // Initialise si ce n'est pas déjà fait
	    }
	    ligues.add(ligue);
	}

	/**
	 * Supprime un employé. Cette méthode est un alias pour remove(Employe employe).
	 * @param employe L'employé à supprimer.
	 * @throws SauvegardeImpossible Si une erreur de sauvegarde se produit.
	 */
	public void delete(Employe employe) throws SauvegardeImpossible {
		remove(employe);
	}
	
}
