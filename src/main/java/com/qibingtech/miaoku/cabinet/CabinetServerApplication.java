package com.qibingtech.miaoku.cabinet;

import com.qibingtech.miaoku.cabinet.server.CabinetServer;
import com.qibingtech.miaoku.cabinet.server.CabinetWebsocketServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CabinetServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(CabinetServerApplication.class, args);
    try {
      new CabinetServer(2000).init();
      new CabinetWebsocketServer(2048).init();
    } catch (Exception e) {
      e.printStackTrace();
    }


  }

}
