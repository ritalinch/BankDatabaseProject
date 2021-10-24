import customExceptions.IllegalValueException;
import customExceptions.SuchAccountAlreadyExistsException;
import entities.Account;
import entities.Client;
import entities.Currency;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Scanner;

public class BankService {

    private static final BankService bankService = new BankService();
    private static final String NAME = "JPATest";

    private Client currentClient = null;
    private EntityManagerFactory emFactory;
    private EntityManager em;
    private Scanner scanner;

    private BankService() {
        bankService.emFactory = Persistence.createEntityManagerFactory(NAME);
        bankService.em = bankService.emFactory.createEntityManager();
        bankService.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) throws SuchAccountAlreadyExistsException, IllegalValueException {

        bankService.tryToSignIn();
        bankService.closeAll();
    }

    private void performTransaction(Runnable runnable) {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            runnable.run();
            transaction.commit();

        } catch (Exception ex) {
            if (transaction.isActive())
                transaction.rollback();

            throw new RuntimeException(ex);
        }
    }

    private void tryToSignIn() throws SuchAccountAlreadyExistsException, IllegalValueException {
        String login;
        String password;
        do {

            System.out.println("Enter login");
            login = bankService.scanner.nextLine();
            password = bankService.scanner.nextLine();

        } while (!logIn(login, password));

        doActions();
    }

    private void doActions() throws SuchAccountAlreadyExistsException, IllegalValueException {
        try {

            boolean logout = false;
            while(!logout) {
                System.out.println("""
                    1. To top up enter _____________________________________1__
                    2. To retain enter _____________________________________2__
                    3. To see all balances in one currency enter ___________3__
                    4. To see total balance in UAH enter ___________________4__
                    5. To create a new account enter _______________________5__
                    6. To transfer money from one account to another enter _6__
                    7. To logout enter _____________________________________7__
                    """);

                String ans = bankService.scanner.nextLine();

                switch(ans) {
                    case "1" -> topUp();
                    case "2" -> retain();
                    case "3" -> getBalancesInChosenCurrency();
                    case "4" -> getTotalBalanceInUAH();
                    case "5" -> createAccount();
                    case "6" -> transfer();
                    case "7" -> logout = true;
                }
            }
        } catch (Exception e) {
            throw e;
        }
    }

    private boolean logIn(String login, String password) {
        TypedQuery<Client> query = bankService.em.createQuery(
                "SELECT c FROM Client c WHERE c.login = :login AND c.password = :password", Client.class);
        query.setParameter("login", login);
        query.setParameter("password", password);

        Optional<Client> logging = Optional.ofNullable(query.getSingleResult());
        if(logging.isPresent()) {
            bankService.currentClient = logging.get();
            return true;
        } else {
            return false;
        }
    }

    private void createAccount() throws SuchAccountAlreadyExistsException {
        System.out.println();
        System.out.println("Enter chosen currency");
        Currency currency = Currency.valueOf(bankService.scanner.nextLine());

        TypedQuery<Short> query = em.createQuery(
                "SELECT COUNT(a) FROM Account a WHERE a.client = :client AND a.currency = :currency", Short.class);
        query.setParameter("client", bankService.currentClient);
        query.setParameter("currency", currency);

        if(query.getSingleResult() != 0) {
            throw new SuchAccountAlreadyExistsException();
        }

        Account account = new Account(currency);
        bankService.currentClient.addAccount(account);
        performTransaction(() -> bankService.em.persist(account));
    }

    private void getBalancesInChosenCurrency() {
        System.out.println();
        System.out.println("Enter chosen currency");
        Currency currency = Currency.valueOf(bankService.scanner.nextLine());

        for (Account a : bankService.currentClient.getAccounts()) {
            System.out.println(a.getCurrency() + ": " +
                    a.getBalance().multiply(new BigDecimal("" + getRate(a.getCurrency(), currency))));
        }
    }

    private void getTotalBalanceInUAH() {
        BigDecimal res = new BigDecimal("0");

        for (Account a : bankService.currentClient.getAccounts()) {
            res = res.add(a.getBalance().multiply(new BigDecimal("" + getRate(a.getCurrency(), Currency.UAH))));
        }
    }

    private Double getRate(Currency from, Currency to) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT c.id FROM CurrencyRate c WHERE c.from = :from AND c.to = :to", Long.class);
        query.setParameter("from", from);
        query.setParameter("to", to);

        Long idObtained = query.getSingleResult();

        TypedQuery<Double> query2 = em.createQuery(
                "SELECT c.rate FROM CurrencyRate c WHERE c.id = :id", Double.class);
        query2.setParameter("id", idObtained);

        return query2.getSingleResult();
    }

    private synchronized void topUp() throws IllegalValueException {
        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(bankService.scanner.nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal amount = new BigDecimal(bankService.scanner.nextLine());

        if(account == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalValueException();
        }

        account.changeBalance(amount);
    }

    private synchronized void retain() throws IllegalValueException{
        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(bankService.scanner.nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal amount = new BigDecimal(bankService.scanner.nextLine());
        if(account == null || amount.compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalValueException();
        }

        account.changeBalance(amount);
    }

    private synchronized void retain(Account account, BigDecimal value) throws IllegalValueException{
        if(account == null || value.compareTo(BigDecimal.ZERO) >= 0) {
            throw new IllegalValueException();
        }

        account.changeBalance(value);
    }


    private synchronized void topUp(Account account, BigDecimal value) throws IllegalValueException{
        if(account == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalValueException();
        }

        account.changeBalance(value);
    }

    private Account getAccountByCurrency(String currency) {
        for(Account a : bankService.currentClient.getAccounts()) {
            if(a.getCurrency().toString().equals(currency)) {
                return a;
            }
        }
        return null;
    }

    private synchronized void transfer() throws IllegalValueException{
        System.out.println();
        System.out.println("Choose currency for 'from' account:");
        Account from = getAccountByCurrency(bankService.scanner.nextLine());

        System.out.println("Choose currency for 'to' account:");
        Account to = getAccountByCurrency(bankService.scanner.nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal value = new BigDecimal(bankService.scanner.nextLine());

        if (from == null || to == null) {
            throw new IllegalValueException();
        }

        if(value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalValueException();
        }

        retain(from, value);
        topUp(to, value);

        System.out.println("Transaction succeeded.");
    }

    private void closeAll() {
        scanner.close();
        if (em != null) em.close();
        if (emFactory != null) emFactory.close();
    }
}
