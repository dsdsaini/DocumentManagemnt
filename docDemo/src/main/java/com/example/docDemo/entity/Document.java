package com.example.docDemo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;


@Entity
@Table(name = "files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    // Store the content type provided during upload (should be validated)
    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column
    private String author; // Metadata

    @Lob // Large Object
    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content; // Plain text content ONLY
    @CreationTimestamp
    @Column(name = "upload_timestamp", nullable = false, updatable = false)
    private LocalDate uploadTimestamp;
}
