package com.learning.banking.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank(message = "user id must not be blank")
        String userId,

        @NotBlank(message = "currency must not be blank")
        @Size(min = 2, max = 3, message = "Currency must be a 3-letter ISO code e.g. USD")
        String currency
) {

}
