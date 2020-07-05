/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.server.connertor.impl;

import com.qibingtech.miaoku.cabinet.constant.Constants;
import com.qibingtech.miaoku.cabinet.server.connertor.CabinetConnertor;
import com.qibingtech.miaoku.cabinet.server.exception.PushException;
import com.qibingtech.miaoku.cabinet.server.group.CabinetChannelGroup;
import com.qibingtech.miaoku.cabinet.server.model.MsgWrapper;
import com.qibingtech.miaoku.cabinet.server.model.Session;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import com.qibingtech.miaoku.cabinet.server.proxy.MsgProxy;
import com.qibingtech.miaoku.cabinet.server.session.impl.SessionManagerImpl;
import com.qibingtech.miaoku.cabinet.webserver.DwrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @program: cabinet
 *
 * @description: 柜连接接口实现
 *
 * @author: xrds
 *
 * @create: 2020-07-05 08:55
 **/
@Component("cabinetConnertor")
public class CabinetConnertorImpl implements CabinetConnertor {

  private final static Logger log = LoggerFactory.getLogger(CabinetConnertorImpl.class);

  private SessionManagerImpl sessionManager;
  private MsgProxy proxy;

  public void setSessionManager(SessionManagerImpl sessionManager) {
    this.sessionManager = sessionManager;
  }
  public void setProxy(MsgProxy proxy) {
    this.proxy = proxy;
  }


  @Override
  public void heartbeatToClient(ChannelHandlerContext hander,MsgWrapper wrapper) {
    //设置心跳响应时间
    hander.channel().attr(Constants.SessionConfig.SERVER_SESSION_HEARBEAT).set(System.currentTimeMillis());
  }

  @Override
  public void pushGroupMessage(MsgWrapper wrapper)
      throws RuntimeException {
    //这里判断群组ID 是否存在 并且该用户是否在群组内
    CabinetChannelGroup.broadcast(wrapper.getBody());
    DwrUtil.sedMessageToAll((CabinetMsgProto.CabinetMsg)wrapper.getBody());
    proxy.saveOnlineMessageToDB(wrapper);
  }

  @Override
  public void pushMessage(MsgWrapper wrapper) throws RuntimeException {
    try {
      //sessionManager.send(wrapper.getSessionId(), wrapper.getBody());
      Session session = sessionManager.getSession(wrapper.getSessionId());
      /*
       * 服务器集群时，可以在此
       * 判断当前session是否连接于本台服务器，如果是，继续往下走，如果不是，将此消息发往当前session连接的服务器并 return
       * if(session!=null&&!session.isLocalhost()){//判断当前session是否连接于本台服务器，如不是
       * //发往目标服务器处理
       * return;
       * }
       */
      if (session != null) {
        boolean result = session.write(wrapper.getBody());
        return ;
      }
    } catch (Exception e) {
      log.error("connector pushMessage  Exception.", e);
      throw new RuntimeException(e.getCause());
    }
  }


  @Override
  public void pushMessage(String sessionId,MsgWrapper wrapper) throws RuntimeException{
    //判断是不是无效用户回复    
    if(!sessionId.equals(Constants.ImserverConfig.REBOT_SESSIONID)){//判断非机器人回复时验证
      Session session = sessionManager.getSession(sessionId);
      if (session == null) {
        throw new RuntimeException(String.format("session %s is not exist.", sessionId));
      }
    }
    try {
      ///取得接收人 给接收人写入消息
      Session responseSession = sessionManager.getSession(wrapper.getReSessionId());
      if (responseSession != null && responseSession.isConnected() ) {
        boolean result = responseSession.write(wrapper.getBody());
        if(result){
          proxy.saveOnlineMessageToDB(wrapper);
        }else{
          proxy.saveOfflineMessageToDB(wrapper);
        }
        return;
      }else{
        proxy.saveOfflineMessageToDB(wrapper);
      }
    } catch (PushException e) {
      log.error("connector send occur PushException.", e);

      throw new RuntimeException(e.getCause());
    } catch (Exception e) {
      log.error("connector send occur Exception.", e);
      throw new RuntimeException(e.getCause());
    }

  }
  @Override
  public boolean validateSession(MsgWrapper wrapper) throws RuntimeException {
    try {
      return sessionManager.exist(wrapper.getSessionId());
    } catch (Exception e) {
      log.error("connector validateSession Exception!", e);
      throw new RuntimeException(e.getCause());
    }
  }

  @Override
  public void close(ChannelHandlerContext hander,MsgWrapper wrapper) {
    String sessionId = getChannelSessionId(hander);
    if (StringUtils.isNotBlank(sessionId)) {
      close(hander);
      log.warn("connector close channel sessionId -> " + sessionId + ", ctx -> " + hander.toString());
    }
  }

  @Override
  public void close(ChannelHandlerContext hander) {
    String sessionId = getChannelSessionId(hander);
    try {
      String nid = hander.channel().id().asShortText();
      Session session = sessionManager.getSession(sessionId);
      if (session != null) {
        sessionManager.removeSession(sessionId,nid);
        CabinetChannelGroup.remove(hander.channel());
        log.info("connector close sessionId -> " + sessionId + " success " );
      }
    } catch (Exception e) {
      log.error("connector close sessionId -->"+sessionId+"  Exception.", e);
      throw new RuntimeException(e.getCause());
    }
  }

  @Override
  public void close(String sessionId) {
    try {
      Session session = sessionManager.getSession(sessionId);
      if (session != null) {
        sessionManager.removeSession(sessionId);
        List<Channel> list = session.getSessionAll();
        for(Channel ch:list){
          CabinetChannelGroup.remove(ch);
        }
        log.info("connector close sessionId -> " + sessionId + " success " );
      }
    } catch (Exception e) {
      log.error("connector close sessionId -->"+sessionId+"  Exception.", e);
      throw new RuntimeException(e.getCause());
    }
  }

  @Override
  public void connect(ChannelHandlerContext ctx, MsgWrapper wrapper) {
    try {
      String sessionId = wrapper.getSessionId();
      String sessionId0 = getChannelSessionId(ctx);
      //当sessionID存在或者相等  视为同一用户重新连接
      if (StringUtils.isNotEmpty(sessionId0) || sessionId.equals(sessionId0)) {
        log.info("connector reconnect sessionId -> " + sessionId + ", ctx -> " + ctx.toString());
        pushMessage(proxy.getReConnectionStateMsg(sessionId0));
      } else {
        log.info("connector connect sessionId -> " + sessionId + ", sessionId0 -> " + sessionId0 + ", ctx -> " + ctx.toString());
        sessionManager.createSession(wrapper, ctx);
        setChannelSessionId(ctx, sessionId);
        log.info("create channel attr sessionId " + sessionId + " successful, ctx -> " + ctx.toString());
      }
    } catch (Exception e) {
      log.error("connector connect  Exception.", e);
    }
  }

  @Override
  public boolean exist(String sessionId) throws Exception {
    return sessionManager.exist(sessionId);
  }
  @Override
  public String getChannelSessionId(ChannelHandlerContext ctx) {
    return ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).get();
  }

  private void setChannelSessionId(ChannelHandlerContext ctx, String sessionId) {
    ctx.channel().attr(Constants.SessionConfig.SERVER_SESSION_ID).set(sessionId);
  }




}
