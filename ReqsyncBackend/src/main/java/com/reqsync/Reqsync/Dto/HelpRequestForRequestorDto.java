package com.reqsync.Reqsync.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class HelpRequestForRequestorDto {

    @Null(message = "ID should not be provided.") // Allows ID to be null
    private Long id;

    @Null(message = "Name is required.")
    @Size(max = 50, message = "Name must be less than 50 characters.")
    private String name;

    @Null(message = "Phone number is required.")
    @Size(max = 15, message = "Phone number must be less than 15 characters.")
    private String phone;

    @Null(message = "Area is required.")
    @Size(max = 100, message = "Area name must be less than 100 characters.")
    private String area;

    @NotBlank(message = "Help type is required.")
    @Size(max = 100, message = "Help type must be less than 100 characters.")
    private String helpType;

    @NotBlank(message = "Message is required.")
    private String message; // LOB data to handle large text

    @Null(message = "Resolved status should not be provided.") // Allows resolved status to be null
    private boolean isResolved; // Requestor only sees this field
}
