package jdbc;

import personnel.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Implémentation de l'interface Passerelle pour la gestion du personnel
 * en utilisant une base de données JDBC.
 */
public class JDBC implements Passerelle {
    private Connection connection;
    private GestionPersonnel gestionPersonnel;
    private HashMap<Integer, Ligue> liguesLoaded; // Map pour stocker les ligues chargées par ID pour une recherche rapide
    private HashMap<Integer, Employe> employesLoaded; // Map pour stocker les employés chargés par ID

    /**
     * Constructeur de la classe JDBC.
     * Initialise la connexion à la base de données et s'assure que les tables existent et ont le bon schéma.
     * @param gestionPersonnel L'instance de GestionPersonnel à laquelle cette passerelle est associée.
     */
    public JDBC(GestionPersonnel gestionPersonnel) {
        this.gestionPersonnel = gestionPersonnel;
        this.liguesLoaded = new HashMap<>();
        this.employesLoaded = new HashMap<>();
        try {
            // Charge le pilote JDBC
            Class.forName(Credentials.getDriverClassName());
            // Établit la connexion à la base de données
            connection = DriverManager.getConnection(Credentials.getUrl(), Credentials.getUser(), Credentials.getPassword());
            System.out.println("Connexion à la base de données réussie.");
            // Appelle la méthode pour vérifier et créer/mettre à jour les tables et colonnes
            initializeDatabaseSchema(); 
        } catch (ClassNotFoundException e) {
            // Gère l'exception si le pilote JDBC n'est pas trouvé
            throw new RuntimeException("Pilote JDBC introuvable : " + e.getMessage(), e);
        } catch (SQLException e) {
            // Gère les exceptions SQL lors de la connexion
            throw new RuntimeException("Erreur de connexion à la base de données : " + e.getMessage(), e);
        }
    }

    /**
     * Initialise le schéma de la base de données en vérifiant et créant les tables
     * si elles n'existent pas, puis en ajoutant les colonnes manquantes.
     * Cette méthode préserve les données existantes.
     * @throws SQLException Si une erreur SQL se produit.
     */
    private void initializeDatabaseSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            System.out.println("Vérification et mise à jour du schéma de la base de données (préservation des données)...");

            // 1. Création de la table 'ligue' si elle n'existe pas (NOM CORRIGÉ)
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ligue (" +
                                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                                    "nom VARCHAR(255) UNIQUE NOT NULL" +
                                    ")");
            System.out.println("Table 'ligue' vérifiée/créée si nécessaire.");

            // 2. Ajout des colonnes manquantes à la table 'ligue' (NOM CORRIGÉ)
            addColumnIfNotExist(statement, "ligue", "administrateur_id", "INTEGER");

            // 3. Création de la table 'employe' si elle n'existe pas (NOM CORRIGÉ)
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS employe (" +
                                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                                    "nom VARCHAR(255) NOT NULL," +
                                    "prenom VARCHAR(255) NOT NULL," +
                                    "mail VARCHAR(255) UNIQUE NOT NULL," +
                                    "password VARCHAR(255) NOT NULL," +
                                    "ligue_id INTEGER," +
                                    "FOREIGN KEY (ligue_id) REFERENCES ligue(id) ON DELETE SET NULL" + // NOM CORRIGÉ
                                    ")");
            System.out.println("Table 'employe' vérifiée/créée si nécessaire.");

            // 4. Ajout des colonnes manquantes à la table 'employe' (NOM CORRIGÉ)
            addColumnIfNotExist(statement, "employe", "date_arrivee", "DATE");
            addColumnIfNotExist(statement, "employe", "date_depart", "DATE");
            addColumnIfNotExist(statement, "employe", "est_root", "BOOLEAN DEFAULT FALSE");
            
            // 5. Ajout des index uniques si elles n'existent pas (NOMS CORRIGÉS)
            try {
                statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS idx_mail ON employe (mail)");
                statement.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS idx_ligue_nom ON ligue (nom)");
            } catch (SQLException e) {
                // Ignorer si l'index existe déjà (pour les SGBD qui le permettent)
                System.err.println("Erreur lors de la création des index (peut-être déjà existants) : " + e.getMessage());
            }

