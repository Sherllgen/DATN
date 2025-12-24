package com.project.evgo.user.request;

import com.project.evgo.sharedkernel.enums.UserGender;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateProfileRequest(
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,

        @Pattern(regexp = "^(\\+84|0)[0-9]{9}$", message = "Invalid phone number format")
        String phoneNumber,

        UserGender gender,

        @Past(message = "Birthday must be in the past")
        LocalDate birthday
) {

}
