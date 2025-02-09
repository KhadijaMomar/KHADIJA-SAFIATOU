package personnel;

import java.io.Serializable;
import java.time.LocalDate;
import personnel.DateInvalideException;
import personnel.DateIncoherenteException;
import java.time.LocalDate;

/**
 * Employé d'une ligue hébergée par la M2L. Certains peuvent 
 * être administrateurs des employés de leur ligue.
 * Un seul employé, rattaché à aucune ligue, est le root.
 * Il est impossible d'instancier directement un employé, 
 * il faut passer la méthode {@link Ligue#addEmploye addEmploye}.
 */
public class Employe implements Serializable, Comparable<Employe> {
    private static final long serialVersionUID = 4795721718037994734L;
    private String nom, prenom, password, mail;
    private Ligue ligue;
    private GestionPersonnel gestionPersonnel;
    private LocalDate dateDepart; 
    private LocalDate dateArrivee; 
    
    

    /**
     * Constructeur de la classe Employe.
     * 
     * @param gestionPersonnel La gestion du personnel à laquelle l'employé est rattaché.
     * @param ligue La ligue à laquelle l'employé appartient.
     * @param nom Le nom de l'employé.
     * @param prenom Le prénom de l'employé.
     * @param mail L'adresse e-mail de l'employé.
     * @param password Le mot de passe de l'employé.
     * @param dateArrivee La date d'arrivée de l'employé (peut être null).
     * @param dateDepart La date de départ de l'employé (peut être null).
     * @throws DateIncoherenteException Si la date de départ est avant la date d'arrivée (lorsque les deux dates sont non nulles).
     * @throws DateInvalideException Si la date d'arrivée ou de départ est dans le passé.
     */
    Employe(GestionPersonnel gestionPersonnel, Ligue ligue, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart) {
        this.gestionPersonnel = gestionPersonnel;
        this.nom = nom;
        this.prenom = prenom;
        this.password = password;
        this.mail = mail;
        this.ligue = ligue;
        this.dateArrivee = dateArrivee;
        this.dateDepart= dateDepart;


        // Vérification des dates invalides (si elles sont dans le passé)
        if (dateArrivee != null && dateArrivee.isAfter(LocalDate.now())) {
            throw new DateInvalideException("La date d'arrivée ne peut pas être dans le futur.");
        }

        if (dateDepart != null && dateDepart.isBefore(LocalDate.now())) {
            throw new DateInvalideException("La date de départ ne peut pas être dans le passé.");
        }

        // Vérification des dates incohérentes (si la date de départ est avant la date d'arrivée)
        if (dateArrivee != null && dateDepart != null && dateDepart.isBefore(dateArrivee)) {
            throw new DateIncoherenteException("La date de départ ne peut pas être avant la date d'arrivée de l'employe.");
        }

        this.dateArrivee = dateArrivee; 
        this.dateDepart = dateDepart; 
    }

    /**
     * Retourne vrai si l'employé est administrateur de la ligue passée en paramètre.
     * 
     * @param ligue La ligue pour laquelle on souhaite vérifier si l'employé est administrateur.
     * @return vrai si l'employé est administrateur de la ligue, faux sinon.
     */
    public boolean estAdmin(Ligue ligue) {
        return ligue.getAdministrateur() == this;
    }

    /**
     * Retourne vrai si l'employé est le root (super-utilisateur).
     * 
     * @return vrai si l'employé est le root, faux sinon.
     */
    public boolean estRoot() {
        return gestionPersonnel.getRoot() == this;
    }

    /**
     * Retourne le nom de l'employé.
     * 
     * @return Le nom de l'employé.
     */
    public String getNom() {
        return nom;
    }


	/**
	 * Retourne la ligue à laquelle l'employé est affecté.
	 * @return la ligue à laquelle l'employé est affecté.
	 */
	
	public Ligue getLigue()
	{
		return ligue;
	}

