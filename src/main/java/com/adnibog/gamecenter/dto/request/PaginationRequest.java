package com.adnibog.gamecenter.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.Parameter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    @Parameter(description = "Number of items to return", example = "10")
    @Builder.Default
    private int limit = 10;

    @Parameter(description = "Pagination cursor string")
    private String lastEvaluatedKey;

    @Parameter(description = "Search keyword filter")
    private String search;
}
