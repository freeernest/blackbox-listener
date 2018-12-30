package springvertx.verticlelauncher;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import utils.NetworkUtil;

public class VertxFactoryObject implements FactoryBean<Vertx> {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private static Vertx vertx = Vertx.vertx();
    private ClusterManager clusterManager;
    private String haGroup;

    @Override
    public Vertx getObject() throws Exception {
        return vertx;
    }

    public void setClusterManager(ClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public void initCluster() {
        vertx.close();
        VertxOptions options = new VertxOptions();
        String host = NetworkUtil.getDefaultAddress();
        options.setClusterHost(host).setClusterPort(0).setClustered(true);
        logger.info("Public address {}", host);

        String haGroup = System.getProperty("hagroup", this.haGroup);
        if (haGroup != null) {
            options.setHAGroup(haGroup);
            options.setHAEnabled(true);
        }
        if (clusterManager != null)
            options.setClusterManager(clusterManager);

        Object monitor = new Object();
        Vertx.clusteredVertx(options, vertxAsyncResult -> {
            vertx = vertxAsyncResult.result();
            synchronized (monitor) {
                monitor.notify();
            }
        });
        try {
            synchronized (monitor) {
                monitor.wait();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Class<Vertx> getObjectType() {
        return Vertx.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public void setHaGroup(String haGroup) {
        this.haGroup = haGroup;
    }
}
