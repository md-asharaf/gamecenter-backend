package com.adnibog.gamecenter.dto.request;

import lombok.Data;

@Data
public class CreateProjectRequest {
  private String name;
  private String field1Label;
  private String field2Label;
  private String field3Label;
}
