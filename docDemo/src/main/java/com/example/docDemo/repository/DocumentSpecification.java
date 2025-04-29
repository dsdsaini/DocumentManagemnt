package com.example.docDemo.repository;

import com.example.docDemo.entity.Document;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DocumentSpecification {

    public static Specification<Document> hasAuthor(String author) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(author)) { // Check if author is null or empty/whitespace
                return criteriaBuilder.conjunction(); // No filter applied, always true
            }
            // Case-insensitive search for author
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), "%" + author.toLowerCase() + "%");
        };
    }

    public static Specification<Document> hasContentType(String contentType) {
        return (root, query, criteriaBuilder) -> {
            if (!StringUtils.hasText(contentType)) {
                return criteriaBuilder.conjunction();
            }
            // Exact match (case-insensitive often preferred for content types too)
            return criteriaBuilder.equal(criteriaBuilder.lower(root.get("contentType")), contentType.toLowerCase());
        };
    }

    public static Specification<Document> uploadDate(LocalDate uploadTimestamp) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (uploadTimestamp != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("uploadTimestamp"), uploadTimestamp));
            }
            if (predicates.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}