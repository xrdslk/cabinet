/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.server.session.impl;

import com.qibingtech.miaoku.cabinet.constant.Constants;
import com.qibingtech.miaoku.cabinet.server.group.CabinetChannelGroup;
import com.qibingtech.miaoku.cabinet.server.model.MsgWrapper;
import com.qibingtech.miaoku.cabinet.server.model.Session;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import com.qibingtech.miaoku.cabinet.server.proxy.MsgProxy;
import com.qibingtech.miaoku.cabinet.server.session.SessionManager;
import com.qibingtech.miaoku.cabinet.webserver.DwrUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.directwebremoting.ScriptSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @program: cabinet
 *
 * @description: session 管理实现
 *
 * @author: xrds
 *
 * @create: 2020-07-05 13:14
 **/
@Component("sessionManager")
public class SessionManagerImpl implements SessionManager {

  private final static Logger log = LoggerFactory.getLogger(SessionManagerImpl.class);
  private MsgProxy proxy;

  public void setProxy(MsgProxy proxy) {
    this.proxy = proxy;
  }
  /**
   * The set of currently active Sessions for this Manager, keyed by session
   * identifier.
   */
  protected Map<String, Session> sessions = new ConcurrentHashMap<String, Session>();

  public synchronized void addSession(Session session) {
    if (null == session) {
      return;
    }
    sessions.put(session.getAccount(), session);
    if(session.getSource()!= Constants.ImserverConfig.DWR){
      CabinetChannelGroup.add(session.getSession());
    }
    //全员发送上线消息
    CabinetMsgProto.CabinetMsg model = proxy.getOnLineStateMsg(session.getAccount());
    CabinetChannelGroup.broadcast(model);
    DwrUtil.sedMessageToAll(model);
    log.debug("put a session " + session.getAccount() + " to sessions!");
    log.debug("session size " + sessions.size() );
  }

  public synchronized void updateSession(Session session) {
    session.setUpdateTime(System.currentTimeMillis());
    sessions.put(session.getAccount(), session);
  }

  /**
   * Remove this Session from the active Sessions for this Manager.
   */
  public synchronized void removeSession(String sessionId) {
    try{
      Session session = getSession(sessionId);
      if(session!=null){
        session.closeAll();
        sessions.remove(sessionId);
        CabinetMsgProto.CabinetMsg model = proxy.getOffLineStateMsg(sessionId);
        CabinetChannelGroup.broadcast(model);
        DwrUtil.sedMessageToAll(model);
      }
    }catch(Exception e){

    }
    log.debug("session size " + sessions.size() );
    log.info("system remove the session " + sessionId + " from sessions!");
  }


  public synchronized void removeSession(String sessionId,String nid) {
    try{
      Session session = getSession(sessionId);
      if(session!=null){
        int source = session.getSource();
        if(source==Constants.ImserverConfig.WEBSOCKET || source==Constants.ImserverConfig.DWR){
          session.close(nid);
          //判断没有其它session后 从SessionManager里面移除
          if(session.otherSessionSize()<1){
            sessions.remove(sessionId);
            CabinetMsgProto.CabinetMsg model = proxy.getOffLineStateMsg(sessionId);
            CabinetChannelGroup.broadcast(model);
            //dwr全员消息广播
            DwrUtil.sedMessageToAll(model);
          }
        } else{
          session.close();
          sessions.remove(sessionId);
          CabinetMsgProto.CabinetMsg model = proxy.getOffLineStateMsg(sessionId);
          CabinetChannelGroup.broadcast(model);
          DwrUtil.sedMessageToAll(model);
        }
      }
    }catch(Exception e){

    }finally{


    }
    log.info("remove the session " + sessionId + " from sessions!");
  }

  public Session getSession(String sessionId) {
    return sessions.get(sessionId);
  }

  public Session[] getSessions() {
    return sessions.values().toArray(new Session[0]);
  }

  public Set<String> getSessionKeys() {
    return sessions.keySet();
  }

  public int getSessionCount() {
    return sessions.size();
  }


