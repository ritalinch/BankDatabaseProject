package services;

import entities.Client;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

public class SignInSignUpService {

    static void register() {

        System.out.println("Enter your name:");
        String name = MainService.scanner().nextLine();

        System.out.println("Enter your surname:");
        String surname = MainService.scanner().nextLine();

        System.out.println("Enter your age");
        Integer age = Integer.parseInt(MainService.scanner().nextLine());

        System.out.println("Create login:");
        String login = MainService.scanner().nextLine();

        System.out.println("Create password:");
        String password = MainService.scanner().nextLine();

        TypedQuery<Long> query = MainService.em().createQuery(
                "SELECT COUNT(c) FROM Client c WHERE c.login = :login AND c.password = :password", Long.class);
        query.setParameter("login", login);
        query.setParameter("password", password);

        if (query.getSingleResult() != 0L) {
            System.err.println("""
                    Entity with this data already exists.
                    You are not able to create more.
                    Try one more time.
                    """);
            register();

        } else {
            MainService.performTransaction(MainService.em(), () -> MainService.em().persist(new Client(
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

    static void tryToSignIn() {
        System.out.println("Enter login");
        String login = MainService.scanner().nextLine();

        System.out.println("Enter password");
        String password = MainService.scanner().nextLine();

        logIn(login, password);
    }

    private static void logIn(String login, String password) {
        TypedQuery<Client> query = MainService.em().createQuery(
                "SELECT c FROM Client c WHERE c.login = :login AND c.password = :password", Client.class);
        query.setParameter("login", login);
        query.setParameter("password", password);

        try {
            BankService.setCurrentClient(query.getSingleResult());
            MainService.doActions();
        } catch (NoResultException e) {
            System.err.println("No such user.");
            MainService.start();
        }
    }

}
