package customExceptions;

public class SuchEntityExistsException extends Exception {

    public SuchEntityExistsException() {
        super("Entity with this data already exists. You are not able to create more.");
    }

}
