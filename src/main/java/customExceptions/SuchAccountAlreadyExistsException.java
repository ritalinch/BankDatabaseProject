package customExceptions;

public class SuchAccountAlreadyExistsException extends Exception{
    public SuchAccountAlreadyExistsException() {
        super("entities.Account in this currency already exists. You are not able to create more.");
    }
}
