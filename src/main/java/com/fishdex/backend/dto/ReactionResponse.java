package com.fishdex.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReactionResponse {
    private String type;
    private long count;
    private boolean reacted;
}
