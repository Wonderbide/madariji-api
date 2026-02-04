package com.backcover.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class UpdateProgressRequest {
    @NotNull(message = "Page number cannot be null")
    @Min(value = 1, message = "Page number must be at least 1") // Ou 0 si les pages sont indexées à partir de 0
    private Integer pageNumber;

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }
}