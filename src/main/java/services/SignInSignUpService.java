package services;

import customExceptions.SuchEntityExistsException;
import entities.Client;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.Scanner;

public class SignInSignUpService {

    void register(EntityManager EM, Scanner SCANNER) {

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

            System.err.println(new SuchEntityExistsException().getMessage());
            System.out.println("Try one more time");
            register(EM, SCANNER);

        } else {
            BankService.performTransaction(() -> EM.persist(new Client(
                    name,
                    surname,
                    age,
                    login,
                    password
            )));

            System.out.println("A new client was created.");

            tryToSignIn(EM, SCANNER);
        }
    }

    void tryToSignIn(EntityManager EM, Scanner SCANNER) {
        String login;
        String password;
        do {

            System.out.println("Enter login");
            login = SCANNER.nextLine();

            System.out.println("Enter password");
            password = SCANNER.nextLine();

        } while (!logIn(EM, login, password));
    }

    private boolean logIn(EntityManager EM, String login, String password) {
        TypedQuery<Client> query = EM.createQuery(
                "SELECT c FROM Client c WHERE c.login = :login AND c.password = :password", Client.class);
        query.setParameter("login", login);
        query.setParameter("password", password);

        try {
            BankService.setCurrentClient(query.getSingleResult());
            return true;
        } catch (NoResultException e) {
            System.err.println("No such user.");
            BankService.makeBankServiceStarted();
        }

        return false;
    }

}
