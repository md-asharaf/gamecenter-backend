package com.adnibog.gamecenter.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UploadJobDto {
    private String id;
    private String projectId;
    private String status;
    private String errorMessage;
}
