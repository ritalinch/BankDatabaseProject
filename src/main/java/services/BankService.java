package services;

import customExceptions.IllegalValueException;
import customExceptions.SuchAccountAlreadyExistsException;
import customExceptions.SuchEntityExistsException;
import entities.Account;
import entities.Client;
import entities.Currency;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Scanner;

public class BankService {

    private static final String NAME = "JPATest";
    private static final EntityManagerFactory EM_FACTORY = Persistence.createEntityManagerFactory(NAME);
    private static final EntityManager EM = EM_FACTORY.createEntityManager();
    private static final BankService BANK_SERVICE = new BankService();
    private static final Scanner SCANNER = new Scanner(System.in);

    private Client currentClient = null;

    public static void makeBankServiceStarted() {
        try {
            BANK_SERVICE.start();
        } catch (SuchAccountAlreadyExistsException | IllegalValueException | SuchEntityExistsException e) {
            System.out.println(e.getMessage());
        }
    }

    private void start() throws SuchAccountAlreadyExistsException, IllegalValueException, SuchEntityExistsException {

        System.out.println("""
                To sign in type '1'
                To sign up type '2'
                -------->
                """);

        switch (SCANNER.nextLine()) {
            case "1" -> tryToSignIn();
            case "2" -> register();
            default -> start();
        }

        closeAll();

    }

    private void performTransaction(Runnable runnable) {
        EntityTransaction transaction = EM.getTransaction();
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

    private void tryToSignIn() throws SuchAccountAlreadyExistsException, IllegalValueException, SuchEntityExistsException {
        String login;
        String password;
        do {

            System.out.println("Enter login");
            login = SCANNER.nextLine();

            System.out.println("Enter password");
            password = SCANNER.nextLine();

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

                String ans = SCANNER.nextLine();

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

    private void register() throws SuchEntityExistsException, SuchAccountAlreadyExistsException, IllegalValueException {

        System.out.println("Enter your name:");
        String name = SCANNER.nextLine();

        System.out.println("Enter your surname:");
        String surname = SCANNER.nextLine();

        System.out.println("Enter your age");
        Integer age = Integer.parseInt(SCANNER.nextLine());

        System.out.println("Create login:");
        String login = SCANNER.nextLine();

        System.out.println("Create password:");
        String password = SCANNER.nextLine();

        TypedQuery<Long> query = EM.createQuery(
                "SELECT COUNT(c) FROM Client c WHERE c.login = :login AND c.password = :password", Long.class);
        query.setParameter("login", login);
        query.setParameter("password", password);

        if(query.getSingleResult() != 0L) {
            throw new SuchEntityExistsException();
        } else {
            performTransaction(() -> EM.persist(new Client(
                    name,
                    surname,
                    age,
                    login,
                    password
            )));

            System.out.println("A new client was created.");

            tryToSignIn();
        }

    }

    private boolean logIn(String login, String password) throws SuchAccountAlreadyExistsException, SuchEntityExistsException, IllegalValueException {
        TypedQuery<Client> query = EM.createQuery(
                "SELECT c FROM Client c WHERE c.login = :login AND c.password = :password", Client.class);
        query.setParameter("login", login);
        query.setParameter("password", password);

        try {
            currentClient = query.getSingleResult();
            return true;
        } catch (NoResultException e) {
            System.err.println("No such user.");
            start();
        }

        return false;

    }

    private void createAccount() throws SuchAccountAlreadyExistsException {
        System.out.println();
        System.out.println("Enter chosen currency");
        Currency currency = Currency.valueOf(SCANNER.nextLine());

        TypedQuery<Short> query = EM.createQuery(
                "SELECT COUNT(a) FROM Account a WHERE a.client = :client AND a.currency = :currency", Short.class);
        query.setParameter("client", currentClient);
        query.setParameter("currency", currency);

        if(query.getSingleResult() != 0) {
            throw new SuchAccountAlreadyExistsException();
        }

        Account account = new Account(currency);
        currentClient.addAccount(account);
        performTransaction(() -> EM.persist(account));
    }

    private void getBalancesInChosenCurrency() {
        System.out.println();
        System.out.println("Enter chosen currency");
        Currency currency = Currency.valueOf(SCANNER.nextLine());

        for (Account a : currentClient.getAccounts()) {
            System.out.println(a.getCurrency() + ": " +
                    a.getBalance().multiply(new BigDecimal("" + getRate(a.getCurrency(), currency))));
        }
    }

    private void getTotalBalanceInUAH() {
        BigDecimal res = new BigDecimal("0");

        for (Account a : currentClient.getAccounts()) {
            res = res.add(a.getBalance().multiply(new BigDecimal("" + getRate(a.getCurrency(), Currency.UAH))));
        }
    }

    private Double getRate(Currency from, Currency to) {
        TypedQuery<Long> query = EM.createQuery(
                "SELECT c.id FROM CurrencyRate c WHERE c.currencyFrom = :from AND c.currencyTo = :to", Long.class);
        query.setParameter("from", from);
        query.setParameter("to", to);

        Long idObtained = query.getSingleResult();

        TypedQuery<Double> query2 = EM.createQuery(
                "SELECT c.rate FROM CurrencyRate c WHERE c.id = :id", Double.class);
        query2.setParameter("id", idObtained);

        return query2.getSingleResult();
    }

    private synchronized void topUp() throws IllegalValueException {
        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(SCANNER.nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal amount = new BigDecimal(SCANNER.nextLine());

        if(account == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalValueException();
        }

        account.changeBalance(amount);
    }

    private synchronized void retain() throws IllegalValueException{
        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(SCANNER.nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal amount = new BigDecimal(SCANNER.nextLine());
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
        for(Account a : currentClient.getAccounts()) {
            if(a.getCurrency().toString().equals(currency)) {
                return a;
            }
        }
        return null;
    }

    private synchronized void transfer() throws IllegalValueException{
        System.out.println();
        System.out.println("Choose currency for 'from' account:");
        Account from = getAccountByCurrency(SCANNER.nextLine());

        System.out.println("Choose currency for 'to' account:");
        Account to = getAccountByCurrency(SCANNER.nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal value = new BigDecimal(SCANNER.nextLine());

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
        SCANNER.close();
        if (EM != null) EM.close();
        if (EM_FACTORY != null) {
            EM_FACTORY.close();
        }
    }

}
