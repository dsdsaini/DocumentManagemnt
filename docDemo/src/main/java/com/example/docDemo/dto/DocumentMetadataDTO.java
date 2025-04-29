package com.example.docDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadataDTO {
    private Long id;

    private String filename;

    private String contentType;

    private String author;

    private LocalDate uploadTimestamp;

}


