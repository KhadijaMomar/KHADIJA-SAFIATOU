package personnel;
public class EmployeDejaExistantException extends IllegalArgumentException {
    private static final long serialVersionUID = 1L;

    public EmployeDejaExistantException(String message) {
        super(message);
    }

    public EmployeDejaExistantException(String message, Throwable cause) {
        super(message, cause);
    }
}
