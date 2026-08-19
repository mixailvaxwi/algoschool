package com.algoschool.submission.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SubmissionRequest {
        @NotBlank(message = "Ответ не может быть пустым")
        private String payload;
}