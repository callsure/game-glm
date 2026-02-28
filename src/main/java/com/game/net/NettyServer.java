package com.game.net;

import com.game.config.ServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Netty 服务器
 * 基于Netty实现的高性能游戏服务器
 *
 * @author Harleysama
 */
@Slf4j
public class NettyServer {

    private final ServerConfig config;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;

    public NettyServer(ServerConfig config) {
        this.config = config;
    }

    /**
     * 启动服务器
     */
    public void start() throws InterruptedException {
        log.info("正在启动游戏服务器... 端口: {}", config.getPort());

        // Boss线程组 - 处理连接请求
        bossGroup = new NioEventLoopGroup(1);
        // Worker线程组 - 处理IO操作
        workerGroup = new NioEventLoopGroup(config.getWorkerThreads());

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // 心跳检测 - 读超时60秒
                            pipeline.addLast("idleStateHandler",
                                    new IdleStateHandler(config.getHeartbeatTimeout(), 0, 0, TimeUnit.SECONDS));

                            // 协议编解码
                            pipeline.addLast("frameDecoder", new PacketFrameDecoder());
                            pipeline.addLast("frameEncoder", new PacketFrameEncoder());

                            // 业务处理器
                            pipeline.addLast("gameHandler", new GameServerHandler());
                        }
                    });

            // 绑定端口并启动
            ChannelFuture future = bootstrap.bind(config.getPort()).sync();
            serverChannel = future.channel();

            log.info("========================================");
            log.info("游戏服务器启动成功! (*￣︶￣)");
            log.info("监听端口: {}", config.getPort());
            log.info("工作线程数: {}", config.getWorkerThreads());
            log.info("心跳超时: {}秒", config.getHeartbeatTimeout());
            log.info("========================================");

            // 阻塞，直到服务器关闭
            serverChannel.closeFuture().sync();

        } finally {
            shutdown();
        }
    }

    /**
     * 优雅关闭服务器
     */
    public void shutdown() {
        log.info("正在关闭游戏服务器...");

        if (serverChannel != null) {
            serverChannel.close().syncUninterruptibly();
        }

        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }

        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }

        log.info("游戏服务器已关闭。再见啦，笨蛋! (￣^￣)ゞ");
    }
}
