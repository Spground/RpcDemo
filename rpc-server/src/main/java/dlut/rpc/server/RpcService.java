package dlut.rpc.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
*@author WuJie
*@date 2018年1月28日下午4:07:49
*@version 1.0
**/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component// 表明可被Spring扫描
public @interface RpcService {
	Class<?> value();
}
