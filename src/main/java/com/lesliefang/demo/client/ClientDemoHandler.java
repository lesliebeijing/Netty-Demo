package com.lesliefang.demo.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.TimeUnit;

public class ClientDemoHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

        ctx.executor().schedule(() -> {
            System.out.println("client send message1");
            ctx.channel().writeAndFlush("hello I am client 1\n"); // 注意加 \n 因为使用了 LineBasedFrameDecoder
        }, 1, TimeUnit.SECONDS);

        ctx.executor().schedule(() -> {
            System.out.println("client send message2");
            ctx.channel().writeAndFlush("hello I am client 2\n");
        }, 2, TimeUnit.SECONDS);

        ctx.executor().schedule(() -> {
            System.out.println("client send message3");
            ctx.channel().writeAndFlush("hello I am client 3\n");
        }, 3, TimeUnit.SECONDS);
    }
}
