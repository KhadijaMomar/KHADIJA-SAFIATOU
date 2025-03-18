  package jdbc;

    import personnel.GestionPersonnel;
    import personnel.Ligue;
    import personnel.Employe;
    import personnel.SauvegardeImpossible;

    import java.sql.*;
    import java.time.LocalDate;
    import java.sql.DriverManager;
    import java.sql.PreparedStatement;
    import java.sql.Connection;
    import java.sql.ResultSet;
    import java.sql.SQLException;
    import java.sql.Statement;


import personnel.*;

        public class JDBC implements Passerelle {
            private Connection connection;
            private GestionPersonnel gestionPersonnel; // Variable d'instance

            // Constructeur pour initialiser gestionPersonnel
            public JDBC(GestionPersonnel gestionPersonnel) {
                this.gestionPersonnel = gestionPersonnel; 
                try {
                    Class.forName(Credentials.getDriverClassName());
                    connection = DriverManager.getConnection(Credentials.getUrl(), Credentials.getUser(), Credentials.getPassword());
                } catch (ClassNotFoundException e) {
                    System.out.println("Pilote JDBC non installé.");
                } catch (SQLException e) {
                    System.out.println(e);
                }
            }
        
        
         @Override
            public GestionPersonnel getGestionPersonnel() {
                try {
                    // Charge les ligues depuis la base de données
                    String requete = "SELECT * FROM ligue";
                    Statement instruction = connection.createStatement();
                    ResultSet ligues = instruction.executeQuery(requete);
                    while (ligues.next()) {
                        gestionPersonnel.addLigue(ligues.getInt("id"), ligues.getString("nom"));
                    }

                    // Charge le root depuis la base de données
                    Employe root = getRoot();
                    if (root != null) {
                        gestionPersonnel.addRoot(root.getNom(), root.getPassword());
                    } else {
                        // Si le root n'existe pas, il sera créé lors de l'appel à addRoot()
                        gestionPersonnel.addRoot("root", "toor");
                    }
                } catch (SQLException | SauvegardeImpossible e) {
                    System.out.println("Erreur lors du chargement des données : " + e.getMessage());
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

            // Gestion de ligue_id : NULL pour le root, sinon l'ID de la ligue
            if (employe.getLigue() == null) {
                instruction.setNull(7, java.sql.Types.INTEGER); // NULL pour le root
            } else {
                instruction.setInt(7, employe.getLigue().getId()); // ID de la ligue pour les autres employés
            }

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
        String query = "SELECT * FROM employe WHERE nom = 'root'";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int ligueId = rs.getInt("ligue_id");
                Ligue ligue = null;
                if (!rs.wasNull()) { // Vérifie si ligue_id est NULL
                    ligue = gestionPersonnel.getLigue(ligueId);
                }
                return Employe.createEmployeWithId(
                    gestionPersonnel,
                    rs.getInt("id"),
                    rs.getString("nom"),
                    rs.getString("prenom"),
                    rs.getString("mail"),
                    rs.getString("password"),
                    rs.getDate("date_arrivee") != null ? rs.getDate("date_arrivee").toLocalDate() : null,
                    rs.getDate("date_depart") != null ? rs.getDate("date_depart").toLocalDate() : null,
                    ligue
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Retourne null si le root n'existe pas
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
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
