package com.example.docDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QAResponseDTO {

    private Long documentId;

    private String filename;

    private String snippet;

    private String author;

    private LocalDate uploadTimestamp;
}