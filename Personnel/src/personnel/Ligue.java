package personnel;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

public class Ligue implements Serializable, Comparable<Ligue> {
    private static final long serialVersionUID = 1L;
    private int id = -1; // -1 pour un nouvel objet qui n'a pas encore d'ID de base de données
    private String nom;
    private SortedSet<Employe> employes; // Collection des employés de cette ligue
    private Employe administrateur; // L'employé administrateur de cette ligue
    private GestionPersonnel gestionPersonnel; // Référence à l'instance de GestionPersonnel

    /**
     * Constructeur pour la création d'une NOUVELLE ligue (qui sera insérée en base de données).
     * L'ID sera généré par la base de données.
     * @param gestionPersonnel L'instance de GestionPersonnel.
     * @param nom Le nom de la nouvelle ligue.
     * @throws SauvegardeImpossible Si une erreur se produit lors de l'insertion.
     */
    public Ligue(GestionPersonnel gestionPersonnel, String nom) throws SauvegardeImpossible {
        this(gestionPersonnel, -1, nom); // Appelle le constructeur interne avec ID par défaut
        this.id = gestionPersonnel.insert(this); // Insère la nouvelle ligue en BD et récupère l'ID
    }

    /**
     * Constructeur pour le chargement d'une ligue existante depuis la base de données.
     * @param gestionPersonnel L'instance de GestionPersonnel.
     * @param id L'ID de la ligue dans la base de données.
     * @param nom Le nom de la ligue.
     */
    public Ligue(GestionPersonnel gestionPersonnel, int id, String nom) {
        this.nom = nom;
        this.employes = new TreeSet<>(); // Initialise la collection d'employés
        this.gestionPersonnel = gestionPersonnel;
        this.administrateur = null; // L'administrateur sera défini après le chargement des employés
        this.id = id; // L'ID est fourni car la ligue existe déjà en BD
    }

    // --- Getters ---
    public int getId() {
        return id;
    }

    /**
     * Définit l'ID de la ligue. Utilisé en interne par la passerelle JDBC lors du chargement initial.
     * @param id Le nouvel ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    /**
     * Retourne une vue non modifiable de la collection des employés de cette ligue.
     * @return Un SortedSet non modifiable d'Employe.
     */
    public SortedSet<Employe> getEmployes() {
        return Collections.unmodifiableSortedSet(employes); // Retourne une vue non modifiable
    }

    public Employe getAdministrateur() {
        return administrateur;
    }

    // --- Setters ---
    public void setNom(String nom) throws SauvegardeImpossible {
        this.nom = nom;
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    /**
     * Définit l'administrateur de cette ligue.
     * L'administrateur doit être un employé de cette ligue ou l'utilisateur 'root'.
     * @param administrateur L'employé à désigner comme administrateur. Peut être null.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     * @throws IllegalArgumentException Si l'employé n'appartient pas à cette ligue et n'est pas 'root'.
     */
    public void setAdministrateur(Employe administrateur) throws SauvegardeImpossible {
        if (administrateur != null) {
            // L'administrateur doit être root ou faire partie de cette ligue.
            // On peut assouplir pour root car il est "super-admin".
            if (!administrateur.estRoot() && (administrateur.getLigue() == null || !administrateur.getLigue().equals(this))) {
                throw new IllegalArgumentException("L'administrateur doit être un employé de cette ligue ou le root.");
            }
        }
        this.administrateur = administrateur;
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    // --- Méthodes métier ---

    /**
     * Ajoute un employé à la collection interne de la ligue.
     * Cette méthode ne déclenche pas directement l'insertion en base de données,
     * l'insertion est gérée par le constructeur de l'Employe.
     * @param employe L'employé à ajouter.
     */
    public void addEmploye(Employe employe) {
        if (this.employes == null) {
            this.employes = new TreeSet<>();
        }
        this.employes.add(employe);
    }

    /**
     * Retire un employé de la collection interne de la ligue.
     * @param employe L'employé à retirer.
     */
    public void removeEmploye(Employe employe) {
        if (this.employes != null) {
            this.employes.remove(employe);
        }
    }

    /**
     * Supprime cette ligue de l'application et de la base de données.
     * Supprime également tous les employés associés à cette ligue.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la suppression.
     * @throws IllegalArgumentException Si l'on tente de supprimer la ligue associée à l'utilisateur 'root'.
     */
    public void remove() throws SauvegardeImpossible {
        // Empêche la suppression de la ligue si elle est associée à l'utilisateur 'root'
        // (Bien que 'root' n'ait pas de ligue assignée directement, cette vérification est une sécurité)
        if (gestionPersonnel.getRoot() != null && gestionPersonnel.getRoot().getLigue() != null && equals(gestionPersonnel.getRoot().getLigue())) {
            throw new IllegalArgumentException("La ligue du 'root' ne peut pas être supprimée directement.");
        }
        // Pour chaque employé de cette ligue, le supprimer (ce qui supprime aussi de la BD)
        // Crée une copie pour éviter ConcurrentModificationException
        for (Employe employe : new TreeSet<>(employes)) {
            employe.remove(); // Ceci appellera gestionPersonnel.delete(employe)
        }
        employes.clear(); // Vide la collection locale après suppression

        // La suppression de la ligue dans la base de données via JDBC.delete(Ligue)
        // gérera la mise à NULL de administrateur_id pour cette ligue.
        gestionPersonnel.remove(this); // Supprime la ligue de la collection de GestionPersonnel et de la BD
    }

    @Override
    public int compareTo(Ligue autre) {
        return getNom().compareTo(autre.getNom());
    }

    @Override
    public String toString() {
        return nom + " (id: " + id + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ligue ligue = (Ligue) o;
        // L'égalité est basée sur l'ID car il est unique pour chaque ligue
        return id == ligue.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Ajoute un employé à la ligue. Cette méthode est utilisée par LigueConsole.
     * Elle délègue la création de l'employé à GestionPersonnel.
     * @param nom Le nom de l'employé.
     * @param prenom Le prénom de l'employé.
     * @param mail L'adresse mail de l'employé.
     * @param password Le mot de passe de l'employé.
     * @param dateArrivee La date d'arrivée.
     * @param dateDepart La date de départ.
     * @throws SauvegardeImpossible Si une erreur de sauvegarde se produit.
     * @throws IllegalArgumentException Si les données sont invalides.
     */
	public void addEmploye(String nom, String prenom, String mail, String password, LocalDate dateArrivee,
			LocalDate dateDepart) throws SauvegardeImpossible, IllegalArgumentException {
		gestionPersonnel.addEmploye(this, nom, prenom, mail, password, dateArrivee, dateDepart);
	}
}
