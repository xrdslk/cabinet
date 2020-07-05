/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet;

import com.qibingtech.miaoku.cabinet.server.CabinetServer;
import org.springframework.context.annotation.Bean;

/**
 * @program: cabinet
 *
 * @description: 创建配置类
 *
 * @author: xrds
 *
 * @create: 2020-07-05 20:14
 **/
public class InitConfig {

  @Bean(initMethod = "init",destroyMethod = "destroy")
  public CabinetServer initCabinetServer() {
    return new CabinetServer(2000);
  }

}
