package personnel;

/**
 * Levée si l'on tente de supprimer le super-utilisateur.
 */

public class ImpossibleDeSupprimerRoot extends RuntimeException
{
	public ImpossibleDeSupprimerRoot(String string) {
		super(string); // Appel au constructeur de la classe parente RuntimeException
	}

	private static final long serialVersionUID = 6850643427556906205L;
}
