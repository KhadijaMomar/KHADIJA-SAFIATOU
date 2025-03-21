package personnel;

public interface Passerelle 
{
	public GestionPersonnel getGestionPersonnel();
	public void sauvegarderGestionPersonnel(GestionPersonnel gestionPersonnel)  throws SauvegardeImpossible;
	public int insert(Ligue ligue) throws SauvegardeImpossible;
	

	  /**
     * 
     * @param employe L'employé à insérer.
     * @return L'identifiant de l'employé inséré.
     * @throws SauvegardeImpossible Si l'insertion échoue.
     */
    public int insert(Employe employe) throws SauvegardeImpossible;
	public Employe getRoot();
	
	boolean utilisateurExiste(String nomUtilisateur);
	
	public void update(Ligue ligue) throws SauvegardeImpossible;
	
	public void update(Employe employe) throws SauvegardeImpossible;
	
	public void delete(Employe employe) throws SauvegardeImpossible;
	
	public void delete(Ligue ligue) throws SauvegardeImpossible;
}





