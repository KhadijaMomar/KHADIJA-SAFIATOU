package personnel;

import java.io.Serializable;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class GestionPersonnel implements Serializable {
    private static final long serialVersionUID = -105283113987886425L;
    private static GestionPersonnel gestionPersonnel = null;
    private SortedSet<Ligue> ligues;
    private Employe root;

    public final static int SERIALIZATION = 1, JDBC = 2;
    public final static int TYPE_PASSERELLE = JDBC;

    private static Passerelle passerelle;

    public static GestionPersonnel getGestionPersonnel() {
        if (gestionPersonnel == null) {
            gestionPersonnel = new GestionPersonnel();
            try { // Capture de l'exception pour l'initialisation du root
                gestionPersonnel.initialiserRoot();
            } catch (SauvegardeImpossible e) {
                // Cette exception devrait être gérée au niveau de l'application ou loggée
                // Ici, juste la remonter au niveau supérieur
                throw new RuntimeException("Impossible d'initialiser la gestion du personnel: " + e.getMessage(), e);
            }
            // Le chargement via passerelle.getGestionPersonnel() devrait déjà gérer les exceptions de persistence.
            // Si c'est une nouvelle instance, elle n'aura pas été sauvegardée avant.
            // On s'assure que l'instance de GestionPersonnel est chargée depuis la passerelle
            // si elle existe déjà dans la persistance.
            GestionPersonnel loadedGestionPersonnel = passerelle.getGestionPersonnel();
            if (loadedGestionPersonnel != null) {
                gestionPersonnel = loadedGestionPersonnel;
            }
        }
        return gestionPersonnel;
    }

    private GestionPersonnel() {
        if (gestionPersonnel != null) {
            throw new RuntimeException("Vous ne pouvez créer qu'une seule instance de cet objet.");
        }
        ligues = new TreeSet<>();
        passerelle = TYPE_PASSERELLE == JDBC ? new jdbc.JDBC(this) : new serialisation.Serialization();
    }

    // La méthode initialiserRoot n'attrape plus SauvegardeImpossible, elle la propage.
    private void initialiserRoot() throws SauvegardeImpossible {
        creerRootSiInexistant();
    }

    public Ligue getLigue(int id) {
        for (Ligue ligue : ligues) {
            if (ligue.getId() == id) {
                return ligue;
            }
        }
        return null;
    }

    public void update(Employe employe) throws SauvegardeImpossible {
        passerelle.update(employe);
    }

    public void sauvegarder() throws SauvegardeImpossible {
        passerelle.sauvegarderGestionPersonnel(this);
    }

    public Ligue getLigue(Employe administrateur) {
        if (administrateur.estAdmin(administrateur.getLigue())) {
            return administrateur.getLigue();
        } else {
            return null;
        }
    }

    public SortedSet<Ligue> getLigues() {
        return Collections.unmodifiableSortedSet(ligues);
    }

    public Ligue addLigue(String nom) throws SauvegardeImpossible {
        Ligue ligue = new Ligue(this, nom);
        ligues.add(ligue);
        return ligue;
    }

    public Ligue addLigue(int id, String nom) {
        Ligue ligue = new Ligue(this, id, nom);
        ligues.add(ligue);
        return ligue;
    }

    void remove(Ligue ligue) {
        ligues.remove(ligue);
    }

    int insert(Ligue ligue) throws SauvegardeImpossible {
        return passerelle.insert(ligue);
    }

    public int insert(Employe employe) throws SauvegardeImpossible {
        return passerelle.insert(employe);
    }

    public Employe getRoot() {
        return root;
    }

    public void creerRootSiInexistant() throws SauvegardeImpossible {
        if (root == null) {
            root = passerelle.getRoot();
            if (root == null) {
                addRoot("root", "toor");
            }
        }
    }

    public void addRoot(String nom, String password) throws SauvegardeImpossible {
        if (root == null) {
            root = new Employe(this, null, nom, "", "", password, null, null);
        }
    }

    public Passerelle getPasserelle() {
        return passerelle;
    }

    public void update(Ligue ligue) throws SauvegardeImpossible {
        passerelle.update(ligue);
    }
}