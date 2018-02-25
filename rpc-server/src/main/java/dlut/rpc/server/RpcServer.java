package dlut.rpc.server;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import dlut.rpc.common.RpcDecoder;
import dlut.rpc.common.RpcEncoder;
import dlut.rpc.common.RpcRequest;
import dlut.rpc.common.RpcResponse;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * @author WuJie
 * @date 2018年1月28日下午4:23:02
 * @version 1.0
 **/
public class RpcServer implements ApplicationContextAware, InitializingBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

	private String serverAddress;
	private ZookeeperServiceRegistry serviceRegistry;

	private Map<String, Object> handlerMap = new HashMap<>();

	public RpcServer(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public RpcServer(String serverAddress, ZookeeperServiceRegistry serviceRegistry) {
		// TODO Auto-generated constructor stub
		this.serverAddress = serverAddress;
		this.serviceRegistry = serviceRegistry;
	}

	@Override
	public void setApplicationContext(ApplicationContext ctx) throws BeansException {
		// TODO Auto-generated method stub
		Map<String, Object> serviceBeanMap = ctx.getBeansWithAnnotation(RpcService.class);
		if (MapUtils.isNotEmpty(serviceBeanMap)) {
			for (Object serviceBean : serviceBeanMap.values()) {
				String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
				handlerMap.put(interfaceName, serviceBean);
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();

		try {
			ServerBootstrap bootStrap = new ServerBootstrap();
			// 配置Server端的NIO
			bootStrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							// TODO Auto-generated method stub
							LOGGER.debug("initChannel");
							ch.pipeline()
							.addLast(new RpcDecoder(RpcRequest.class))// 处理Rpc请求 RpcRequest
							.addLast(new RpcEncoder(RpcResponse.class))// 处理Rpc RpcResponse
							.addLast(new RpcHandler(handlerMap));// 处理Rpc请求
						}
					})
					.option(ChannelOption.SO_BACKLOG, 128)
					.option(ChannelOption.SO_KEEPALIVE, true);

			// 解析地址
			String[] array = serverAddress.split(":");
			String host = array[0];
			int port = Integer.parseInt(array[1]);

			ChannelFuture future = bootStrap.bind(host, port).sync();
			LOGGER.debug("server started on port {}", port);

			// 注册服务
			if (serviceRegistry != null) {
				serviceRegistry.register(serverAddress);
			}
			future.channel().closeFuture().sync();
		} finally {
			LOGGER.debug("worker group and boss group shutdown");
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}
}
