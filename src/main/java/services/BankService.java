package services;

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
    private final SignInSignUpService SIGN_IN_SIGN_UP_SERVICE = new SignInSignUpService();

    public static void makeBankServiceStarted() {
        BANK_SERVICE.start();
    }

    private void start() {

        System.out.println("""
                To sign in type '1'
                To sign up type '2'
                -------->
                """);

        switch (SCANNER.nextLine()) {
            case "1" -> SIGN_IN_SIGN_UP_SERVICE.tryToSignIn(EM, SCANNER);
            case "2" -> SIGN_IN_SIGN_UP_SERVICE.register(EM, SCANNER);
            default -> start();
        }

        closeAll();

    }

    private void doActions() {
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
    }

    static void performTransaction(Runnable runnable) {

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

    private void createAccount() {
        try {

            System.out.println("Enter chosen currency");
            Currency currency = Currency.valueOf(SCANNER.nextLine());
            TypedQuery<Long> query = EM.createQuery(
                    "SELECT COUNT(a) FROM Account a WHERE a.client = :client AND a.currency = :currency", Long.class);
            query.setParameter("client", currentClient);
            query.setParameter("currency", currency);

            if(query.getSingleResult() != 0) {
                System.err.println("""
                Account in this currency already exists.
                You are not able to create more.
                """);
            } else {
                Account account = new Account(currency, currentClient);
                currentClient.addAccount(account);
                performTransaction(() -> EM.persist(account));
                System.out.println("Account was created.");
            }


        } catch (IllegalArgumentException e) {
            System.err.println("No such currency exists.");
        } finally {
            doActions();
        }
    }

    private void getBalancesInChosenCurrency() {

        checkAccountList();

        System.out.println("Enter chosen currency");
        Currency currency = Currency.valueOf(SCANNER.nextLine());

        for (Account a : currentClient.getAccounts()) {
            System.out.println(a.getCurrency() + ": " +
                    a.getBalance().multiply(new BigDecimal("" + getRate(a.getCurrency(), currency))));
        }
    }

    private void getTotalBalanceInUAH() {

        checkAccountList();

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

    private synchronized void topUp() {

        checkAccountList();

        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(SCANNER.nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal amount = new BigDecimal(SCANNER.nextLine());

        if(account == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Illegal value");
            topUp();
        }

        account.changeBalance(amount);
    }

    private synchronized void retain() {

        checkAccountList();

        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(SCANNER.nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal amount = new BigDecimal(SCANNER.nextLine());
        if(account == null || amount.compareTo(BigDecimal.ZERO) >= 0) {
            System.err.println("Illegal value");
            retain();
        }

        account.changeBalance(amount);
    }

    private synchronized void retain(Account account, BigDecimal value) {
        if(account == null || value.compareTo(BigDecimal.ZERO) >= 0) {
            System.err.println("Illegal value");
            retain(account, value);
        }

        account.changeBalance(value);
    }


    private synchronized void topUp(Account account, BigDecimal value) {
        if(account == null || value.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Illegal value");
            topUp(account, value);
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

    private synchronized void transfer() {

        checkAccountList();

        System.out.println("Choose currency for 'from' account:");
        Account from = getAccountByCurrency(SCANNER.nextLine());

        System.out.println("Choose currency for 'to' account:");
        Account to = getAccountByCurrency(SCANNER.nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal value = new BigDecimal(SCANNER.nextLine());

        if (from == null || to == null) {
            System.err.println("Illegal value");
            doActions();
        }

        if(value.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Illegal value");
            doActions();
        }

        retain(from, value);
        topUp(to, value);

        System.out.println("Transaction succeeded.");
    }

    static synchronized void setCurrentClient(Client client) {
        BANK_SERVICE.currentClient = client;
        BANK_SERVICE.doActions();
    }

    private void checkAccountList() {
        if (currentClient.getAccounts().isEmpty()) {
            System.out.println("You have no accounts.");
            doActions();
        }
    }

    private synchronized void closeAll() {
        SCANNER.close();
        if (EM != null) EM.close();
        if (EM_FACTORY != null) {
            EM_FACTORY.close();
        }
    }

}
