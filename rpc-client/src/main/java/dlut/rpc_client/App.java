package dlut.rpc_client;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import dlut.rpc.IHelloService;

/**
 * Hello world!
 *
 */

public class App {
    @SuppressWarnings("resource")
	public static void main( String[] args ) {
    	ApplicationContext context = new ClassPathXmlApplicationContext("spring-client.xml");
    	
    	RpcProxy rpcProxy = context.getBean(RpcProxy.class);
        
    	IHelloService helloService = rpcProxy.create(IHelloService.class);
        
    	String result = helloService.hello("memeda");
        
    	System.out.println(result);
        
        System.exit(0);
    }
}
