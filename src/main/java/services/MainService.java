package services;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.Scanner;

public class MainService {

    private static final String NAME = "JPATest";
    private static final EntityManagerFactory EM_FACTORY = Persistence.createEntityManagerFactory(NAME);
    private static final EntityManager EM = EM_FACTORY.createEntityManager();
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void start() {

        RatesUpdateService.update();

        System.out.println("""
                To sign in type '1'
                To sign up type '2'
                -------->
                """);

        switch (SCANNER.nextLine()) {
            case "1" -> SignInSignUpService.tryToSignIn();
            case "2" -> SignInSignUpService.register();
            default -> start();
        }

        closeAll();

    }

    static void doActions() {
        boolean logout = false;
        while (!logout) {
            System.out.println("""
                    1. To top up enter _____________________________________1__
                    2. To retain enter _____________________________________2__
                    3. To see total balance in UAH enter ___________________3__
                    4. To create a new account enter _______________________4__
                    5. To transfer money from one account to another enter _5__
                    6. To search through transactions by amount_____________6__
                    7. To search through transactions by date ______________7__
                    8. To logout enter _____________________________________8__
                    """);

            String ans = SCANNER.nextLine();

            switch (ans) {
                case "1" -> BankService.topUp();
                case "2" -> BankService.retain();
                case "3" -> BankService.getTotalBalanceInUAH();
                case "4" -> BankService.createAccount();
                case "5" -> BankService.transfer();
                case "6" -> SearchService.searchThroughTransactionsByValue();
                case "7" -> SearchService.searchThroughTransactionsByDate();
                case "8" -> logout = true;
            }
        }
    }

    public static EntityManager em() {
        return EM;
    }

    public static Scanner scanner() {
        return SCANNER;
    }

    public static void performTransaction(EntityManager EM, Runnable runnable) {

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

    private static synchronized void closeAll() {
        SCANNER.close();
        if (EM != null) EM.close();
        if (EM_FACTORY != null) {
            EM_FACTORY.close();
        }
    }
}
