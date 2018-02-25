package dlut.rpc_client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dlut.rpc.common.Constant;

/**
*@author WuJie
*@date 2018年1月29日上午11:37:09
*@version 1.0
**/
public class ServiceDiscover {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscover.class);
	private CountDownLatch latch = new CountDownLatch(1);
	private volatile List<String> dataList = new ArrayList<>();
	private String registryAddress;
	
	public ServiceDiscover(String registryAddress) {
		this.registryAddress = registryAddress;
		ZooKeeper zk = connectServer();
		if (zk != null) {
			watchNode(zk);
		}
	}
	
	public String discover() {
		String data = null;
		int size = dataList.size();
		if (size > 0) {
			if (size == 1) {
				data = dataList.get(0);
				LOGGER.debug("using only data: {}", data);
			} else {
				data = dataList.get(ThreadLocalRandom.current().nextInt(size));
				LOGGER.debug("using random data: {}", data);
			}
		}
		return data;
	}
	
	//查看所有结点
	private void watchNode(final ZooKeeper zk) {
		// TODO Auto-generated method stub
		try {
			List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {

				@Override
				public void process(WatchedEvent event) {
					// TODO Auto-generated method stub
					if (event.getType() == Event.EventType.NodeChildrenChanged) {
						watchNode(zk);
					}
				}
			});
			List<String> dataList = new ArrayList<>();
			for (String node : nodeList ) {
				byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
				dataList.add(new String(bytes));
			}
			this.dataList = dataList;
			LOGGER.debug("node data: {}", dataList);
		} catch (KeeperException | InterruptedException e) {
			LOGGER.error("", e);
		}
	}
	
	//连接到zookeeper
	private ZooKeeper connectServer() {
		// TODO Auto-generated method stub
		 ZooKeeper zk = null;
	        try {
	            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, new Watcher() {
	                @Override
	                public void process(WatchedEvent event) {
	                    if (event.getState() == Event.KeeperState.SyncConnected) {
	                        latch.countDown();
	                    }
	                }
	            });
	            latch.await();
	        } catch (IOException | InterruptedException e) {
	            LOGGER.error("", e);
	        }
	        return zk;
	}
	
	
	
}
