/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.server.exception;

/**
 * @program: cabinet
 *
 * @description: 推送异常
 *
 * @author: xrds
 *
 * @create: 2020-07-05 14:01
 **/
public class PushException extends IllegalStateException {

  private static final long serialVersionUID = -4953949710626671131L;

  public PushException() {
    super();
  }

  public PushException(String message) {
    super(message);
  }

  public PushException(Throwable throwable) {
    super(throwable);
  }

  public PushException(String message, Throwable throwable) {
    super(message, throwable);
  }

}
