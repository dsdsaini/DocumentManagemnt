package com.example.docDemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ErrorResponseDTO {
    private LocalDate timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
