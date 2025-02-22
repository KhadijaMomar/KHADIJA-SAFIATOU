package jdbc;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import personnel.*;

public class JDBC implements Passerelle 
{
	Connection connection;

	public JDBC()
	{
		try
		{
			Class.forName(Credentials.getDriverClassName());
			connection = DriverManager.getConnection(Credentials.getUrl(), Credentials.getUser(), Credentials.getPassword());
		}
		catch (ClassNotFoundException e)
		{
			System.out.println("Pilote JDBC non installé.");
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
	}
	
	@Override
	public GestionPersonnel getGestionPersonnel() 
	{
		GestionPersonnel gestionPersonnel = new GestionPersonnel();
		try 
		{
			String requete = "select * from ligue";
			Statement instruction = connection.createStatement();
			ResultSet ligues = instruction.executeQuery(requete);
			while (ligues.next())
				gestionPersonnel.addLigue(ligues.getInt(1), ligues.getString(2));
		}
		catch (SQLException e)
		{
			System.out.println(e);
		}
		return gestionPersonnel;
	}

	@Override
	public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible 
	{
		close();
	}
	
	public void close() throws SauvegardeImpossible
	{
		try
		{
			if (connection != null)
				connection.close();
		}
		catch (SQLException e)
		{
			throw new SauvegardeImpossible(e);
		}
	}
	
	@Override
	public int insert(Ligue ligue) throws SauvegardeImpossible 
	{
		try 
		{
			PreparedStatement instruction;
			instruction = connection.prepareStatement("insert into ligue (nom) values(?)", Statement.RETURN_GENERATED_KEYS);
			instruction.setString(1, ligue.getNom());		
			instruction.executeUpdate();
			ResultSet id = instruction.getGeneratedKeys();
			id.next();
			return id.getInt(1);
		} 
		catch (SQLException exception) 
		{
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
		// TODO Auto-generated method stub
		return null;
	}
	
	
	 @Override
	
	    public boolean utilisateurExiste(String nomUtilisateur) {
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
	
}
