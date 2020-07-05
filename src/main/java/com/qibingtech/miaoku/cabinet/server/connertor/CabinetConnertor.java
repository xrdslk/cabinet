package com.qibingtech.miaoku.cabinet.server.connertor;

import com.qibingtech.miaoku.cabinet.server.model.MsgWrapper;
import io.netty.channel.ChannelHandlerContext;

/**
 * @program: cabinet
 * @description: 柜链接接口
 * @author: lk
 * @create: 2020-07-05 08:53
 **/
public interface CabinetConnertor {
  /**
   * 发送心跳检测 到客户端
   * @param hander
   * @param wrapper
   */
  void heartbeatToClient(ChannelHandlerContext hander, MsgWrapper wrapper);
  /**
   * 发送消息
   * @param wrapper
   * @throws RuntimeException
   */
  void pushMessage(MsgWrapper wrapper) throws RuntimeException;
  /**
   * 发送组消息
   * @param wrapper
   * @throws RuntimeException
   */
  public void pushGroupMessage(MsgWrapper wrapper) throws RuntimeException;
  /**
   * 验证session
   * @param wrapper
   * @return
   * @throws RuntimeException
   */
  boolean validateSession(MsgWrapper wrapper) throws RuntimeException;

  /**
   * 关闭连接
   * @param hander
   * @param wrapper
   */
  void close(ChannelHandlerContext hander,MsgWrapper wrapper);

  void close(String sessionId);

  void close(ChannelHandlerContext hander);

  void connect(ChannelHandlerContext ctx, MsgWrapper wrapper) ;

  boolean exist(String sessionId) throws Exception;
  /**
   * 发送消息
   * @param sessionId  发送人
   * @param wrapper   发送内容
   * @throws RuntimeException
   */
  void pushMessage(String sessionId,MsgWrapper wrapper) throws RuntimeException;
  /**
   * 获取用户唯一标识符
   * @param ctx
   * @return
   */
  String getChannelSessionId(ChannelHandlerContext ctx);
}
