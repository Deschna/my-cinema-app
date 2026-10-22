package cinema.exception;

public class ResourceReferencedException extends RuntimeException {
    public ResourceReferencedException(String message, Throwable cause) {
        super(message, cause);
    }
}
