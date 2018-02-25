package dlut.rpc.server;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
*@author WuJie
*@date 2018年1月28日下午4:25:14
*@version 1.0
**/
public class RpcBootstrap {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new ClassPathXmlApplicationContext("spring.xml");
	}

}
