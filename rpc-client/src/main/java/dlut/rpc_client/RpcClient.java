package dlut.rpc_client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlut.rpc.common.RpcDecoder;
import dlut.rpc.common.RpcEncoder;
import dlut.rpc.common.RpcRequest;
import dlut.rpc.common.RpcResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
*@author WuJie
*@date 2018年1月29日下午12:40:58
*@version 1.0
**/
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
	
	private String host = null;
	private int port = 0;
	
	private RpcResponse response;

	private Object lock = new Object();;
	
	public RpcClient(String host, int port) {
		// TODO Auto-generated constructor stub
		this.host = host;
		this.port = port;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
		// TODO Auto-generated method stub
		this.response = msg;
		LOGGER.debug("receive msg from ");
		LOGGER.debug("content => {}", this.response);
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
	public RpcResponse send(RpcRequest request) throws InterruptedException {
		// TODO Auto-generated method stub
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group)
			.channel(NioSocketChannel.class)
			.handler(new ChannelInitializer<SocketChannel>() {

				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					// TODO Auto-generated method stub
					ch.pipeline()
					.addLast(new RpcEncoder(RpcRequest.class)) //编码请求
					.addLast(new RpcDecoder(RpcResponse.class))//解码回复
					.addLast(RpcClient.this);
				}
				
			})
			.option(ChannelOption.SO_KEEPALIVE, true);
			LOGGER.debug("host => {}, port => {}", host, port);
			ChannelFuture future = bootstrap.connect(host, port).sync();
			future.channel().writeAndFlush(request).sync();
			
			LOGGER.debug("connected");
			
			synchronized (lock ) {
				lock.wait();//阻塞直到消息被RpcResponse被读取到
			}
			
			if (response != null) {
				future.channel().closeFuture().sync();
			}
			return response;
		} finally {
			group.shutdownGracefully();
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		LOGGER.error("client caught exception", cause);
        ctx.close();
	}
}
