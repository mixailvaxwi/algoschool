package com.algoschool.ejudge.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EjudgeRunStatusResponse {
    private boolean ok;
    private Result result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private Run run;

        @JsonProperty("compiler_output")
        private String compilerOutput;

        @JsonProperty("tests")
        private JsonNode tests;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Run {
        @JsonProperty("run_id")
        private Integer runId;

        private Integer status;
    }
}