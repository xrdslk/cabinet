/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.server.model;

import java.io.Serializable;

/**
 * @program: cabinet
 *
 * @description: 消息包装信息
 *
 * @author: xrds
 *
 * @create: 2020-07-05 08:42
 **/
public class MsgWrapper implements Serializable {
  private static final long serialVersionUID = -1621908031090659163L;
  private MessageProtocol protocol;
  private String sessionId;//请求人
  private String reSessionId;//接收人
  private int source;//来源 用于区分是websocket还是socekt
  private Object body;

  private MsgWrapper() {
  }

  public MsgWrapper(MessageProtocol protocol, String sessionId,String reSessionId, Object body) {
    this.protocol = protocol;
    this.sessionId = sessionId;
    this.reSessionId = reSessionId;
    this.body = body;
  }

  public enum MessageProtocol {
    CONNECT, CLOSE, HEART_BEAT, SEND,GROUP, NOTIFY, REPLY, ON_LINE,OFF_LINE
  }

  public boolean isGroup() {
    return MessageProtocol.GROUP.equals(this.protocol);
  }

  public boolean isConnect() {
    return MessageProtocol.CONNECT.equals(this.protocol);
  }

  public boolean isClose() {
    return MessageProtocol.CLOSE.equals(this.protocol);
  }

  public boolean isHeartbeat() {
    return MessageProtocol.HEART_BEAT.equals(this.protocol);
  }

  public boolean isSend() {
    return MessageProtocol.SEND.equals(this.protocol);
  }

  public boolean isNotify() {
    return MessageProtocol.NOTIFY.equals(this.protocol);
  }

  public boolean isReply() {
    return MessageProtocol.REPLY.equals(this.protocol);
  }

  public boolean isOnline() {
    return MessageProtocol.ON_LINE.equals(this.protocol);
  }

  public boolean isOffline() {
    return MessageProtocol.OFF_LINE.equals(this.protocol);
  }



  public void setProtocol(MessageProtocol protocol) {
    this.protocol = protocol;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public Object getBody() {
    return body;
  }

  public void setBody(Object body) {
    this.body = body;
  }

  public String getReSessionId() {
    return reSessionId;
  }

  public void setReSessionId(String reSessionId) {
    this.reSessionId = reSessionId;
  }

  public int getSource() {
    return source;
  }

  public void setSource(int source) {
    this.source = source;
  }


}
