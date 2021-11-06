package services;

import entities.Account;
import entities.Client;
import entities.Currency;

import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
        }
    }

    static void getTotalBalanceInUAH() {

        if (checkAccountList()) {
            return;
        }

        BigDecimal resInUah = new BigDecimal("0");

        for (Account a : currentClient.getAccounts()) {
            if (a.getCurrency() == Currency.UAH) {
                resInUah = resInUah.add(a.getBalance());
            } else {
                resInUah = resInUah.add(a.getBalance().divide(new BigDecimal("" + getRate(a.getCurrency())), RoundingMode.HALF_DOWN));
            }
        }

        System.out.println("Total balance in UAH is " + resInUah + " UAH");
    }

    private static Double getRate(Currency currency) {

        if (currency == Currency.UAH) {
            return 1.0;
        }

        TypedQuery<Double> query = MainService.em().createQuery(
                "SELECT c.rate " +
                        "FROM CurrencyRate c " +
                        "WHERE c.id = (SELECT MAX(id) " +
                        "FROM CurrencyRate " +
                        "WHERE currency = :currency)", Double.class);

        query.setParameter("currency", currency);

        return query.getSingleResult();
    }

    static synchronized void topUp() {

        if (checkAccountList()) {
            return;
        }

        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(MainService.scanner().nextLine());

        if (account == null) {
            System.out.println("No account with this currency.");
        } else {
            System.out.println("Enter amount to transfer:");
            BigDecimal amount = new BigDecimal(MainService.scanner().nextLine());
            account.topUpBalance(amount);
        }
    }

    static synchronized void retain() {
        if (checkAccountList()) {
            return;
        }
        System.out.println("Choose currency for account:");
        Account account = getAccountByCurrency(MainService.scanner().nextLine());

        if (account == null) {
            System.err.println("No account with this currency");
        } else {
            System.out.println("Enter amount to transfer:");
            BigDecimal amount = new BigDecimal(MainService.scanner().nextLine());
            account.retainBalance(amount);
        }
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

        if (checkAccountList()) {
            return;
        }

        System.out.println("Choose currency for 'from' account:");
        Account from = getAccountByCurrency(MainService.scanner().nextLine());

        System.out.println("Choose currency for 'to' account:");
        Account to = getAccountByCurrency(MainService.scanner().nextLine());

        if (from == null || to == null) {
            System.err.println("Illegal currencies.");
        } else {
            System.out.println("Enter amount to transfer:");
            BigDecimal value = new BigDecimal(MainService.scanner().nextLine());
            if (value.compareTo(from.getBalance()) > 0
                    || value.compareTo(BigDecimal.ZERO) < 0) {
                System.err.println("Illegal argument to transfer.");
            } else {
                from.retainBalance(value);
                to.topUpBalance(convertCurrency(value, from.getCurrency(), to.getCurrency()));
                System.out.println("Transaction succeeded.");
            }
        }
    }

    private static BigDecimal convertCurrency(BigDecimal value, Currency from, Currency to) {
        BigDecimal midResInUah = value.divide(new BigDecimal("" + getRate(from)));
        return midResInUah.multiply(new BigDecimal("" + getRate(to)));
    }

    static void setCurrentClient(Client client) {
        currentClient = client;
    }

    private static boolean checkAccountList() {
        boolean isEmpty = currentClient.getAccounts().isEmpty();
        if (isEmpty) {
            System.err.println("You have no accounts.");
        }
        return isEmpty;
    }
}
