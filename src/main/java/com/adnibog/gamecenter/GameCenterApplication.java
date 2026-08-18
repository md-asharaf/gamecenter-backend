package com.adnibog.gamecenter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class GameCenterApplication {

  public static void main(String[] args) {
    SpringApplication.run(GameCenterApplication.class, args);
  }

}
