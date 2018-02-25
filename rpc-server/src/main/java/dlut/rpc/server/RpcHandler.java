package dlut.rpc.server;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlut.rpc.common.RpcRequest;
import dlut.rpc.common.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;

/**
*@author WuJie
*@date 2018年1月29日上午9:56:26
*@version 1.0
**/
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);
	private final Map<String, Object> handlerMap;
	
	public RpcHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
		// TODO Auto-generated method stub
		RpcResponse response = new RpcResponse();
		response.setRequestId(msg.getRequestId());
		try {
			Object result = handle(msg);
			response.setResult(result);
		} catch (Throwable t) {
			LOGGER.debug("handle ocurred error ==> {}", t);
			response.setError(t);
		}
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);//写完然后关闭channel
	}
	
	private Object handle(RpcRequest request) throws Throwable {
		String className = request.getClassName();//此处极易出错，需要保证客户端和服务端的className是一致的，否则直接GG
		Object serviceBean = handlerMap.get(className);
		if (serviceBean == null) {
			throw new Throwable(String.format("can not find service bean by given name [%s] in server ", className));
		}
		System.out.println(handlerMap);
		System.out.println(className);
		Class<?> serviceClass = serviceBean.getClass();
		String methodName = request.getMethodName();
		Class<?>[] parameterTypes = request.getParameterTypes();
		Object[] parameters = request.getParameters();
		LOGGER.debug("handle request {}", request.getRequestId());
		//以下为利用CGlib反射调用serviceBean的方法
		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
		return serviceFastMethod.invoke(serviceBean, parameters);
		
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		// TODO Auto-generated method stub
		LOGGER.error("server caught exception", cause);
		ctx.close();
	}
}
