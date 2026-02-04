package com.backcover.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TTSRequest {
    
    @NotBlank(message = "Text is required")
    @Size(max = 500, message = "Text must not exceed 500 characters")
    private String text;
}