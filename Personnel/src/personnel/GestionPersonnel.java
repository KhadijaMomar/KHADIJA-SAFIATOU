package personnel;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Gestion du personnel. Un seul objet de cette classe existe.
 * Il n'est pas possible d'instancier directement cette classe,
 * la méthode {@link #getGestionPersonnel getGestionPersonnel}
 * le fait automatiquement et retourne toujours le même objet.
 * Dans le cas où {@link #sauvegarder()} a été appelé lors
 * d'une exécution précédente, c'est l'objet sauvegardé qui est
 * retourné.
 */
public class GestionPersonnel implements Serializable {
    private static final long serialVersionUID = -105283113987886425L;
    private static GestionPersonnel gestionPersonnel = null; // Instance unique de GestionPersonnel
    private SortedSet<Ligue> ligues; // Ensemble des ligues gérées
    private Employe root; // Employé root (super-utilisateur)

    // Constantes pour le type de passerelle (JDBC ou Serialization)
    public final static int SERIALIZATION = 1, JDBC = 2;
    public final static int TYPE_PASSERELLE = JDBC; // Utilisation de JDBC par défaut

    // Passerelle pour la persistance des données
    private static Passerelle passerelle;

    /**
     * Retourne l'unique instance de cette classe.
     * Crée cet objet s'il n'existe déjà.
     * @return l'unique objet de type {@link GestionPersonnel}.
     */
    public static GestionPersonnel getGestionPersonnel() {
        if (gestionPersonnel == null) {
            gestionPersonnel = new GestionPersonnel(); // Crée une nouvelle instance si elle n'existe pas
            gestionPersonnel.initialiserRoot(); // Initialise le root
        }
        return gestionPersonnel;
    }

    /**
     * Constructeur privé pour empêcher l'instanciation directe.
     */
    private GestionPersonnel() {
        if (gestionPersonnel != null) {
            throw new RuntimeException("Vous ne pouvez créer qu'une seule instance de cet objet.");
        }
        ligues = new TreeSet<>(); // Initialise l'ensemble des ligues
        passerelle = TYPE_PASSERELLE == JDBC ? new jdbc.JDBC(this) : new serialisation.Serialization(); // Initialise la passerelle
    }

    /**
     * Initialise le root en vérifiant s'il existe déjà dans la base de données.
     */
   

   
    private void initialiserRoot() {
        try {
            creerRootSiInexistant(); // Appel de la méthode métier
        } catch (SauvegardeImpossible e) {
            System.err.println("Erreur lors de l'initialisation du root : " + e.getMessage());
        }
    }
   
    
    /**
     * Retourne la ligue correspondant à l'ID spécifié.
     * @param id L'ID de la ligue.
     * @return La ligue correspondante, ou null si aucune ligue n'est trouvée.
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
     * Met à jour les informations d'un employé dans la base de données.
     * @param employe L'employé à mettre à jour.
     * @throws SauvegardeImpossible Si la mise à jour échoue.
     */
    public void update(Employe employe) throws SauvegardeImpossible {
        passerelle.update(employe);
    }

    /**
     * Sauvegarde l'état de la gestion du personnel.
     * @throws SauvegardeImpossible Si la sauvegarde échoue.
     */
    public void sauvegarder() throws SauvegardeImpossible {
        passerelle.sauvegarderGestionPersonnel(this);
    }

    /**
     * Retourne la ligue dont l'administrateur est l'employé passé en paramètre.
     * @param administrateur L'administrateur de la ligue recherchée.
     * @return La ligue administrée par l'employé, ou null s'il n'est pas administrateur.
     */
    public Ligue getLigue(Employe administrateur) {
        if (administrateur.estAdmin(administrateur.getLigue())) {
            return administrateur.getLigue();
        } else {
            return null;
        }
    }

    /**
     * Retourne toutes les ligues enregistrées.
     * @return Un ensemble trié de toutes les ligues.
     */
    public SortedSet<Ligue> getLigues() {
        return Collections.unmodifiableSortedSet(ligues);
    }

    /**
     * Ajoute une nouvelle ligue.
     * @param nom Le nom de la ligue.
     * @return La ligue créée.
     * @throws SauvegardeImpossible Si l'insertion échoue.
     */
    public Ligue addLigue(String nom) throws SauvegardeImpossible {
        Ligue ligue = new Ligue(this, nom);
        ligues.add(ligue);
        return ligue;
    }

    /**
     * Ajoute une ligue avec un ID spécifique.
     * @param id L'ID de la ligue.
     * @param nom Le nom de la ligue.
     * @return La ligue créée.
     */
    public Ligue addLigue(int id, String nom) {
        Ligue ligue = new Ligue(this, id, nom);
        ligues.add(ligue);
        return ligue;
    }

    /**
     * Supprime une ligue.
     * @param ligue La ligue à supprimer.
     */
    void remove(Ligue ligue) {
        ligues.remove(ligue);
    }

    /**
     * Insère une ligue dans la base de données.
     * @param ligue La ligue à insérer.
     * @return L'ID de la ligue insérée.
     * @throws SauvegardeImpossible Si l'insertion échoue.
     */
    int insert(Ligue ligue) throws SauvegardeImpossible {
        return passerelle.insert(ligue);
    }

    /**
     * Insère un employé dans la base de données.
     * @param employe L'employé à insérer.
     * @return L'ID de l'employé inséré.
     * @throws SauvegardeImpossible Si l'insertion échoue.
     */
    public int insert(Employe employe) throws SauvegardeImpossible {
        return passerelle.insert(employe);
    }

    /**
     * Retourne le root (super-utilisateur).
     * @return Le root.
     */
    public Employe getRoot() {
        return root;
    }

    /**
     * Crée le root (super-utilisateur) et l'insère dans la base de données.
     * @param nom Le nom du root.
     * @param password Le mot de passe du root.
     * @throws SauvegardeImpossible Si l'insertion du root échoue.
     */

    
    
    public void creerRootSiInexistant() throws SauvegardeImpossible {
        if (root == null) {
            root = passerelle.getRoot(); // Charge le root depuis la base de données
            if (root == null) {
                // Si le root n'existe pas, créez-le
                addRoot("root", "toor");
            }
        }
    }
    
    public void addRoot(String nom, String password) throws SauvegardeImpossible {
        if (root == null) {
            // Crée un employé sans ligue (ligue = null) et le marque comme root
            root = new Employe(this, null, nom, "", "", password, null, null);
            root.setId(passerelle.insert(root)); // Insère le root dans la base de données
        }
    }
    
  
  
    

    /**
     * Met à jour les informations d'une ligue dans la base de données.
     * @param ligue La ligue à mettre à jour.
     * @throws SauvegardeImpossible Si la mise à jour échoue.
     */
    public void update(Ligue ligue) throws SauvegardeImpossible {
        passerelle.update(ligue);
    }
}