package services;

import entities.Currency;
import entities.Transaction;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SearchService {

    private static final CriteriaBuilder criteriaBuilder = MainService.em().getCriteriaBuilder();
    private static final CriteriaQuery<Transaction> criteriaQuery = criteriaBuilder.createQuery(Transaction.class);
    private static final Root<Transaction> root = criteriaQuery.from(Transaction.class);
    private static CriteriaQuery<Transaction> res = criteriaBuilder.createQuery(Transaction.class);

    static void searchThroughTransactionsByDate() {
        System.out.println("Enter date 'from' in 'dd/MM/yyyy' format");
        String from = MainService.scanner().nextLine();

        System.out.println("Enter date 'to' in 'dd/MM/yyyy' format");
        String to = MainService.scanner().nextLine();

        try {
            Date fromDate = new SimpleDateFormat("dd/MM/yyyy").parse(from);
            Date toDate = new SimpleDateFormat("dd/MM/yyyy").parse(to);

            res = criteriaQuery
                    .select(root)
                    .where(criteriaBuilder.between(root.get("timestamp"), fromDate, toDate));

            TypedQuery<Transaction> typedQuery = MainService.em().createQuery(res);
            List<Transaction> list = typedQuery.getResultList();

            list.forEach(System.out::println);

        } catch (ParseException e) {
            System.err.println("Illegal argument");
        }

    }

    static void searchThroughTransactionsByValue() {
        try {
            System.out.println("Enter currency of transactions: ");
            Currency currency = Currency.valueOf(MainService.scanner().nextLine());

            System.out.println("Enter minimum amount: ");
            BigDecimal min = new BigDecimal(MainService.scanner().nextLine());

            System.out.println("Enter maximum amount: ");
            BigDecimal max = new BigDecimal(MainService.scanner().nextLine());

            res = criteriaQuery
                    .select(root)
                    .where(criteriaBuilder.between(root.get("amount"), min, max))
                    .where(criteriaBuilder.equal(root.get("currency"), currency));
            TypedQuery<Transaction> typedQuery = MainService.em().createQuery(res);
            List<Transaction> list = typedQuery.getResultList();

            list.forEach(System.out::println);

        } catch (IllegalArgumentException | NullPointerException e) {
            e.printStackTrace();
        }
    }
}
