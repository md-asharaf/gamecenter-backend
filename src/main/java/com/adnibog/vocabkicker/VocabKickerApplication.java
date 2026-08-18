package com.adnibog.vocabkicker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class VocabKickerApplication {

  public static void main(String[] args) {
    SpringApplication.run(VocabKickerApplication.class, args);
  }

}
