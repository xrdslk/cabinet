/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.client.handler;

import com.qibingtech.miaoku.cabinet.server.handler.CabinetServerHandler;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: cabinet
 *
 * @description: 客户端handler
 *
 * @author: xrds
 *
 * @create: 2020-07-05 20:38
 **/
public class CabinetClientHandler extends ChannelInboundHandlerAdapter {

  private final static Logger log = LoggerFactory.getLogger(CabinetServerHandler.class);

  @Override
  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    CabinetMsgProto.CabinetMsg cabinetMsg = CabinetMsgProto.CabinetMsg.newBuilder().setDeviceId("001").setCmd(1).setMsgtype(1).buildPartial();
    ctx.writeAndFlush(cabinetMsg);
  }






}