  @Override
  public  Session createSession(MsgWrapper wrapper, ChannelHandlerContext ctx) {
    String sessionId = wrapper.getSessionId();
    Session session = sessions.get(sessionId);
    if (session != null) {
      log.info("session " + sessionId + " exist!");
      //当链接来源不是同一来源或者 是socket链接，踢掉已经登录的session 
      if(session.getSource()==Constants.ImserverConfig.SOCKET){
        // 如果session已经存在则销毁session
        //从广播组清除
        log.info("sessionId" + session.getNid() +"------------------"+ ctx.channel().id().asShortText()+ "      !");
        CabinetChannelGroup.remove(session.getSession());
        session.close(session.getNid());
        sessions.remove(session.getAccount());
        log.info("session " + sessionId + " have been closed!");
      }else if(session.getSource()==Constants.ImserverConfig.WEBSOCKET){
        //用于解决websocket多开页面session被踢下线的问题
        Session  newsession = setSessionContent(ctx,wrapper,sessionId);
        session.addSessions(newsession);
        updateSession(session);
        CabinetChannelGroup.add(newsession.getSession());
        log.info("session " + sessionId + " update!");
        return newsession;
      }else if(session.getSource()==Constants.ImserverConfig.DWR){
        //清除dwr session
        log.info("sessionId ----" + session.getAccount() +" start remove !");
        session.closeAll();
        sessions.remove(session.getAccount());
        log.info("session " + sessionId + " have been closed!");
      }
    }

    session = setSessionContent(ctx,wrapper,sessionId);
    addSession(session);
    return session;
  }


  @Override
  public Session createSession(ScriptSession scriptSession, String sessionid) {

    Session dwrsession = new Session(scriptSession);
    dwrsession.setAccount(sessionid);
    dwrsession.setSource(Constants.ImserverConfig.DWR);
    dwrsession.setPlatform((String)scriptSession.getAttribute(Constants.DWRConfig.BROWSER));
    dwrsession.setPlatformVersion((String)scriptSession.getAttribute(Constants.DWRConfig.BROWSER_VERSION));
    dwrsession.setBindTime(System.currentTimeMillis());
    dwrsession.setUpdateTime(System.currentTimeMillis());
    Session session = sessions.get(sessionid);
    if (session != null) {
      log.info("session " + sessionid + " exist!");
      if(session.getSource()!=Constants.ImserverConfig.DWR){
        //从广播组清除
        log.info("sessionId ----" + session.getAccount() +" start remove !");
        CabinetChannelGroup.remove(session.getSession());
        List<Channel> channels = session.getSessionAll();
        if(channels!=null&&channels.size()>0){
          for(Channel cl:channels){
            CabinetChannelGroup.remove(cl);
          }
        }
        //session.close();
        sessions.remove(session.getAccount());
        log.info("session " + sessionid + " have been closed!");
      }else if(session.getSource()==Constants.ImserverConfig.DWR){
        session.addSessions(dwrsession);
        updateSession(session);
        log.info("session " + sessionid + " update!");
        return dwrsession;
      }
    }
    addSession(dwrsession);
    return dwrsession;
  }


  /**
   * 设置session内容
   * @param ctx
   * @param wrapper
   * @param sessionId
   * @return
   */
  private Session  setSessionContent(ChannelHandlerContext ctx,MsgWrapper wrapper,String sessionId){
    log.info("create new session " + sessionId + ", ctx -> " + ctx.toString());
    CabinetMsgProto.CabinetMsg model = (CabinetMsgProto.CabinetMsg)wrapper.getBody();
    Session session = new Session(ctx.channel());
    session.setAccount(sessionId);
    session.setSource(wrapper.getSource());
    session.setAppKey(model.getAppKey());
    session.setDeviceId(model.getDeviceId());
    session.setPlatform(model.getPlatformChannel());
    session.setPlatformVersion(model.getPlatformVersion());
    session.setSign(model.getSign());
    session.setBindTime(System.currentTimeMillis());
    session.setUpdateTime(session.getBindTime());
    log.info("create new session " + sessionId + " successful!");
    return session;
  }


  @Override
  public boolean exist(String sessionId) {
    Session session = getSession(sessionId);
    return session != null ? true : false;
  }
}