            System.out.println("Schéma de la base de données vérifié/mis à jour avec succès (les tables existantes et leurs données sont préservées).");
        }
    }

    /**
     * Ajoute une colonne à une table si elle n'existe pas déjà.
     * @param statement Le Statement JDBC.
     * @param tableName Le nom de la table.
     * @param columnName Le nom de la colonne à ajouter.
     * @param columnType Le type de la colonne (ex: "INTEGER", "VARCHAR(255)", "DATE", "BOOLEAN DEFAULT FALSE").
     * @throws SQLException Si une erreur SQL se produit.
     */
    private void addColumnIfNotExist(Statement statement, String tableName, String columnName, String columnType) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            if (!columns.next()) {
                // La colonne n'existe pas, l'ajouter
                String alterSql = "ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType;
                statement.executeUpdate(alterSql);
                System.out.println("Colonne '" + columnName + "' ajoutée à la table '" + tableName + "'.");
            } else {
                System.out.println("La colonne '" + columnName + "' existe déjà dans la table '" + tableName + "'.");
            }
        }
    }


    @Override
    public int insert(Ligue ligue) throws SauvegardeImpossible {
        String sql = "INSERT INTO ligue (nom) VALUES (?)"; // NOM CORRIGÉ
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ligue.getNom());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    liguesLoaded.put(id, ligue); // Ajoute à la map des ligues chargées
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de l'insertion de la ligue : " + e.getMessage(), e);
        }
        return -1; // Ne devrait pas arriver
    }

    @Override
    public int insert(Employe employe) throws SauvegardeImpossible {
        String sql = "INSERT INTO employe (nom, prenom, mail, password, date_arrivee, date_depart, ligue_id, est_root) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"; // NOM CORRIGÉ
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, employe.getNom());
            pstmt.setString(2, employe.getPrenom());
            pstmt.setString(3, employe.getMail());
            pstmt.setString(4, employe.getPassword());
            pstmt.setDate(5, employe.getDateArrivee() != null ? Date.valueOf(employe.getDateArrivee()) : null);
            pstmt.setDate(6, employe.getDateDepart() != null ? Date.valueOf(employe.getDateDepart()) : null);
            pstmt.setObject(7, employe.getLigue() != null ? employe.getLigue().getId() : null, Types.INTEGER);
            pstmt.setBoolean(8, employe.estRoot()); // Sauvegarde le statut root
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    employesLoaded.put(id, employe); // Ajoute à la map des employés chargés
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de l'insertion de l'employé : " + e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public void update(Ligue ligue) throws SauvegardeImpossible {
        String sql = "UPDATE ligue SET nom = ?, administrateur_id = ? WHERE id = ?"; // NOM CORRIGÉ
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ligue.getNom());
            pstmt.setObject(2, ligue.getAdministrateur() != null ? ligue.getAdministrateur().getId() : null, Types.INTEGER);
            pstmt.setInt(3, ligue.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de la mise à jour de la ligue : " + e.getMessage(), e);
        }
    }

    @Override
    public void update(Employe employe) throws SauvegardeImpossible {
        String sql = "UPDATE employe SET nom = ?, prenom = ?, mail = ?, password = ?, date_arrivee = ?, date_depart = ?, ligue_id = ?, est_root = ? WHERE id = ?"; // NOM CORRIGÉ
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, employe.getNom());
            pstmt.setString(2, employe.getPrenom());
            pstmt.setString(3, employe.getMail());
            pstmt.setString(4, employe.getPassword());
            pstmt.setDate(5, employe.getDateArrivee() != null ? Date.valueOf(employe.getDateArrivee()) : null);
            pstmt.setDate(6, employe.getDateDepart() != null ? Date.valueOf(employe.getDateDepart()) : null);
            pstmt.setObject(7, employe.getLigue() != null ? employe.getLigue().getId() : null, Types.INTEGER);
            pstmt.setBoolean(8, employe.estRoot()); // Met à jour le statut root
            pstmt.setInt(9, employe.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de la mise à jour de l'employé : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Ligue ligue) throws SauvegardeImpossible {
        String sql = "DELETE FROM ligue WHERE id = ?"; // NOM CORRIGÉ
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, ligue.getId());
            pstmt.executeUpdate();
            liguesLoaded.remove(ligue.getId()); // Supprime de la map des ligues chargées
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de la suppression de la ligue : " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(Employe employe) throws SauvegardeImpossible {
        String sql = "DELETE FROM employe WHERE id = ?"; // NOM CORRIGÉ
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, employe.getId());
            pstmt.executeUpdate();
            employesLoaded.remove(employe.getId()); // Supprime de la map des employés chargés
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de la suppression de l'employé : " + e.getMessage(), e);
        }
    }

    /**
     * Charge toutes les ligues et leurs employés depuis le système de persistance
     * et les ajoute à l'instance de GestionPersonnel.
     * @throws SauvegardeImpossible Si une erreur se produit lors du chargement.
     */
    @Override
    public void getGestionPersonnel() throws SauvegardeImpossible {
        // Efface les maps pour un rechargement propre
        liguesLoaded.clear();
        employesLoaded.clear();

        // 1. Charger toutes les ligues
        String selectLiguesSql = "SELECT id, nom, administrateur_id FROM ligue"; // NOM CORRIGÉ
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectLiguesSql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                Ligue ligue = new Ligue(gestionPersonnel, id, nom);
                gestionPersonnel.add(ligue); // Ajoute à la collection de GestionPersonnel
                liguesLoaded.put(id, ligue); // Ajoute à la map locale pour référence rapide
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors du chargement des ligues : " + e.getMessage(), e);
        }

        // 2. Charger tous les employés et les associer à leurs ligues
        String selectEmployesSql = "SELECT id, nom, prenom, mail, password, date_arrivee, date_depart, ligue_id, est_root FROM employe"; // NOM CORRIGÉ
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectEmployesSql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String mail = rs.getString("mail");
                String password = rs.getString("password");
                LocalDate dateArrivee = rs.getDate("date_arrivee") != null ? rs.getDate("date_arrivee").toLocalDate() : null;
                LocalDate dateDepart = rs.getDate("date_depart") != null ? rs.getDate("date_depart").toLocalDate() : null;
                int ligueId = rs.getInt("ligue_id");
                boolean estRoot = rs.getBoolean("est_root");

                Ligue ligue = liguesLoaded.get(ligueId); // Récupère la ligue par son ID

                Employe employe = new Employe(gestionPersonnel, id, ligue, nom, prenom, mail, password, dateArrivee, dateDepart);
                if (ligue != null) {
                    ligue.addEmploye(employe); // Ajoute l'employé à sa ligue
                }
                employesLoaded.put(id, employe); // Ajoute à la map locale pour référence rapide

                // Si c'est l'employé root, le définir dans GestionPersonnel
                if (estRoot) {
                    gestionPersonnel.setRoot(employe);
                    employe.setEstRoot(true); // S'assurer que l'objet en mémoire a le bon statut
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors du chargement des employés : " + e.getMessage(), e);
        }

        // 3. Associer les administrateurs aux ligues (fait après le chargement de tous les employés)
        String selectLigueAdminsSql = "SELECT id, administrateur_id FROM ligue WHERE administrateur_id IS NOT NULL"; // NOM CORRIGÉ
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectLigueAdminsSql)) {
            while (rs.next()) {
                int ligueId = rs.getInt("id");
                int adminId = rs.getInt("administrateur_id");
                Ligue ligue = liguesLoaded.get(ligueId);
                Employe administrateur = employesLoaded.get(adminId);
                if (ligue != null && administrateur != null) {
                    ligue.setAdministrateur(administrateur); // Associe l'administrateur à la ligue
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de l'association des administrateurs de ligue : " + e.getMessage(), e);
        }
    }


    @Override
    public Employe getEmployeByNom(String nom) throws SauvegardeImpossible {
        String sql = "SELECT id, nom, prenom, mail, password, date_arrivee, date_depart, ligue_id, est_root FROM employe WHERE nom = ?"; // NOM CORRIGÉ
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, nom);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String prenom = rs.getString("prenom");
                    String mail = rs.getString("mail");
                    String password = rs.getString("password");
                    LocalDate dateArrivee = rs.getDate("date_arrivee") != null ? rs.getDate("date_arrivee").toLocalDate() : null;
                    LocalDate dateDepart = rs.getDate("date_depart") != null ? rs.getDate("date_depart").toLocalDate() : null;
                    int ligueId = rs.getInt("ligue_id");
                    boolean estRoot = rs.getBoolean("est_root");

                    Ligue ligue = liguesLoaded.get(ligueId); // Récupère la ligue par son ID

                    Employe employe = new Employe(gestionPersonnel, id, ligue, nom, prenom, mail, password, dateArrivee, dateDepart);
                    if (estRoot) {
                        employe.setEstRoot(true);
                    }
                    return employe;
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de la récupération de l'employé par nom : " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Employe getEmployeByMail(String mail) throws SauvegardeImpossible {
        String sql = "SELECT id, nom, prenom, mail, password, date_arrivee, date_depart, ligue_id, est_root FROM employe WHERE mail = ?"; // NOM CORRIGÉ
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, mail);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String password = rs.getString("password");
                    LocalDate dateArrivee = rs.getDate("date_arrivee") != null ? rs.getDate("date_arrivee").toLocalDate() : null;
                    LocalDate dateDepart = rs.getDate("date_depart") != null ? rs.getDate("date_depart").toLocalDate() : null;
                    int ligueId = rs.getInt("ligue_id");
                    boolean estRoot = rs.getBoolean("est_root");

                    Ligue ligue = liguesLoaded.get(ligueId); // Récupère la ligue par son ID

                    Employe employe = new Employe(gestionPersonnel, id, ligue, nom, prenom, mail, password, dateArrivee, dateDepart);
                    if (estRoot) {
                        employe.setEstRoot(true);
                    }
                    return employe;
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de la récupération de l'employé par mail : " + e.getMessage(), e);
        }
        return null;
    }

    @Override
    public Employe getEmploye(int id) throws SauvegardeImpossible {
        String sql = "SELECT id, nom, prenom, mail, password, date_arrivee, date_depart, ligue_id, est_root FROM employe WHERE id = ?"; // NOM CORRIGÉ
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String nom = rs.getString("nom");
                    String prenom = rs.getString("prenom");
                    String mail = rs.getString("mail");
                    String password = rs.getString("password");
                    LocalDate dateArrivee = rs.getDate("date_arrivee") != null ? rs.getDate("date_arrivee").toLocalDate() : null;
                    LocalDate dateDepart = rs.getDate("date_depart") != null ? rs.getDate("date_depart").toLocalDate() : null;
                    int ligueId = rs.getInt("ligue_id");
                    boolean estRoot = rs.getBoolean("est_root");

                    Ligue ligue = liguesLoaded.get(ligueId); // Récupère la ligue par son ID

                    Employe employe = new Employe(gestionPersonnel, id, ligue, nom, prenom, mail, password, dateArrivee, dateDepart);
                    if (estRoot) {
                        employe.setEstRoot(true);
                    }
                    return employe;
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de la récupération de l'employé par ID : " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Ferme la connexion à la base de données.
     * @throws SauvegardeImpossible Si une erreur SQL se produit lors de la fermeture de la connexion.
     */
    @Override
    public void close() throws SauvegardeImpossible {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connexion à la base de données fermée.");
            }
        } catch (SQLException exception) {
            throw new SauvegardeImpossible(exception);
        }
    }

    /**
     * Sauvegarde l'état complet de la gestion du personnel.
     * Pour JDBC, les modifications sont persistées directement, donc cette méthode ferme la connexion.
     * @param gestionPersonnel L'instance de GestionPersonnel à sauvegarder.
     * @throws SauvegardeImpossible Si une erreur de sauvegarde se produit.
     */
    @Override
    public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible {
        close(); // Ferme la connexion à la base de données
    }

	@Override
	public boolean utilisateurExiste(String nomUtilisateur) throws SauvegardeImpossible {
		// TODO Auto-generated method stub
		return false;
	}
}