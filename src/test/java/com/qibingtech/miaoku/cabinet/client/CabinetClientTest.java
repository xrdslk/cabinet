/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.client;

import com.qibingtech.miaoku.cabinet.constant.Constants;
import com.qibingtech.miaoku.cabinet.data.CabinetMessageData;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import java.util.Scanner;
import java.util.UUID;

/**
 * @program: cabinet
 *
 * @description: 客户端测试
 *
 * @author: xrds
 *
 * @create: 2020-07-05 20:48
 **/
public class CabinetClientTest {


  public  String host = "127.0.0.1";
  public  int port = 2000;

  public Bootstrap bootstrap = getBootstrap();
  public Channel channel = getChannel(host, port);

  public   Bootstrap getBootstrap() {
    EventLoopGroup group = new NioEventLoopGroup();
    Bootstrap b = new Bootstrap();
    b.group(group);
    b.channel(NioSocketChannel.class);
    b.handler(new ChannelInitializer<Channel>() {
      @Override
      protected void initChannel(Channel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        pipeline.addLast("decoder", new ProtobufDecoder(CabinetMsgProto.CabinetMsg.getDefaultInstance()));
        pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new IdleStateHandler(Constants.ImserverConfig.READ_IDLE_TIME,Constants.ImserverConfig.WRITE_IDLE_TIME,0));
        pipeline.addLast("encoder", new ProtobufEncoder());
        pipeline.addLast("handler", new CabinetClientHandlerTest());
      }
    });
    return b;
  }


  public   Channel getChannel(String host, int port) {
    Channel channel;
    try {
      channel = bootstrap.connect(host, port).sync().channel();
    } catch (Exception e) {
      System.out.println("Connect Server (host[" + host + "]:port[" + port + "]) Failure." + e);
      return null;
    }
    return channel;
  }

  public  void connect(Object msg) throws Exception {
    if (channel != null) {
      channel.writeAndFlush(msg).sync();
    }
  }


  public static void main(String[] args) throws Exception {
    try {
      //String currentuser = "abc";
      String currentuser = UUID.randomUUID().toString().replaceAll("-", "");
      //链接socket服务
      new CabinetClientTest().connect(new CabinetMessageData().generateConnect(currentuser));
      Scanner sc = new Scanner(System.in);
      System.out.println("输入用户SessionId：");
      String reuser=sc.next();
      System.out.println("和"+reuser+"发信息吧！");
      sc.nextLine();
      while (true) {
        String line = sc.nextLine();
        new CabinetClientTest().connect(new CabinetMessageData().generateSend(currentuser, reuser,line));
        if (line.equals("exit")) break;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }
  }





}
