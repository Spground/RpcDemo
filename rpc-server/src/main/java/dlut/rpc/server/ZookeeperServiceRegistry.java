package dlut.rpc.server;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlut.rpc.common.Constant;

/**
*@author WuJie
*@date 2018年1月28日下午4:26:23
*@version 1.0
* 创建服务注册中心support by zookeeper
**/
public class ZookeeperServiceRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);
	
	private String registryAddress;

	protected CountDownLatch latch = new CountDownLatch(1);
	
	public ZookeeperServiceRegistry(String registryAddress) {
		this.registryAddress = registryAddress;
	}
	
	public void register(String data) {
		if (data != null) {
			ZooKeeper zk = connectServer();
			if (Objects.nonNull(zk)) {
				createNode(zk, data);
			}
		}
	}

	private void createNode(ZooKeeper zk, String data) {
		// TODO Auto-generated method stub
		try {
			byte[] bytes = data.getBytes();
			String path = zk.create(Constant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			LOGGER.debug("create zookeeper node ({} => {})", path, data);
		} catch (KeeperException | InterruptedException e) {
			LOGGER.error("", e);
		}
	}

	private ZooKeeper connectServer() {
		// TODO Auto-generated method stub
		ZooKeeper zk = null;
		try {
			zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
				
				public void process(WatchedEvent event) {
					// TODO Auto-generated method stub
					if (event.getState() == Event.KeeperState.SyncConnected) {
						latch.countDown();
					}
				}
			});
			latch.await();
		} catch (IOException |  InterruptedException e) {
			LOGGER.error("", e);
		}
		return zk;
	}
	
	
}
