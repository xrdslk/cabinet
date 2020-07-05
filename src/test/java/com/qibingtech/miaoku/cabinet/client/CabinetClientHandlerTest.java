/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.client;

import com.qibingtech.miaoku.cabinet.constant.Constants;
import com.qibingtech.miaoku.cabinet.data.CabinetMessageData;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgBodyProto;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import io.netty.channel.ChannelHandler.Sharable;
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
 * @create: 2020-07-05 20:51
 **/
@Sharable
public class CabinetClientHandlerTest extends ChannelInboundHandlerAdapter
  {

    private final static Logger logger = LoggerFactory.getLogger(CabinetClientHandlerTest.class);



    @Override
    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
      CabinetMsgProto.CabinetMsg message = (CabinetMsgProto.CabinetMsg) o;

      if(message.getCmd()== Constants.CmdType.HEARTBEAT){
        ctx.channel().writeAndFlush(new CabinetMessageData().generateHeartbeat());
        System.out.println("------------心跳检测--------------"+message);
      }else if(message.getCmd()==Constants.CmdType.ONLINE){
        System.out.println(message.getSender()+"------------上线了--------------");
      }else if(message.getCmd()==Constants.CmdType.RECON){
        System.out.println(message.getSender()+"------------重新连接--------------");
      }else if(message.getCmd()==Constants.CmdType.OFFLINE){
        System.out.println(message.getSender()+"------------下线了--------------");
      }else if(message.getCmd()==Constants.CmdType.MESSAGE){
        CabinetMsgBodyProto.CabinetMsgBody content =  CabinetMsgBodyProto.CabinetMsgBody.parseFrom(message.getContent()) ;
        logger.info(message.getSender()+" 响应给用户 :" + content.getContent());
        System.out.println(message.getSender()+" 响应给用户:" + content.getContent());
      }

    }


    public void userEventTriggered(ChannelHandlerContext ctx, Object o) throws Exception {
      //断线重连
    }

}
