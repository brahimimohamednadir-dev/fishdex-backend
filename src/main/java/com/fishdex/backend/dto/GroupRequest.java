package com.fishdex.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Used only by the legacy GroupController (Sprint-1 endpoint).
 * The new multipart create endpoint uses @RequestParam directly.
 */
@Data
public class GroupRequest {

    @NotBlank
    @Size(min = 3, max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    private String visibility = "PUBLIC";
    private String category   = "FRIENDS";
}
