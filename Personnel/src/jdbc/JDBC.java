package jdbc;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import personnel.*;

public class JDBC implements Passerelle {
    Connection connection;

    // Constructeur pour établir la connexion à la base de données
    public JDBC() {
        try {
            // Pour charger le pilote JDBC
            Class.forName(Credentials.getDriverClassName());
            // Pour etablir la connexion avec les informations d'identification
            connection = DriverManager.getConnection(Credentials.getUrl(), Credentials.getUser(), Credentials.getPassword());
        } catch (ClassNotFoundException e) {
            System.out.println("Pilote JDBC non installé.");
        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    @Override
    public GestionPersonnel getGestionPersonnel() {
        GestionPersonnel gestionPersonnel = GestionPersonnel.getGestionPersonnel();
        try {
            // Pour charger les ligues depuis la base de données
            String requete = "SELECT * FROM ligue";
            Statement instruction = connection.createStatement();
            ResultSet ligues = instruction.executeQuery(requete);
            while (ligues.next()) {
                gestionPersonnel.addLigue(ligues.getInt("id"), ligues.getString("nom"));
            }

            // Charger l'utilisateur root
            Employe root = getRoot();
            if (root != null) {
                try {
                    gestionPersonnel.addRoot(root.getNom(), root.getPassword());
                } catch (SauvegardeImpossible e) {
                    System.out.println("Erreur lors de l'ajout du root: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println(e);
        }
        return gestionPersonnel;
    }

    @Override
    public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible {
        close();
    }
    // Pour fermer la connexion à la base de données
    public void close() throws SauvegardeImpossible {
        try {
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            throw new SauvegardeImpossible(e);
        }
    }

    @Override
    public int insert(Ligue ligue) throws SauvegardeImpossible {
        try {
            // Insérer une nouvelle ligue dans la base de données
            PreparedStatement instruction;
            instruction = connection.prepareStatement("insert into ligue (nom) values(?)", Statement.RETURN_GENERATED_KEYS);
            instruction.setString(1, ligue.getNom());
            instruction.executeUpdate();
            ResultSet id = instruction.getGeneratedKeys();
            id.next();
            return id.getInt(1);
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SauvegardeImpossible(exception);
        }
    }

    @Override
    public int insert(Employe employe) throws SauvegardeImpossible {
        try {
            // Insérer un nouvel employé dans la base de données
            PreparedStatement instruction;
            instruction = connection.prepareStatement(
                "INSERT INTO employe (nom, prenom, mail, password, date_arrivee, date_depart, ligue_id) VALUES (?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            instruction.setString(1, employe.getNom());
            instruction.setString(2, employe.getPrenom());
            instruction.setString(3, employe.getMail());
            instruction.setString(4, employe.getPassword());
            instruction.setDate(5, employe.getDateArrivee() != null ? java.sql.Date.valueOf(employe.getDateArrivee()) : null);
            instruction.setDate(6, employe.getDateDepart() != null ? java.sql.Date.valueOf(employe.getDateDepart()) : null);
            instruction.setInt(7, employe.getLigue() != null ? employe.getLigue().getId() : -1); // -1 si pas de ligue (root)

            instruction.executeUpdate();
            ResultSet id = instruction.getGeneratedKeys();
            if (id.next()) {
                return id.getInt(1); // Retourne l'ID généré
            } else {
                throw new SauvegardeImpossible("Aucun ID généré pour l'employé.");
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SauvegardeImpossible(exception);
        }
    }

    @Override
    public Employe getRoot() {
        // Récupérer l'utilisateur root depuis la base de données
        String query = "SELECT * FROM employe WHERE nom = 'root'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Employe(
                    GestionPersonnel.getGestionPersonnel(),
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("mail"),
                    rs.getString("password"),
                    rs.getDate("date_arrivee") != null ? rs.getDate("date_arrivee").toLocalDate() : null,
                    rs.getDate("date_depart") != null ? rs.getDate("date_depart").toLocalDate() : null,
                    rs.getInt("ligue_id")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retourne null si aucun root n'est trouvé
    }

    @Override
    public boolean utilisateurExiste(String nomUtilisateur) {
        // Pour vérifier si un utilisateur existe dans la base de données
        String query = "SELECT COUNT(*) FROM employe WHERE nom = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, nomUtilisateur);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    
    @Override
    public void update(Ligue ligue) throws SauvegardeImpossible {
        try {
            // Mise à jour ,le nom de la ligue dans la base de données
            PreparedStatement instruction = connection.prepareStatement("UPDATE ligue SET nom = ? WHERE id = ?");
            instruction.setString(1, ligue.getNom());
            instruction.setInt(2, ligue.getId());
            instruction.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SauvegardeImpossible(exception);
        }
    }

    @Override
    public void update(Employe employe) throws SauvegardeImpossible {
        try {
            // Mise à jour des informations de l'employé dans la base de données
            PreparedStatement instruction = connection.prepareStatement(
                "UPDATE employe SET nom = ?, prenom = ?, mail = ?, password = ?, date_arrivee = ?, date_depart = ?, ligue_id = ? WHERE id = ?"
            );
            instruction.setString(1, employe.getNom());
            instruction.setString(2, employe.getPrenom());
            instruction.setString(3, employe.getMail());
            instruction.setString(4, employe.getPassword());
            instruction.setDate(5, employe.getDateArrivee() != null ? java.sql.Date.valueOf(employe.getDateArrivee()) : null);
            instruction.setDate(6, employe.getDateDepart() != null ? java.sql.Date.valueOf(employe.getDateDepart()) : null);
            instruction.setInt(7, employe.getLigue() != null ? employe.getLigue().getId() : -1); // -1 si pas de ligue (root)
            instruction.setInt(8, employe.getId());

            instruction.executeUpdate();
        } catch (SQLException exception) {
            exception.printStackTrace();
            throw new SauvegardeImpossible(exception);
        }
    }	
}
