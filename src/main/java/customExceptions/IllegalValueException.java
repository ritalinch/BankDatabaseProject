package customExceptions;

public class IllegalValueException extends Exception{

    public IllegalValueException() {
        super("""
                Illegal value.
                Try one more time.
                """);
    }

}
