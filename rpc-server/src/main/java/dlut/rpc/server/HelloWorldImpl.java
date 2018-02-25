package dlut.rpc.server;

import dlut.rpc.IHelloService;

/**
*@author WuJie
*@date 2018年1月28日下午4:03:33
*@version 1.0
**/
@RpcService(IHelloService.class)
public class HelloWorldImpl implements IHelloService {

	public String hello(String name) {
		// TODO Auto-generated method stub
		return "Hello " + name + " ==> from remote greeting";
	}	
}
