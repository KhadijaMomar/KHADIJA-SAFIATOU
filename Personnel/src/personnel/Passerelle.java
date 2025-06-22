// Fichier: personnel/Passerelle.java
package personnel;

import java.time.LocalDate;
import java.util.SortedSet;

/**
 * Interface définissant les opérations de persistance pour la gestion du personnel.
 * Cette interface permet d'abstraire la couche de stockage (par exemple, sérialisation, JDBC).
 */
public interface Passerelle {
    // Méthodes d'insertion, mise à jour, suppression

    /**
     * Insère une nouvelle ligue dans le système de persistance.
     * @param ligue La ligue à insérer.
     * @return L'ID généré pour la ligue insérée.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     */
    int insert(Ligue ligue) throws SauvegardeImpossible;

    /**
     * Insère un nouvel employé dans le système de persistance.
     * @param employe L'employé à insérer.
     * @return L'ID généré pour l'employé inséré.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     */
    int insert(Employe employe) throws SauvegardeImpossible;

    /**
     * Met à jour les informations d'une ligue dans le système de persistance.
     * @param ligue La ligue à mettre à jour.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     */
    void update(Ligue ligue) throws SauvegardeImpossible;

    /**
     * Met à jour les informations d'un employé dans le système de persistance.
     * @param employe L'employé à mettre à jour.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     */
    void update(Employe employe) throws SauvegardeImpossible;

    /**
     * Supprime une ligue du système de persistance.
     * @param ligue La ligue à supprimer.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     */
    void delete(Ligue ligue) throws SauvegardeImpossible;

    /**
     * Supprime un employé du système de persistance.
     * @param employe L'employé à supprimer.
     * @throws SauvegardeImpossible Si une erreur se produit lors de la sauvegarde.
     */
    void delete(Employe employe) throws SauvegardeImpossible;

    /**
     * Récupère toutes les ligues et leurs employés depuis le système de persistance.
     * Construit une instance de GestionPersonnel avec les données chargées.
     * @return L'instance de GestionPersonnel chargée.
     * @throws SauvegardeImpossible Si une erreur se produit lors de l'accès aux données.
     */
    GestionPersonnel getGestionPersonnel() throws SauvegardeImpossible;

    /**
     * Récupère un employé par son nom.
     * @param nom Le nom de l'employé.
     * @return L'employé correspondant, ou null si non trouvé.
     * @throws SauvegardeImpossible Si une erreur se produit lors de l'accès aux données.
     */
    Employe getEmployeByNom(String nom) throws SauvegardeImpossible;

    /**
     * Récupère un employé par son adresse mail.
     * @param mail L'adresse mail de l'employé.
     * @return L'employé correspondant, ou null si non trouvé.
     * @throws SauvegardeImpossible Si une erreur se produit lors de l'accès aux données.
     */
    Employe getEmployeByMail(String mail) throws SauvegardeImpossible;

    /**
     * Récupère un employé par son ID.
     * @param id L'ID de l'employé.
     * @return L'employé correspondant, ou null si non trouvé.
     * @throws SauvegardeImpossible Si une erreur se produit lors de l'accès aux données.
     */
    Employe getEmploye(int id) throws SauvegardeImpossible; 

    /**
     * Ferme les ressources de la passerelle (par exemple, la connexion à la base de données).
     * @throws SauvegardeImpossible Si une erreur SQL se produit lors de la fermeture de la connexion.
     */
    void close() throws SauvegardeImpossible;

    /**
     * Sauvegarde l'état complet de la gestion du personnel.
     * Pour JDBC, les modifications sont persistées directement, donc cette méthode ferme la connexion.
     * @param gestionPersonnel L'instance de GestionPersonnel à sauvegarder.
     * @throws SauvegardeImpossible Si une erreur de sauvegarde se produit.
     */
    void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible;

	/**
	 * Vérifie si un utilisateur existe par son nom d'utilisateur (mail ou nom).
	 * @param nomUtilisateur Le nom d'utilisateur ou l'adresse mail à vérifier.
	 * @return true si l'utilisateur existe, false sinon.
	 * @throws SauvegardeImpossible Si une erreur de sauvegarde se produit.
	 */
	boolean utilisateurExiste(String nomUtilisateur) throws SauvegardeImpossible;
	
	 /**
     * Récupère l'employé root (administrateur principal).
     * @return L'employé root.
     */
    Employe getRoot();

    /**
     * Hache un mot de passe .
     * @param password Le mot de passe en clair à hacher.
     * @return Le mot de passe haché.
     * @throws SauvegardeImpossible Si une erreur se produit lors du hachage.
     */
    String hashPassword(String password) throws SauvegardeImpossible; 
}