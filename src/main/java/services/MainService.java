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

        System.out.println("""
                To sign in type '1'
                To sign up type '2'
                -------->
                """);

        switch (SCANNER.nextLine()) {
            case "1" -> SignInSignUpService.tryToSignIn(EM, SCANNER);
            case "2" -> SignInSignUpService.register(EM, SCANNER);
            default -> start();
        }

        closeAll();

    }

    static void doActions() {
        boolean logout = false;
        while(!logout) {
            System.out.println("""
                    1. To top up enter _____________________________________1__
                    2. To retain enter _____________________________________2__
                    4. To see total balance in UAH enter ___________________3__
                    5. To create a new account enter _______________________4__
                    6. To transfer money from one account to another enter _5__
                    7. To logout enter _____________________________________6__
                    """);

            String ans = SCANNER.nextLine();

            switch(ans) {
                case "1" -> BankService.topUp(SCANNER);
                case "2" -> BankService.retain(SCANNER);
                case "3" -> BankService.getTotalBalanceInUAH(EM);
                case "4" -> BankService.createAccount(SCANNER, EM);
                case "5" -> BankService.transfer(SCANNER);
                case "6" -> logout = true;
            }
        }
    }

    static void performTransaction(EntityManager EM, Runnable runnable) {

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
