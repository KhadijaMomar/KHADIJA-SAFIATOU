package personnel;

/**
 * Exception levée lorsqu'il est impossible de sauvegarder le gestionnaire.
 */
public class SauvegardeImpossible extends Exception {
    private static final long serialVersionUID = 6651919630441855001L;

    // Constructeur prenant un message en paramètre
    public SauvegardeImpossible(String message) {
        super(message);
    }

    // Constructeur prenant une exception cause en paramètre
    public SauvegardeImpossible(Exception cause) {
        super(cause);
    }

    /**
     * Nouveau constructeur prenant un message et une cause en paramètres.
     * @param message Le message détaillé de l'erreur.
     * @param cause La cause de l'exception (par exemple, une SQLException).
     */
    public SauvegardeImpossible(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        if (getCause() != null) {
            System.err.println("Causé par : ");
            getCause().printStackTrace();
        }
    }
}
