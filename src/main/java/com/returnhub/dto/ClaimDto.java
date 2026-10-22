package com.returnhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class ClaimDto {

    private Long itemId;

    @NotBlank(message = "Detailed proof of ownership is required")
    @Size(min = 10, max = 1500, message = "Proof description must be between 10 and 1500 characters")
    private String proofDescription;

    @NotNull(message = "Date when the item was lost/belonged to you is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate dateLost;

    @NotBlank(message = "Contact phone number is required")
    private String contactPhone;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Please enter a valid email address")
    private String contactEmail;

    @Size(max = 1000, message = "Message must not exceed 1000 characters")
    private String message;

    public ClaimDto() {
    }

    // Getters and Setters
    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public String getProofDescription() {
        return proofDescription;
    }

    public void setProofDescription(String proofDescription) {
        this.proofDescription = proofDescription;
    }

    public LocalDate getDateLost() {
        return dateLost;
    }

    public void setDateLost(LocalDate dateLost) {
        this.dateLost = dateLost;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
