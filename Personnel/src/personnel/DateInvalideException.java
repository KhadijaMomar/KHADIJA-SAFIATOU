package personnel;

public class DateInvalideException extends IllegalArgumentException {
    public DateInvalideException(String message) {
        super(message);
    }
}