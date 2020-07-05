package com.qibingtech.miaoku.cabinet.server.session;

import com.qibingtech.miaoku.cabinet.server.model.MsgWrapper;
import com.qibingtech.miaoku.cabinet.server.model.Session;
import io.netty.channel.ChannelHandlerContext;
import java.util.Set;
import org.directwebremoting.ScriptSession;

/**
 * @program: cabinet
 * @description: session 管理
 * @author: lk
 * @create: 2020-07-05 13:12
 **/
public interface SessionManager {
  /**
   * 添加指定session
   *
   * @param session
   */
  void addSession(Session session);

  void updateSession(Session session);


  /**
   * 删除指定session
   *
   * @param sessionId
   */
  void removeSession(String sessionId);

  /**
   * 删除指定session
   *
   *
   * @param sessionId
   * @param nid  is socketid 
   */
  void removeSession(String sessionId,String nid);

  /**
   * 根据指定sessionId获取session
   *
   * @param sessionId
   * @return
   */
  Session getSession(String sessionId);

  /**
   * 获取所有的session
   *
   * @return
   */
  Session[] getSessions();

  /**
   * 获取所有的session的id集合
   *
   * @return
   */
  Set<String> getSessionKeys();

  /**
   * 获取所有的session数目
   *
   * @return
   */
  int getSessionCount();

  Session createSession(MsgWrapper wrapper, ChannelHandlerContext ctx);

  Session createSession(ScriptSession scriptSession, String sessionid);

  boolean exist(String sessionId) ;
}
