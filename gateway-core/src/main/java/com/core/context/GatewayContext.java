package com.core.context;

import io.netty.channel.ChannelHandlerContext;

public class GatewayContext extends BasicContext{


    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        super(protocol, nettyCtx, keepAlive);
    }
}
