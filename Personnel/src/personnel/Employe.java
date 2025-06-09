package personnel;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class Employe implements Serializable, Comparable<Employe> {
    private static final long serialVersionUID = 4795721718037994734L;
    private int id;
    private String nom, prenom, password, mail;
    private Ligue ligue;
    private GestionPersonnel gestionPersonnel;
    private LocalDate dateDepart;
    private LocalDate dateArrivee;
    // Ajout d'un champ pour stocker le statut root en mémoire.
    // La persistance est gérée par la colonne 'est_root' dans la BD via JDBC.
    private boolean estRootStatus; 

    /**
     * Constructeur pour la création d'un NOUVEL employé (qui sera inséré en base de données).
     * L'ID sera généré par la base de données.
     * @param gestionPersonnel L'instance de GestionPersonnel.
     * @param ligue La ligue à laquelle l'employé appartient. Peut être null pour l'utilisateur 'root'.
     * @param nom Le nom de l'employé.
     * @param prenom Le prénom de l'employé.
     * @param mail L'adresse mail de l'employé (doit être unique).
     * @param password Le mot de passe de l'employé.
     * @param dateArrivee La date d'arrivée de l'employé.
     * @param dateDepart La date de départ de l'employé (peut être null).
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     * @throws IllegalArgumentException Si les données fournies sont invalides (ex: mot de passe vide, ligue manquante).
     */
    public Employe(GestionPersonnel gestionPersonnel, Ligue ligue, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart) throws SauvegardeImpossible {
        // Validation que le mot de passe n'est ni null ni vide (trim pour enlever les espaces)
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }
        // La règle pour la ligue s'applique, sauf pour le root.
        if (ligue == null && !nom.equals("root")) { // 'root' est un cas spécial sans ligue assignée directement
             throw new IllegalArgumentException("Un employé doit être associé à une ligue.");
        }

        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.password = password;
        this.ligue = ligue;
        this.gestionPersonnel = gestionPersonnel;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.estRootStatus = nom.equals("root"); // Définit le statut root lors de la création
        // Insère le nouvel employé dans la base de données et récupère son ID généré
        this.id = gestionPersonnel.insert(this); 
        if (ligue != null) {
            ligue.addEmploye(this); // Ajoute à la collection d'employés de la ligue
        }
    }

    /**
     * Constructeur pour le chargement d'un employé existant depuis la base de données.
     * Il ne déclenche PAS d'insertion dans la base de données.
     * @param gestionPersonnel L'instance de GestionPersonnel.
     * @param id L'ID de l'employé dans la base de données.
     * @param ligue La ligue à laquelle l'employé appartient.
     * @param nom Le nom de l'employé.
     * @param prenom Le prénom de l'employé.
     * @param mail L'adresse mail de l'employé.
     * @param password Le mot de passe de l'employé.
     * @param dateArrivee La date d'arrivée de l'employé.
     * @param dateDepart La date de départ de l'employé.
     */
    public Employe(GestionPersonnel gestionPersonnel, int id, Ligue ligue, String nom, String prenom, String mail, String password, LocalDate dateArrivee, LocalDate dateDepart) {
        this.id = id; // L'ID est directement fourni car l'employé existe déjà
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.password = password;
        this.ligue = ligue;
        this.gestionPersonnel = gestionPersonnel;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        // Le statut root sera défini par la passerelle lors du chargement, via gestionPersonnel.setRoot()
        this.estRootStatus = false; // Initialisé à false, sera mis à jour si c'est le root
        // AUCUN APPEL À gestionPersonnel.insert(this) ici !
        if (ligue != null) {
            ligue.addEmploye(this); // Ajoute à la collection d'employés de la ligue
        }
    }


    // --- Getters ---
    public int getId() {
        return id;
    }

    /**
     * Définit l'ID de l'employé. Utilisé en interne par la passerelle JDBC lors du chargement initial.
     * @param id Le nouvel ID.
     */
    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getMail() {
        return mail;
    }

    public String getPassword() {
        return password;
    }

    public Ligue getLigue() {
        return ligue;
    }

    public LocalDate getDateArrivee() {
        return dateArrivee;
    }

    public LocalDate getDateDepart() {
        return dateDepart;
    }
    
    /**
     * Retourne l'instance de GestionPersonnel associée à cet employé.
     * @return L'instance de GestionPersonnel.
     */
    public GestionPersonnel getGestionPersonnel() {
        return gestionPersonnel;
    }

    // --- Setters ---
    public void setNom(String nom) throws SauvegardeImpossible {
        this.nom = nom;
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    public void setPrenom(String prenom) throws SauvegardeImpossible {
        this.prenom = prenom;
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    public void setMail(String mail) throws SauvegardeImpossible {
        this.mail = mail;
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    public void setPassword(String password) throws SauvegardeImpossible {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide.");
        }
        this.password = password;
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    public void setLigue(Ligue ligue) throws SauvegardeImpossible {
        if (this.ligue != null) {
            this.ligue.removeEmploye(this); // Retire de l'ancienne ligue
        }
        this.ligue = ligue;
        if (this.ligue != null) {
            this.ligue.addEmploye(this); // Ajoute à la nouvelle ligue
        }
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    public void setDateArrivee(LocalDate dateArrivee) throws DateInvalideException, SauvegardeImpossible {
        if (dateArrivee != null && dateArrivee.isAfter(LocalDate.now())) {
            throw new DateInvalideException("La date d'arrivée ne peut pas être dans le futur.");
        }
        if (this.dateDepart != null && dateArrivee != null && dateArrivee.isAfter(this.dateDepart)) {
            throw new DateInvalideException("La date d'arrivée ne peut pas être après la date de départ.");
        }
        this.dateArrivee = dateArrivee;
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    public void setDateDepart(LocalDate dateDepart) throws DateInvalideException, SauvegardeImpossible {
        if (dateDepart != null && this.dateArrivee != null && dateDepart.isBefore(this.dateArrivee)) {
            throw new DateIncoherenteException("La date de départ ne peut pas être avant la date d'arrivée.");
        }
        this.dateDepart = dateDepart;
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    /**
     * Définit le statut 'root' de cet employé.
     * @param isRoot true si l'employé doit être root, false sinon.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     */
    public void setEstRoot(boolean isRoot) throws SauvegardeImpossible {
        this.estRootStatus = isRoot;
        gestionPersonnel.update(this); // Persiste la modification en base de données
    }

    // --- Méthodes métier ---

    /**
     * Supprime cet employé de l'application et de la base de données.
     * Gère également la désignation de l'administrateur de ligue si cet employé était un administrateur.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la suppression.
     * @throws ImpossibleDeSupprimerRoot Si l'on tente de supprimer l'utilisateur 'root'.
     */
    public void remove() throws SauvegardeImpossible {
        if (estRoot()) { // Utilise la méthode estRoot() pour vérifier
            throw new ImpossibleDeSupprimerRoot("L'employé 'root' ne peut pas être supprimé.");
        }
        if (ligue != null) {
            // Si cet employé est administrateur de sa ligue, l'admin doit être remplacé (mis à null)
            if (equals(ligue.getAdministrateur())) {
                ligue.setAdministrateur(null); // L'administrateur est mis à null pour le moment
            }
            ligue.removeEmploye(this); // Retire l'employé de la ligue
        }

        // Vérifie si cet employé est administrateur d'une autre ligue et supprime cette affiliation
        // (ceci est géré par la clé étrangère ON DELETE SET NULL dans la BD pour administrateur_id)
        for (Ligue l : gestionPersonnel.getLigues()) {
            if (l.getAdministrateur() != null && l.getAdministrateur().equals(this)) {
                l.setAdministrateur(null); // Retire l'admin de cette ligue aussi en mémoire et persiste
            }
        }

        gestionPersonnel.remove(this); // Supprime l'employé de la base de données et des collections
    }

    /**
     * Vérifie si cet employé est l'utilisateur 'root'.
     * @return true si l'employé est 'root', false sinon.
     */
    public boolean estRoot() {
        // Le statut root est maintenant stocké et géré directement par l'objet Employe.
        // La méthode initialiserRoot() de GestionPersonnel s'assure que le root est correctement défini
        // et son statut 'estRootStatus' est mis à jour lors du chargement.
        return this.estRootStatus;
    }

    /**
     * Vérifie si cet employé est administrateur d'une ligue donnée.
     * @param ligue La ligue à vérifier.
     * @return true si l'employé est administrateur de la ligue ou est 'root', false sinon.
     */
    public boolean estAdmin(Ligue ligue) {
        if (estRoot()) {
            return true; // Le root est super-administrateur et a tous les droits
        }
        // Sinon, il est admin s'il est l'administrateur désigné de la ligue.
        return ligue != null && ligue.getAdministrateur() != null && ligue.getAdministrateur().equals(this);
    }
    
    /**
     * Vérifie si cet employé est l'administrateur de SA propre ligue.
     * @return true si l'employé est administrateur de sa ligue, false sinon.
     */
    public boolean estAdministrateurLigue() {
        return this.ligue != null && this.ligue.getAdministrateur() != null && this.ligue.getAdministrateur().equals(this);
    }


    // --- Surcharges de Object pour une comparaison correcte ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Employe employe = (Employe) o;
        // Si l'ID est valide (non -1), on utilise l'ID pour l'égalité
        if (this.id != -1 && employe.id != -1) {
            return this.id == employe.id;
        }
        // Sinon, on utilise l'adresse mail (qui doit être unique si l'ID n'est pas encore défini)
        return Objects.equals(mail, employe.mail);
    }

    @Override
    public int hashCode() {
        // Si l'ID est valide, utilise l'ID pour le hashcode
        return (id != -1) ? Objects.hash(id) : Objects.hash(mail);
    }

    // --- Autres méthodes (toString, compareTo) ---
    @Override
    public int compareTo(Employe autre) {
        int cmp = getNom().compareTo(autre.getNom());
        if (cmp != 0)
            return cmp;
        return getPrenom().compareTo(autre.getPrenom());
    }

    @Override
    public String toString() {
        String res = nom + " " + prenom + " (" + mail + ")";
        if (ligue != null) {
            res += ", Ligue: " + ligue.getNom();
        }
        if (dateArrivee != null) {
            res += ", Arrivée: " + dateArrivee;
        }
        if (dateDepart != null) {
            res += ", Départ: " + dateDepart;
        }
        if (id != -1) {
            res += ", ID: " + id;
        }
        if (estRoot()) {
            res += " (ROOT)";
        } else if (ligue != null && ligue.getAdministrateur() != null && ligue.getAdministrateur().equals(this)) {
            res += " (Admin de " + ligue.getNom() + ")";
        }
        return res;
    }
}
