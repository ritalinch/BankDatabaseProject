import customExceptions.IllegalValueException;
import customExceptions.SuchAccountAlreadyExistsException;
import services.BankService;

public class Main {

    public static void main(String[] args) throws SuchAccountAlreadyExistsException, IllegalValueException {

        BankService.makeBankServiceStarted();

    }

}
