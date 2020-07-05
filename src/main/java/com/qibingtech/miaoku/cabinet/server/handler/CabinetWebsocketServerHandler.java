/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.server.handler;

import com.qibingtech.miaoku.cabinet.constant.Constants;
import com.qibingtech.miaoku.cabinet.server.connertor.impl.CabinetConnertorImpl;
import com.qibingtech.miaoku.cabinet.server.model.MsgWrapper;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import com.qibingtech.miaoku.cabinet.server.proxy.MsgProxy;
import com.qibingtech.miaoku.cabinet.util.CabinetUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: cabinet
 *
 * @description: websocket server handler
 *
 * @author: xrds
 *
 * @create: 2020-07-05 22:08
 **/
public class CabinetWebsocketServerHandler extends SimpleChannelInboundHandler<CabinetMsgProto.CabinetMsg> {

  private final static Logger log = LoggerFactory.getLogger(CabinetWebsocketServerHandler.class);
  private CabinetConnertorImpl connertor = null;
  private MsgProxy proxy = null;

  public CabinetWebsocketServerHandler(MsgProxy proxy, CabinetConnertorImpl connertor) {
    this.connertor = connertor;
    this.proxy = proxy;
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object o) throws Exception {
    String sessionId = ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).get();
    //发送心跳包
    if (o instanceof IdleStateEvent && ((IdleStateEvent) o).state().equals(IdleState.WRITER_IDLE)) {
      if(StringUtils.isNotEmpty(sessionId)){
       CabinetMsgProto.CabinetMsg.Builder builder =CabinetMsgProto.CabinetMsg.newBuilder();
        builder.setCmd(Constants.CmdType.HEARTBEAT);
        builder.setMsgtype(Constants.ProtobufType.SEND);
        ctx.channel().writeAndFlush(builder);
      }
      log.debug(IdleState.WRITER_IDLE +"... from "+sessionId+"-->"+ctx.channel().remoteAddress()+" nid:" +ctx.channel().id().asShortText());
    }

    //如果心跳请求发出70秒内没收到响应，则关闭连接
    if ( o instanceof IdleStateEvent && ((IdleStateEvent) o).state().equals(IdleState.READER_IDLE)){
      log.debug(IdleState.READER_IDLE +"... from "+sessionId+" nid:" +ctx.channel().id().asShortText());
      Long lastTime = (Long) ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).get();
      if(lastTime == null || ((System.currentTimeMillis() - lastTime)/1000>= Constants.ImserverConfig.PING_TIME_OUT))
      {
        connertor.close(ctx);
      }
      //ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(null);
    }
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx,CabinetMsgProto.CabinetMsg message)
      throws Exception {
    try {
      String sessionId = connertor.getChannelSessionId(ctx);
      // inbound
      if (message.getMsgtype() == Constants.ProtobufType.SEND) {
        ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(System.currentTimeMillis());
        MsgWrapper wrapper = proxy.convertToMsgWrapper(sessionId, message);
        if (wrapper != null)
          receiveMessages(ctx, wrapper);
      }
      // outbound
      if (message.getMsgtype() == Constants.ProtobufType.REPLY) {
        MsgWrapper wrapper = proxy.convertToMsgWrapper(sessionId, message);
        if (wrapper != null)
          receiveMessages(ctx, wrapper);
      }
    } catch (Exception e) {
      log.error("CabinetWebsocketServerHandler channerRead error.", e);
      throw e;
    }

  }


  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    log.info("CabinetWebsocketServerHandler  join from "+ CabinetUtils.getRemoteAddress(ctx)+" nid:" + ctx.channel().id().asShortText());
  }

  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    log.debug("CabinetWebsocketServerHandler Disconnected from {" +ctx.channel().remoteAddress()+"--->"+ ctx.channel().localAddress() + "}");
  }

  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    log.debug("CabinetWebsocketServerHandler channelActive from (" + CabinetUtils.getRemoteAddress(ctx) + ")");
  }

  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    log.debug("CabinetWebsocketServerHandler channelInactive from (" + CabinetUtils.getRemoteAddress(ctx) + ")");
    String sessionId = connertor.getChannelSessionId(ctx);
    receiveMessages(ctx,new MsgWrapper(MsgWrapper.MessageProtocol.CLOSE, sessionId,null, null));
  }

  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.warn("CabinetWebsocketServerHandler (" + CabinetUtils.getRemoteAddress(ctx) + ") -> Unexpected exception from downstream." + cause);
  }





  /**
   * to send message
   *
   * @param hander
   * @param wrapper
   */
  private void receiveMessages(ChannelHandlerContext hander, MsgWrapper wrapper) {
    //设置消息来源为Websocket
    wrapper.setSource(Constants.ImserverConfig.WEBSOCKET);
    if (wrapper.isConnect()) {
      connertor.connect(hander, wrapper);
    } else if (wrapper.isClose()) {
      connertor.close(hander,wrapper);
    } else if (wrapper.isHeartbeat()) {
      connertor.heartbeatToClient(hander,wrapper);
    }else if (wrapper.isGroup()) {
      connertor.pushGroupMessage(wrapper);
    }else if (wrapper.isSend()) {
      connertor.pushMessage(wrapper);
    } else if (wrapper.isReply()) {
      connertor.pushMessage(wrapper.getSessionId(),wrapper);
    }
  }

}
