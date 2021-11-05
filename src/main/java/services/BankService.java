package services;

import entities.Account;
import entities.Client;
import entities.Currency;

import javax.persistence.TypedQuery;
import java.math.BigDecimal;

public class BankService {

    private static Client currentClient = null;

    static void createAccount() {
        try {
            System.out.println("Enter chosen currency");
            Currency currency = Currency.valueOf(MainService.scanner().nextLine());
            TypedQuery<Long> query = MainService.em().createQuery(
                    "SELECT COUNT(a) FROM Account a WHERE a.client = :client AND a.currency = :currency", Long.class);
            query.setParameter("client", currentClient);
            query.setParameter("currency", currency);

            if (query.getSingleResult() != 0) {
                System.err.println("""
                        Account in this currency already exists.
                        You are not able to create more.
                        """);
            } else {
                Account account = new Account(currency, currentClient);
                currentClient.addAccount(account);
                MainService.performTransaction(MainService.em(), () -> MainService.em().persist(account));
                System.out.println("Account was created.");
            }


        } catch (IllegalArgumentException e) {
            System.err.println("No such currency exists.");
        } finally {
            MainService.doActions();
        }
    }

    static void getTotalBalanceInUAH() {

        checkAccountList();

        BigDecimal res = new BigDecimal("0");

        for (Account a : currentClient.getAccounts()) {
            if (a.getCurrency() == Currency.UAH) {
                res = res.add(a.getBalance());
            } else {
                res = res.add(a.getBalance().multiply(new BigDecimal("" + getRate(a.getCurrency()))));
            }
        }
    }

    private static Float getRate(Currency currency) {

        TypedQuery<Float> query = MainService.em().createQuery(
                "SELECT c.rate " +
                        "FROM CurrencyRate c " +
                        "WHERE c.id = (SELECT MAX(id) " +
                        "FROM CurrencyRate " +
                        "WHERE currency = :currency)", Float.class);

        query.setParameter("currency", currency);

        return query.getSingleResult();
    }

    static synchronized void topUp() {

        checkAccountList();

        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(MainService.scanner().nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal amount = new BigDecimal(MainService.scanner().nextLine());

        if (account == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Illegal value");
            topUp();
        }

        MainService.performTransaction(MainService.em(),
                () -> MainService.em().persist(account.topUpBalance(amount)));
    }

    static synchronized void retain() {

        checkAccountList();

        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(MainService.scanner().nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal amount = new BigDecimal(MainService.scanner().nextLine());
        if (account == null || amount.compareTo(BigDecimal.ZERO) >= 0) {
            System.err.println("Illegal value");
            retain();
        }


        MainService.performTransaction(MainService.em(),
                () -> MainService.em().persist(account.retainBalance(amount)));
    }

    private static synchronized void retain(Account account, BigDecimal value) {
        if (account == null || value.compareTo(BigDecimal.ZERO) >= 0) {
            System.err.println("Illegal value");
            retain(account, value);
        }

        MainService.performTransaction(MainService.em(),
                () -> MainService.em().persist(account.retainBalance(value)));
    }


    private static synchronized void topUp(Account account, BigDecimal value) {
        if (account == null || value.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Illegal value");
            topUp(account, value);
        }

        MainService.performTransaction(MainService.em(),
                () -> MainService.em().persist(account.topUpBalance(value)));

    }

    private static Account getAccountByCurrency(String currency) {
        for (Account a : currentClient.getAccounts()) {
            if (a.getCurrency().toString().equals(currency)) {
                return a;
            }
        }
        return null;
    }

    static synchronized void transfer() {

        checkAccountList();

        System.out.println("Choose currency for 'from' account:");
        Account from = getAccountByCurrency(MainService.scanner().nextLine());

        System.out.println("Choose currency for 'to' account:");
        Account to = getAccountByCurrency(MainService.scanner().nextLine());

        System.out.println("Enter amount to transfer:");
        BigDecimal value = new BigDecimal(MainService.scanner().nextLine());

        if (from == null || to == null) {
            System.err.println("Illegal value");
            MainService.doActions();
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Illegal value");
            MainService.doActions();
        }

        retain(from, value);
        topUp(to, value);

        System.out.println("Transaction succeeded.");
    }

    static synchronized void setCurrentClient(Client client) {
        currentClient = client;
        MainService.doActions();
    }

    private static void checkAccountList() {
        if (currentClient.getAccounts().isEmpty()) {
            System.err.println("You have no accounts.");
            MainService.doActions();
        }
    }

}
