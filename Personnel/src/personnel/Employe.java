package personnel;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Comparator;

public class Employe implements Serializable, Comparable<Employe> {
    private static final long serialVersionUID = 4795721718037994734L;
    private int id;
    private String nom, prenom, password, mail;
    private Ligue ligue;
    private GestionPersonnel gestionPersonnel;
    private LocalDate dateDepart;
    private LocalDate dateArrivee;

    Employe(GestionPersonnel gestionPersonnel, Ligue ligue, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart) throws SauvegardeImpossible {
        if (ligue == null && gestionPersonnel.getRoot() != null && this != gestionPersonnel.getRoot()) {
            throw new IllegalArgumentException("L'employé doit être associé à une ligue, sauf s'il est root.");
        }
        this.gestionPersonnel = gestionPersonnel;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.mail = mail;
        this.ligue = ligue;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.id = -1;
        this.id = gestionPersonnel.insert(this);
    }

    Employe(GestionPersonnel gestionPersonnel, int id, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart, Ligue ligue) {
        this.gestionPersonnel = gestionPersonnel;
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.password = password;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.ligue = ligue;
    }

    public static Employe createEmploye(GestionPersonnel gestionPersonnel, Ligue ligue, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart) throws SauvegardeImpossible {
        return new Employe(gestionPersonnel, ligue, nom, prenom, mail, password, dateArrivee, dateDepart);
    }

    public static Employe createEmployeWithId(GestionPersonnel gestionPersonnel, int id, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart, Ligue ligue) {
        return new Employe(gestionPersonnel, id, nom, prenom, mail, password, dateArrivee, dateDepart, ligue);
    }

    public boolean estAdmin(Ligue ligue) {
        return ligue.getAdministrateur() == this;
    }

    public boolean estRoot() {
        return gestionPersonnel.getRoot() == this;
    }

    public String getNom() {
        return nom;
    }

    public Ligue getLigue() {
        return ligue;
    }

    public void setNom(String nom) throws SauvegardeImpossible { // Propagande l'exception
        this.nom = nom;
        gestionPersonnel.update(this);
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) throws SauvegardeImpossible { // Propagande l'exception
        this.prenom = prenom;
        gestionPersonnel.update(this);
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setMail(String mail) throws SauvegardeImpossible { // Propagande l'exception
        this.mail = mail;
        gestionPersonnel.update(this);
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void setPassword(String password) throws SauvegardeImpossible { // Propagande l'exception
        this.password = password;
        gestionPersonnel.update(this);
    }

    public LocalDate getDateArrivee() {
        return dateArrivee;
    }

    public void setDateArrivee(LocalDate dateArrivee) throws DateIncoherenteException, SauvegardeImpossible { // Propagande les exceptions
        if (dateArrivee != null && dateDepart != null && dateDepart.isBefore(dateArrivee)) {
            throw new DateIncoherenteException("La date de départ ne peut pas être avant la date d'arrivée.");
        }
        this.dateArrivee = dateArrivee;
        gestionPersonnel.update(this);
    }

    public LocalDate getDateDepart() {
        return dateDepart;
    }

    public void setDateDepart(LocalDate dateDepart) throws DateIncoherenteException, SauvegardeImpossible { // Propagande les exceptions
        if (dateDepart != null && dateArrivee != null && dateDepart.isBefore(dateArrivee)) {
            throw new DateIncoherenteException("La date de départ ne peut pas être avant la date d'arrivée.");
        }
        this.dateDepart = dateDepart;
        gestionPersonnel.update(this);
    }

    public void remove() throws ImpossibleDeSupprimerRoot, SauvegardeImpossible { // Propagande les exceptions
        Employe root = gestionPersonnel.getRoot();
        if (this == root) {
            throw new ImpossibleDeSupprimerRoot();
        }

        gestionPersonnel.getPasserelle().delete(this); // L'exception SauvegardeImpossible est déjà propagée par delete

        if (estAdmin(getLigue()))
            getLigue().setAdministrateur(root);
        getLigue().remove(this);
    }

    @Override
    public int compareTo(Employe autre) {
        int cmp = getNom().compareTo(autre.getNom());
        if (cmp != 0)
            return cmp;
        return getPrenom().compareTo(autre.getPrenom());
    }

    @Override
    public String toString() {
        String res = nom + " " + prenom + " " + mail + " (";
        if (estRoot())
            res += "super-utilisateur";
        else
            res += ligue.toString();
        return res + ")" + " " + dateArrivee + " " + dateDepart;
    }
}