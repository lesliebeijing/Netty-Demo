package com.lesliefang.demo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class ConnectionWatchDog extends ChannelInboundHandlerAdapter implements TimerTask {
    private final Bootstrap bootstrap;
    private final String host;
    private final int port;
    private volatile boolean reconnect;
    private int attempts;
    private Channel channel;
    private HashedWheelTimer timer = new HashedWheelTimer();
    private int reconnectDelay = 5;

    public ConnectionWatchDog(Bootstrap bootstrap, String host, int port, boolean reconnect) {
        this.bootstrap = bootstrap;
        this.host = host;
        this.port = port;
        this.reconnect = reconnect;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive");
        channel = ctx.channel();
        ctx.fireChannelActive();
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelInactive");
        ctx.fireChannelInactive();
        channel = null;

        if (reconnect) {
            attempts = 0;
            scheduleReconnect();
        }
    }

    private void connect() {
        bootstrap.connect(host, port).addListener((future) -> {
            if (future.isSuccess()) {
                System.out.println("connected to " + host + ":" + port);
                attempts = 0;
            } else {
                System.out.println("connect failed " + attempts + " , to reconnect after " + reconnectDelay + " 秒");
                // 这里现在每5秒重连一次直到连接上，可自己实现重连逻辑
                scheduleReconnect();
            }
        });
    }

    public void run(Timeout timeout) {
        synchronized (this.bootstrap) {
            ++attempts;
            connect();
        }
    }

    private void scheduleReconnect() {
        timer.newTimeout(this, reconnectDelay, TimeUnit.SECONDS);
    }

    public void setReconnect(boolean reconnect) {
        this.reconnect = reconnect;
    }
}
