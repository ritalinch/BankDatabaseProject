package entities;

import services.MainService;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    private final List<Transaction> historyOfTransactions = new ArrayList<>();

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "currency")
    @Enumerated(EnumType.ORDINAL)

    private Currency currency;
    private BigDecimal balance = new BigDecimal("0.00");

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    public Account() {
    }

    public Account(Currency currency, Client client) {
        this.currency = currency;
        this.balance = new BigDecimal("0");
        this.client = client;
    }

    public void retainBalance(BigDecimal value) {
        if (balance.subtract(value).compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Not enough money.");
        } else if (value.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Illegal value for transaction");
        } else {
            balance = balance.subtract(value);
            addTransaction(BigDecimal.ZERO.subtract(value),
                    "Balance was retained in " + getCurrency());
        }
    }

    public void topUpBalance(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            System.err.println("Illegal value.");
        } else {
            balance = balance.add(value);
            addTransaction(value, "Balance was topped up in " + getCurrency());
        }
    }

    private void addTransaction(BigDecimal value, String message) {
        Transaction transaction = new Transaction(this, value, balance);
        historyOfTransactions.add(transaction);
        MainService.performTransaction(MainService.em(), () -> MainService.em().persist(transaction));
        System.out.println(message);
    }

    public Long getId() {
        return id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

}
