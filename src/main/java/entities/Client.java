package entities;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "clients")
public class Client {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String surname;

    private Integer age;

    private String login;

    private String password;

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL)
    private Set<Account> accounts = new HashSet<>();

    public Client() {}

    public Client(String name, String surname, Integer age) {
        this.name = name;
        this.surname = surname;
        this.age = age;
    }

    public void addAccount(Account account) {
        if(!accounts.contains(account)) {
            accounts.add(account);
            // account.setClient(this);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
}
