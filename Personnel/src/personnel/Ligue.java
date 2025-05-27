package personnel;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

public class Ligue implements Serializable, Comparable<Ligue> {
    private static final long serialVersionUID = 1L;
    private int id = -1;
    private String nom;
    private SortedSet<Employe> employes;
    private Employe administrateur;
    private GestionPersonnel gestionPersonnel;

    Ligue(GestionPersonnel gestionPersonnel, String nom) throws SauvegardeImpossible {
        this(gestionPersonnel, -1, nom);
        this.id = gestionPersonnel.insert(this);
    }

    Ligue(GestionPersonnel gestionPersonnel, int id, String nom) {
        this.nom = nom;
        employes = new TreeSet<>();
        this.gestionPersonnel = gestionPersonnel;
        administrateur = gestionPersonnel.getRoot();
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public int getId() {
        return id;
    }

    public void setNom(String nom) throws SauvegardeImpossible, IllegalArgumentException { // Ajout de IllegalArgumentException
        if (nom == null || nom.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de la ligue ne peut pas être vide."); // Lance une exception
        }
        this.nom = nom;
        gestionPersonnel.update(this);
    }

    public Employe getAdministrateur() {
        return administrateur;
    }

    public void setAdministrateur(Employe administrateur) throws IllegalArgumentException, SauvegardeImpossible { // Ajout d'exceptions
        Employe root = gestionPersonnel.getRoot();
        if (administrateur != root && administrateur.getLigue() != this) {
            throw new IllegalArgumentException("L'employé sélectionné n'a pas les droits suffisants pour être administrateur."); // Lance une exception
        }
        this.administrateur = administrateur;
        gestionPersonnel.update(this);
    }

    public SortedSet<Employe> getEmployes() {
        return Collections.unmodifiableSortedSet(employes);
    }

    public Employe addEmploye(String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart) throws SauvegardeImpossible {
        Employe employe = new Employe(this.gestionPersonnel, this, nom, prenom, mail, password, dateArrivee, dateDepart);
        employes.add(employe);
        return employe;
    }

    public Employe addEmploye(String nom, String prenom, String mail, String password, LocalDate dateArrivee) throws SauvegardeImpossible {
        return addEmploye(nom, prenom, mail, password, dateArrivee, null);
    }

    public void addEmploye(Employe employe) {
        employes.add(employe);
    }

    void remove(Employe employe) {
        employes.remove(employe);
    }

    public void remove() throws SauvegardeImpossible, ImpossibleDeSupprimerRoot { // Propagande les exceptions
        String nomLigue = this.nom;

        for (Employe employe : new TreeSet<>(employes)) {
            employe.remove(); // Supprime chaque employé (peut lancer ImpossibleDeSupprimerRoot)
        }

        gestionPersonnel.getPasserelle().delete(this); // L'exception SauvegardeImpossible est déjà propagée par delete

        gestionPersonnel.remove(this);
    }

    @Override
    public int compareTo(Ligue autre) {
        return getNom().compareTo(autre.getNom());
    }

    @Override
    public String toString() {
        return nom;
    }
}