package com.backcover.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TTSResponse {
    private String text;
    private String audioBase64;
    private String audioFormat;
}