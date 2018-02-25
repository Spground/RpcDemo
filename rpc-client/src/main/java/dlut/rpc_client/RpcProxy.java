package dlut.rpc_client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.UUID;

import dlut.rpc.common.RpcRequest;
import dlut.rpc.common.RpcResponse;

/**
*@author WuJie
*@date 2018年1月29日下午12:41:05
*@version 1.0
**/
public class RpcProxy {
	private String serverAddress;
	private ServiceDiscover serviceDiscover;
	
	public RpcProxy(ServiceDiscover serviceDiscover) {
		this.serviceDiscover = serviceDiscover;
	}
	
	public RpcProxy(String serverAddress) {
		this.serverAddress = serverAddress;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T create(Class<?> interfaceClass) {
		return (T) Proxy.newProxyInstance(
				interfaceClass.getClassLoader(),
				new Class<?>[]{interfaceClass},
				new InvocationHandler() {
					
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						// TODO Auto-generated method stub
						//封装请求
						RpcRequest request = new RpcRequest();
						request.setRequestId(UUID.randomUUID().toString());
						request.setClassName((String)(method.getDeclaringClass().getField("REMOTE_CLASS_NAME").get(method.getDeclaringClass())));
						request.setMethodName(method.getName());
						request.setParameterTypes(method.getParameterTypes());
						request.setParameters(args);
						
						if (serviceDiscover != null) {
							serverAddress = serviceDiscover.discover();
						} 
						
						String[] array = serverAddress.split(":");
						String host = array[0];
						int port = Integer.parseInt(array[1]);
						
						RpcClient client = new RpcClient(host, port);
						RpcResponse response = client.send(request);//阻塞直到方法返回
						Object obj = response.getResult();
						
						System.out.println(Arrays.toString(obj.getClass().getTypeParameters()));
						
						if (response == null)
							throw new Exception("response is null");
						if (response.getError() != null) {
							throw response.getError();
						} else {
							return response.getResult();
						}
					}
				});
	}
}
