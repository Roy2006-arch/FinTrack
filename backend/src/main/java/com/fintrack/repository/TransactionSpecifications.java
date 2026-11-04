package com.fintrack.repository;
import com.fintrack.dto.TransactionFilter;
import com.fintrack.entity.Transaction;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.*;

public final class TransactionSpecifications {
    private TransactionSpecifications() {}
    public static Specification<Transaction> matching(Long uid, TransactionFilter f) {
        return (root, query, cb) -> {
            if (query != null && query.getResultType() != Long.class && query.getResultType() != long.class) root.fetch("category");
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("user").get("id"), uid));
            if (f.categoryId() != null) predicates.add(cb.equal(root.get("category").get("id"), f.categoryId()));
            if (f.type() != null) predicates.add(cb.equal(root.get("type"), f.type()));
            if (f.paymentMethod() != null) predicates.add(cb.equal(root.get("paymentMethod"), f.paymentMethod()));
            if (f.start() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), f.start()));
            if (f.end() != null) predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), f.end()));
            if (f.search() != null && !f.search().isBlank()) {
                String term = "%" + f.search().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("description")), term, '!'), cb.like(cb.lower(root.get("category").get("name")), term, '!')));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }
}
