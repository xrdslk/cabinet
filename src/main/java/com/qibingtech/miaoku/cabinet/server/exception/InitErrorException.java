/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.server.exception;

/**
 * @program: cabinet
 *
 * @description: 初始化错误异常
 *
 * @author: xrds
 *
 * @create: 2020-07-05 14:44
 **/
public class InitErrorException extends RuntimeException{


  private int errorCode = 1;

  private String errorMsg;

  protected InitErrorException() {

  }

  public InitErrorException(String errorMsg, Throwable e) {
    super(errorMsg, e);
    this.errorMsg = errorMsg;
  }

  public InitErrorException(String errorMsg) {
    super(errorMsg);
    this.errorMsg = errorMsg;
  }
}
