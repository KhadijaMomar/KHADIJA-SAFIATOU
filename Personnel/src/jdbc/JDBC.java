
package jdbc;

import personnel.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;

public class JDBC implements Passerelle {
    private Connection connection;
    private GestionPersonnel gestionPersonnel;
    private HashMap<Integer, Ligue> liguesLoaded;
    private HashMap<Integer, Employe> employesLoaded;

    public JDBC(GestionPersonnel gestionPersonnel) {
        this.gestionPersonnel = gestionPersonnel;
        this.liguesLoaded = new HashMap<>();
        this.employesLoaded = new HashMap<>();
        try {
            Class.forName(Credentials.getDriverClassName());
            connection = DriverManager.getConnection(Credentials.getUrl(), Credentials.getUser(), Credentials.getPassword());
            initializeDatabaseSchema();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Pilote JDBC introuvable : " + e.getMessage(), e);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur de connexion à la base de données : " + e.getMessage(), e);
        }
    }

    private void initializeDatabaseSchema() throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ligue (" +
                                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                                    "nom VARCHAR(255) UNIQUE NOT NULL)");
            addColumnIfNotExist(statement, "ligue", "administrateur_id", "INTEGER");

            statement.executeUpdate("CREATE TABLE IF NOT EXISTS employe (" +
                                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                                    "nom VARCHAR(255) NOT NULL," +
                                    "prenom VARCHAR(255) NOT NULL," +
                                    "mail VARCHAR(255) UNIQUE NOT NULL," +
                                    "password VARCHAR(255) NOT NULL," +
                                    "ligue_id INTEGER," +
                                    "FOREIGN KEY (ligue_id) REFERENCES ligue(id) ON DELETE SET NULL)");
            addColumnIfNotExist(statement, "employe", "date_arrivee", "DATE");
            addColumnIfNotExist(statement, "employe", "date_depart", "DATE");
            addColumnIfNotExist(statement, "employe", "est_root", "BOOLEAN DEFAULT FALSE");

            if (!indexExists("employe", "idx_mail")) {
                statement.executeUpdate("CREATE UNIQUE INDEX idx_mail ON employe (mail)");
            }
            if (!indexExists("ligue", "idx_ligue_nom")) {
                statement.executeUpdate("CREATE UNIQUE INDEX idx_ligue_nom ON ligue (nom)");
            }
        }
    }

    private void addColumnIfNotExist(Statement statement, String tableName, String columnName, String columnType) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet columns = metaData.getColumns(null, null, tableName, columnName)) {
            if (!columns.next()) {
                statement.executeUpdate("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
            }
        }
    }

    private boolean indexExists(String tableName, String indexName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet indexes = metaData.getIndexInfo(null, null, tableName, false, false)) {
            while (indexes.next()) {
                if (indexName.equals(indexes.getString("INDEX_NAME"))) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int insert(Ligue ligue) throws SauvegardeImpossible {
        String sql = "INSERT INTO ligue (nom) VALUES (?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, ligue.getNom());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    liguesLoaded.put(id, ligue);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de l'insertion de la ligue : " + e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public int insert(Employe employe) throws SauvegardeImpossible {
        String sql = "INSERT INTO employe (nom, prenom, mail, password, date_arrivee, date_depart, ligue_id, est_root) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, employe.getNom());
            pstmt.setString(2, employe.getPrenom());
            pstmt.setString(3, employe.getMail());
            pstmt.setString(4, hashPassword(employe.getPassword()));
            pstmt.setDate(5, employe.getDateArrivee() != null ? Date.valueOf(employe.getDateArrivee()) : null);
            pstmt.setDate(6, employe.getDateDepart() != null ? Date.valueOf(employe.getDateDepart()) : null);
            pstmt.setObject(7, employe.getLigue() != null ? employe.getLigue().getId() : null, Types.INTEGER);
            pstmt.setBoolean(8, employe.estRoot());
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    employesLoaded.put(id, employe);
                    return id;
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors de l'insertion de l'employé : " + e.getMessage(), e);
        }
        return -1;
    }

    public String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe : " + e.getMessage(), e);
        }
    }

    @Override
    public void close() throws SauvegardeImpossible {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException exception) {
            throw new SauvegardeImpossible(exception);
        }
    }

    @Override
    public GestionPersonnel getGestionPersonnel() throws SauvegardeImpossible {
        // Chargement des ligues et employés
        liguesLoaded.clear();
        employesLoaded.clear();
        String selectLiguesSql = "SELECT id, nom, administrateur_id FROM ligue";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(selectLiguesSql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                int administrateurId = rs.getInt("administrateur_id");
                Employe administrateur = null; // Initialisation de l'administrateur
                if (administrateurId != 0) {
					 administrateur = getEmploye(administrateurId);// Récupère l'administrateur par ID
                }
                Ligue ligue = new Ligue(gestionPersonnel, id, nom, administrateur);
                gestionPersonnel.add(ligue);
                liguesLoaded.put(id, ligue);
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors du chargement des ligues : " + e.getMessage(), e);
        }

        String selectEmployesSql = "SELECT id, nom, prenom, mail, password, date_arrivee, date_depart, ligue_id, est_root FROM employe";
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

                Ligue ligue = liguesLoaded.get(ligueId);
                Employe employe = new Employe(gestionPersonnel, id, ligue, nom, prenom, mail, password, dateArrivee, dateDepart);
                if (ligue != null) {
                    ligue.addEmploye(employe);
                }
                employesLoaded.put(id, employe);

                if (estRoot) {
                    gestionPersonnel.setRoot(employe);
                    employe.setEstRoot(true);
                }
            }
        } catch (SQLException e) {
            throw new SauvegardeImpossible("Erreur lors du chargement des employés : " + e.getMessage(), e);
        }

        return gestionPersonnel;
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

	@Override
	public Employe getRoot() {
		 return this.gestionPersonnel.getRoot();
	}
}
