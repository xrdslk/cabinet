/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.server.handler;

import com.qibingtech.miaoku.cabinet.constant.Constants;
import com.qibingtech.miaoku.cabinet.server.connertor.CabinetConnertor;
import com.qibingtech.miaoku.cabinet.server.connertor.impl.CabinetConnertorImpl;
import com.qibingtech.miaoku.cabinet.server.model.MsgWrapper;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import com.qibingtech.miaoku.cabinet.server.proxy.MsgProxy;
import com.qibingtech.miaoku.cabinet.util.CabinetUtils;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: cabinet
 *
 * @description: handler
 *
 * @author: xrds
 *@Sharable 全局handler 可复用
 * @create: 2020-07-04 23:48
 **/
@Sharable
public class CabinetServerHandler extends ChannelInboundHandlerAdapter {
  private final static Logger log = LoggerFactory.getLogger(CabinetServerHandler.class);

  private CabinetConnertorImpl connertor = null;
  private MsgProxy proxy = null;

  public CabinetServerHandler(MsgProxy proxy,  CabinetConnertorImpl connertor) {
    this.connertor = connertor;
    this.proxy = proxy;
  }

  /**
   * Netty应用心跳和重连的整个过程 客户端连接服务端 在客户端的的ChannelPipeline中加入一个比较特殊的IdleStateHandler，设置一下客户端的写空闲时间，例如5s
   * 当客户端的所有ChannelHandler中4s内没有write事件，则会触发userEventTriggered方法（上文介绍过）
   * 我们在客户端的userEventTriggered中对应的触发事件下发送一个心跳包给服务端，检测服务端是否还存活，防止服务端已经宕机，客户端还不知道
   * 同样，服务端要对心跳包做出响应，其实给客户端最好的回复就是“不回复”，这样可以服务端的压力，假如有10w个空闲Idle的连接，那么服务端光发送心跳回复，则也是费事的事情，那么怎么才能告诉客户端它还活着呢，其实很简单，因为5s服务端都会收到来自客户端的心跳信息，那么如果10秒内收不到，服务端可以认为客户端挂了，可以close链路
   * 假如服务端因为什么因素导致宕机的话，就会关闭所有的链路链接，所以作为客户端要做的事情就是短线重连
   *
   *
   * @param ctx
   * @param o
   * @throws Exception
   */
  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object o) throws Exception {
    String sessionId = ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).get();
    //发送心跳包
    if (o instanceof IdleStateEvent && ((IdleStateEvent) o).state().equals(IdleState.WRITER_IDLE)) {
      //ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(System.currentTimeMillis());
      if(StringUtils.isNotEmpty(sessionId)){
        CabinetMsgProto.CabinetMsg.Builder builder = CabinetMsgProto.CabinetMsg.newBuilder();
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
      Long currentTime = System.currentTimeMillis();

      if(lastTime == null || ( (currentTime - lastTime)/1000 >= Constants.ImserverConfig.PING_TIME_OUT))
      {
        connertor.close(ctx);
      }
      //ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(null);
    }
  }


  @Override
  public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
    try {
      if (o instanceof CabinetMsgProto.CabinetMsg) {
        CabinetMsgProto.CabinetMsg message = (CabinetMsgProto.CabinetMsg) o;
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
      } else {
        log.warn("CabinetServerHandler channelRead message is not proto.");
      }
    } catch (Exception e) {
      log.error("CabinetServerHandler channerRead error.", e);
      throw e;
    }
  }

  public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
    log.info("CabinetServerHandler  join from "+ CabinetUtils.getRemoteAddress(ctx)+" nid:" + ctx.channel().id().asShortText());
  }

  public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
    log.debug("CabinetServerHandler Disconnected from {" +ctx.channel().remoteAddress()+"--->"+ ctx.channel().localAddress() + "}");
  }

  public void channelActive(ChannelHandlerContext ctx) throws Exception {
    super.channelActive(ctx);
    log.debug("CabinetServerHandler channelActive from (" + CabinetUtils.getRemoteAddress(ctx) + ")");
  }

  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    super.channelInactive(ctx);
    log.debug("CabinetServerHandler channelInactive from (" + CabinetUtils.getRemoteAddress(ctx) + ")");
    String sessionId = connertor.getChannelSessionId(ctx);
    receiveMessages(ctx,new MsgWrapper(MsgWrapper.MessageProtocol.CLOSE, sessionId,null, null));
  }

  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    log.warn("CabinetServerHandler (" + CabinetUtils.getRemoteAddress(ctx) + ") -> Unexpected exception from downstream." + cause);
  }





  /**
   * to send  message
   *
   * @param hander
   * @param wrapper
   */
  private void receiveMessages(ChannelHandlerContext hander, MsgWrapper wrapper) {
    //设置消息来源为socket
    wrapper.setSource(Constants.ImserverConfig.SOCKET);
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
