package com.gmail.clarkin200.LayeredMonolith.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateProductDto(
        @NotBlank(message = "name can't be null")
        String name,
        @NotNull(message = "price can't be null")
        @Positive(message = "can't be negative")
        Float price) {
}
