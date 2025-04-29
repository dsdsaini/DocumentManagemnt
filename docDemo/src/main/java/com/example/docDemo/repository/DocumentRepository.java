package com.example.docDemo.repository;

import com.example.docDemo.entity.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long>, JpaSpecificationExecutor<Document> {

    @Query(value = "SELECT * FROM files d WHERE UPPER(d.content) LIKE CONCAT('%', UPPER(:keyword), '%')", nativeQuery = true)
    Page<Document> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

}
