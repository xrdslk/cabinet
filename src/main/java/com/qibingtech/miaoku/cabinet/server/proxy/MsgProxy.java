package com.qibingtech.miaoku.cabinet.server.proxy;

import com.qibingtech.miaoku.cabinet.server.model.MsgWrapper;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto.CabinetMsg;

/**
 * @program: cabinet
 * @description: 消息代理
 * @author: lk
 * @create: 2020-07-05 08:38
 **/
public interface MsgProxy {

  /**
   * 保存在线消息
   * @param message
   */
  void  saveOnlineMessageToDB(MsgWrapper message);
  /**
   * 保存离线消息
   * @param message
   */
  void  saveOfflineMessageToDB(MsgWrapper message);
  /**
   * 获取上线状态消息
   * @param sessionId
   * @return
   */
  CabinetMsgProto.CabinetMsg getOnLineStateMsg(String sessionId);
  /**
   * 重连状态消息
   * @param sessionId
   * @return
   */
  MsgWrapper  getReConnectionStateMsg(String sessionId);

  /**
   * 获取下线状态消息
   * @param sessionId
   * @return
   */
  CabinetMsgProto.CabinetMsg  getOffLineStateMsg(String sessionId);


  MsgWrapper convertToMsgWrapper(String sessionId, CabinetMsg message);
}
