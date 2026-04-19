package com.fishdex.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class UserStatsResponse {

    private int totalCaptures;
    private CaptureResponse biggestCatch;
    private Double totalWeight;
    private Map<String, Long> capturesBySpecies;
    private String mostActiveMonth; // format "2026-04"
    private int joinedGroupsCount;
}
