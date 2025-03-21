package serialisation;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import personnel.GestionPersonnel;
import personnel.Ligue;
import personnel.SauvegardeImpossible;
import personnel.Employe;
import personnel.Passerelle;

public class Serialization implements Passerelle {
    private static final String FILE_NAME = "GestionPersonnel.srz";

    @Override
    public GestionPersonnel getGestionPersonnel() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            return (GestionPersonnel) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * Sauvegarde le gestionnaire pour qu'il soit ouvert automatiquement
     * lors d'une exécution ultérieure du programme.
     * @throws SauvegardeImpossible Si le support de sauvegarde est inaccessible.
     */
    @Override
    public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel) throws SauvegardeImpossible {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(gestionPersonnel);
        } catch (IOException e) {
            throw new SauvegardeImpossible(e);
        }
    }

    @Override
    public int insert(Ligue ligue) throws SauvegardeImpossible {
        return -1;
    }

    @Override
    public int insert(Employe employe) throws SauvegardeImpossible {
        // Implémentation spécifique à la sérialisation
        // Par exemple, pour_générer un ID unique et sauvegarder l'employé dans un fichier
        throw new SauvegardeImpossible("Non implémenté en mode sérialisation.");
    }

	@Override
	public Employe getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean utilisateurExiste(String nomUtilisateur) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void update(Ligue ligue) throws SauvegardeImpossible {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Employe employe) throws SauvegardeImpossible {
		// TODO Auto-generated method stub
		
	}
	
	  @Override
	    public void delete(Employe employe) throws SauvegardeImpossible {
		// TODO Auto-generated method stub
			
		}
	  
	  @Override
	    public void delete(Ligue ligue) throws SauvegardeImpossible {
		// TODO Auto-generated method stub
			
		}

	
}
