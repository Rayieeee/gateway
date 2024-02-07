package com.core.netty;

import com.common.util.RemotingUtil;
import com.core.config.Config;
import com.core.cycle.LifeCycle;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class NettyHttpServer implements LifeCycle {

    //引入启动配置类
    private final Config config;

    //启动类
    private ServerBootstrap serverBootstrap;

    //boss线程组
    private EventLoopGroup bossGroup;

    //worker线程组
    private EventLoopGroup workerGroup;

    public NettyHttpServer(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
        this.serverBootstrap = new ServerBootstrap();
        if (userEpoll()) {
            this.bossGroup = new EpollEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("epoll-boss", Thread.MAX_PRIORITY));
            this.workerGroup = new EpollEventLoopGroup(config.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("epoll-worker", Thread.MAX_PRIORITY));
        } else {
            this.bossGroup = new NioEventLoopGroup(config.getEventLoopGroupBossNum(),
                    new DefaultThreadFactory("nio-boss", Thread.MAX_PRIORITY));
            this.workerGroup = new NioEventLoopGroup(config.getEventLoopGroupWorkNum(),
                    new DefaultThreadFactory("nio-worker", Thread.MAX_PRIORITY));
        }

    }

    private boolean userEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void start() {
        this.serverBootstrap.group(bossGroup, workerGroup)
                .channel(userEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(config.getPort()))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(
                                new HttpServerCodec(),// HTTP 编解码
                                new HttpObjectAggregator(config.getMaxContentLength()),
                                new NettyServerConnectManagerHandler(),
                                new NettyHttpServerHandler());  //请求报文聚合成FullHttpRequest
                    }
                });

        try {
            this.serverBootstrap.bind().sync();
            log.info("NettyHttpServer start success, port: {}", config.getPort());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void shutdown() {
        // 优雅关闭
        if (this.bossGroup != null) {
            this.bossGroup.shutdownGracefully();
        }
        if (this.workerGroup != null) {
            this.workerGroup.shutdownGracefully();
        }
    }
}
