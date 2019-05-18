package com.lesliefang.demo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

public class PushClient {
    private ConnectionWatchDog watchDog;

    public static void main(String[] args) {
        new PushClient().connect("127.0.0.1", 5510);
    }

    public void connect(String host, int port) {
        Bootstrap bootstrap = new Bootstrap().group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);

        watchDog = new ConnectionWatchDog(bootstrap, host, port, true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(watchDog);
                ch.pipeline().addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                ch.pipeline().addLast(new HeartbeatKeeper());
                ch.pipeline().addLast(new LineBasedFrameDecoder(1024));
                ch.pipeline().addLast(new StringDecoder(CharsetUtil.UTF_8));
                ch.pipeline().addLast(new StringEncoder(CharsetUtil.UTF_8));

                ch.pipeline().addLast(new ClientDemoHandler());
            }
        });

        // 这里如果第一次连接不成功也可以尝试多次连接
        bootstrap.connect(host, port);
    }

    public void disconnect() {
        if (watchDog != null) {
            watchDog.setReconnect(false);
            if (watchDog.getChannel() != null && watchDog.getChannel().isActive()) {
                watchDog.getChannel().close();
            }
        }
    }

    private void post(String message) {
        if (connected()) {
            watchDog.getChannel().writeAndFlush(message);
        } else {
            System.out.println("post !!! not connected");
        }
    }

    public boolean connected() {
        return watchDog != null && watchDog.getChannel() != null && watchDog.getChannel().isActive();
    }
}
