/**
 * 奇兵 Copyright (c) 2016-2020 QiBing,Inc.All Rights Reserved.
 */
package com.qibingtech.miaoku.cabinet.server;

import com.qibingtech.miaoku.cabinet.constant.Constants;
import com.qibingtech.miaoku.cabinet.server.connertor.impl.CabinetConnertorImpl;
import com.qibingtech.miaoku.cabinet.server.exception.InitErrorException;
import com.qibingtech.miaoku.cabinet.server.handler.CabinetServerHandler;
import com.qibingtech.miaoku.cabinet.server.model.proto.CabinetMsgProto;
import com.qibingtech.miaoku.cabinet.server.proxy.MsgProxy;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @program: cabinet
 *
 * @description: 智能柜 socketServer
 *
 * @author: xrds
 *
 * @create: 2020-07-04 23:34
 **/
public class CabinetServer {


  private final static Logger log = LoggerFactory.getLogger(CabinetServer.class);

  private ProtobufDecoder decoder = new ProtobufDecoder(CabinetMsgProto.CabinetMsg.getDefaultInstance());
  private ProtobufEncoder encoder = new ProtobufEncoder();

  private MsgProxy proxy;
  private CabinetConnertorImpl connertor;
  private int port;

  private final EventLoopGroup bossGroup = new NioEventLoopGroup();
  private final EventLoopGroup workerGroup = new NioEventLoopGroup();
  private Channel channel;

  public CabinetServer(int port){
    this.port=port;
  }


  public void init() throws Exception {
    log.info("start cabinet server ...");

    // Server 服务启动
    ServerBootstrap bootstrap = new ServerBootstrap();

    bootstrap.group(bossGroup, workerGroup);
    bootstrap.channel(NioServerSocketChannel.class);
    bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
      @Override
      public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        pipeline.addLast("decoder", decoder);
        pipeline.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast("encoder",encoder);
        pipeline.addLast(new IdleStateHandler(Constants.ImserverConfig.READ_IDLE_TIME,Constants.ImserverConfig.WRITE_IDLE_TIME,0));
        pipeline.addLast("handler", new CabinetServerHandler(proxy,connertor));
      }
    });

    // 可选参数
    bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
    // 绑定接口，同步等待成功
    log.info("start cabinet server at port[" + port + "].");
    ChannelFuture future = bootstrap.bind(port).sync();
    channel = future.channel();
    future.addListener(new ChannelFutureListener() {
      public void operationComplete(ChannelFuture future) throws Exception {
        if (future.isSuccess()) {
          log.info("Server have success bind to " + port);
        } else {
          log.error("Server fail bind to " + port);
          throw new InitErrorException("Server start fail !", future.cause());
        }
      }
    });
    // future.channel().closeFuture().syncUninterruptibly();
  }

  public void destroy() {
    log.info("destroy cabinet server ...");
    // 释放线程池资源
    if (channel != null) {
      channel.close();
    }
    bossGroup.shutdownGracefully();
    workerGroup.shutdownGracefully();
    log.info("destroy cabinet server complate.");
  }




  public void setPort(int port) {
    this.port = port;
  }

  public void setProxy(MsgProxy proxy) {
    this.proxy = proxy;
  }


  public void setConnertor(CabinetConnertorImpl connertor) {
    this.connertor = connertor;
  }





}
