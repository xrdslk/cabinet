/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.data;

import com.qibingtech.miaoku.cabinet.constant.Constants;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgBodyProto;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import com.qibingtech.miaoku.cabinet.util.SystemInfo;
import java.util.Date;
import org.apache.commons.lang3.time.DateFormatUtils;

/**
 * @program: cabinet
 *
 * @description: 消息数据信息
 *
 * @author: xrds
 *
 * @create: 2020-07-05 20:55
 **/
public class CabinetMessageData {

  public  CabinetMsgProto.CabinetMsg.Builder generateConnect(String sessionid) {
    SystemInfo syso = SystemInfo.getInstance();
    CabinetMsgProto.CabinetMsg.Builder builder = CabinetMsgProto.CabinetMsg.newBuilder();
    builder.setVersion("1.0");
    builder.setDeviceId(syso.getMac());
    builder.setCmd(Constants.CmdType.BIND);
    builder.setSender(sessionid);
    builder.setReceiver(sessionid);
    builder.setMsgtype(Constants.ProtobufType.SEND);
    builder.setFlag(1);
    builder.setPlatformChannel(syso.getSystem());
    builder.setPlatformVersion(syso. getSystemName());
    builder.setToken(sessionid);
    builder.setAppKey("123");
    builder.setTimeStamp(DateFormatUtils.format(new Date(), "yyyy-MM-dd HH:mm:ss"));
    builder.setSign("123");
    return builder;
  }

  public  CabinetMsgProto.CabinetMsg.Builder generateHeartbeat() {
    CabinetMsgProto.CabinetMsg.Builder builder = CabinetMsgProto.CabinetMsg.newBuilder();
    builder.setCmd(Constants.CmdType.HEARTBEAT);
    builder.setMsgtype(Constants.ProtobufType.REPLY);
    return builder;
  }

  public  CabinetMsgProto.CabinetMsg.Builder generateSend(String sessionid,String ressionId,String sendcontent) {
    CabinetMsgProto.CabinetMsg.Builder builder = CabinetMsgProto.CabinetMsg.newBuilder();
    builder.setCmd(Constants.CmdType.MESSAGE);
    builder.setSender(sessionid);
    builder.setReceiver(ressionId);
    builder.setMsgtype(Constants.ProtobufType.REPLY);
    builder.setToken(sessionid);
    CabinetMsgBodyProto.CabinetMsgBody.Builder  msg =  CabinetMsgBodyProto.CabinetMsgBody.newBuilder();
    msg.setContent(sendcontent);
    builder.setContent(msg.build().toByteString());
    return builder;
  }
}
