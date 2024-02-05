package com.core.config;

import lombok.Data;

@Data
public class Config {

    private int port = 8888;

    private String applicationName = "gateway";

    private String registryAddress = "127.0.0.1:8848";

    private String env = "dev";

    //Netty相关配置
    //boss线程数
    private int eventLoopGroupBossNum = 1;

    //work线程数
    private int eventLoopGroupWorkNum = Runtime.getRuntime().availableProcessors();

    //内容大小，64M
    private int maxContentLength = 1024 * 1024 * 64;

    //默认单异步模式
    private boolean whenComplete = true;

    //Http Async
    //连接超时时间
    private int connectTimeout = 1000 * 30;

    //请求超时时间
    private int httpRequestTimeout = 1000 * 30;

    //客户端请求重试次数
    private int httpMaxRequestRetry = 3;

    //客户端请求最大连接数
    private int httpMaxConnections = 10000;

    //客户端每个地址支持的最大连接数
    private int httpMaxConnectionsPerHost = 8000;

    //客户端空闲连接超时时间，默认60s
    private int httpPooledConnectionIdleTimeout = 1000 * 60;

}