    /**
     * Modifie le nom de l'employé.
     * 
     * @param nom Le nouveau nom de l'employé.
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * Retourne le prénom de l'employé.
     * 
     * @return Le prénom de l'employé.
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * Modifie le prénom de l'employé.
     * 
     * @param prenom Le nouveau prénom de l'employé.
     */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    /**
     * Retourne l'adresse e-mail de l'employé.
     * 
     * @return L'adresse e-mail de l'employé.
     */
    public String getMail() {
        return mail;
    }

    /**
     * Modifie l'adresse e-mail de l'employé.
     * 
     * @param mail La nouvelle adresse e-mail de l'employé.
     */
    public void setMail(String mail) {
        this.mail = mail;
    }


    /**
     * Vérifie si le mot de passe passé en paramètre correspond à celui de l'employé.
     * 
     * @param password Le mot de passe à vérifier.
     * @return vrai si le mot de passe correspond, faux sinon.
     */
    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    /**
     * Modifie le mot de passe de l'employé.
     * 
     * @param password Le nouveau mot de passe de l'employé.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Retourne la ligue à laquelle l'employé est rattaché.
     * 
     * @return La ligue de l'employé.
     */
  

    /**
     * Retourne la date d'arrivée de l'employé.
     * 
     * @return La date d'arrivée de l'employé (peut être null).
     */
    public LocalDate getDateArrivee() {
        return dateArrivee;
    }

    /**
     * Modifie la date d'arrivée de l'employé.
     * 
     * @param dateArrivee La nouvelle date d'arrivée de l'employé (peut être null).
     * @throws DateIncoherenteException Si la date de départ est déjà définie et est avant la nouvelle date d'arrivée.
     * @throws DateInvalideException Si la date d'arrivée est dans le passé.
     */
    public void setDateArrivee(LocalDate dateArrivee) {
        if (dateArrivee != null && dateArrivee.isBefore(LocalDate.now())) {
            throw new DateInvalideException("La date d'arrivée ne peut pas être dans le passé.");
        }

        if (dateArrivee != null && dateDepart != null && dateDepart.isBefore(dateArrivee)) {
            throw new DateIncoherenteException("La date de départ ne peut pas être avant la date d'arrivée.");
        }

        this.dateArrivee = dateArrivee;
    }

    /**
     * Retourne la date de départ de l'employé.
     * 
     * @return La date de départ de l'employé (peut être null).
     */
    public LocalDate getDateDepart() {
        return dateDepart;
    }

    /**
     * Modifie la date de départ de l'employé.
     * 
     * @param dateDepart La nouvelle date de départ de l'employé (peut être null).
     * @throws DateIncoherenteException Si la date de départ est avant la date d'arrivée.
     * @throws DateInvalideException Si la date de départ est dans le passé.
     */
    public void setDateDepart(LocalDate dateDepart) {
        if (dateDepart != null && dateDepart.isBefore(LocalDate.now())) {
            throw new DateInvalideException("La date de départ ne peut pas être dans le passé.");
        }

        if (dateDepart != null && dateArrivee != null && dateDepart.isBefore(dateArrivee)) {
            throw new DateIncoherenteException("La date de départ ne peut pas être avant la date d'arrivée.");
        }

        this.dateDepart = dateDepart;
    }

    /**
     * Supprime l'employé. Si l'employé est un administrateur, le root récupère les droits d'administration sur sa ligue.
     * 
     * @throws ImpossibleDeSupprimerRoot Si l'employé est le root.
     */
    public void remove() {
        Employe root = gestionPersonnel.getRoot();
        if (this != root) {
            if (estAdmin(getLigue()))
                getLigue().setAdministrateur(root);
            getLigue().remove(this);
        } else
            throw new ImpossibleDeSupprimerRoot();
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



//@Override
//public int compareTo(Employe o) {
//	// TODO Auto-generated method stub
//	return 0;
//}
}







