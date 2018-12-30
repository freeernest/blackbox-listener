package springvertx.verticlelauncher;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class VerticlesDeployer {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private Vertx vertx;
    private List<VerticleBean> verticles;
    private Set<String> deployedVerticles;
    private boolean deploySynchronous = false;
    private JsonObject config;
    private MessageConsumer<Boolean> healthConsumer;

    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    public void setVerticles(List<VerticleBean> verticles) {
        this.verticles = verticles;
    }

    public void setDeploySynchronous(boolean deploySynchronous) {
        this.deploySynchronous = deploySynchronous;
    }

    public void setVerticlesNames(List<String> verticles) {
        List<VerticleBean> vList = new ArrayList<>(verticles.size());
        for (String v : verticles)
            vList.add(new VerticleBean(v, false));

        this.verticles = vList;
    }

    public void deploy() throws Exception {
        deployedVerticles = new HashSet<>();
        int coreCount = Runtime.getRuntime().availableProcessors();

        config = new JsonObject();

        if (!deploySynchronous) {

            deployVerticlesAsynchronous(coreCount);
        } else {

            deployVerticlesSynchronous(coreCount, 0);
        }

        healthConsumer = setupHealthcheckCunsumer();
    }

    private void deployVerticlesAsynchronous(int coreCount) {
        for (VerticleBean vb : verticles) {
            deployVerticle(vb, coreCount, result -> {
                if (result.succeeded()) {
                    logger.info("Deployed " + vb.toString());
                    deployedVerticles.add(vb.getBeanName());

                    if (deployedVerticles.size() == verticles.size()) {
                        logger.info("All verticles were deployed successfully");
                    }
                } else {
                    logger.error("Failed to deploy verticle " + vb.toString(), result.cause());
                }
            });
        }
    }

    public void deployVerticlesSynchronous(int coreCount, int deployPosition) {
        VerticleBean vb = verticles.get(deployPosition);

        deployVerticle(vb, coreCount, result -> {
            if (result.succeeded()) {
                deployedVerticles.add(vb.getBeanName());
                logger.info("Deployed " + vb.toString());
                if (deployPosition < verticles.size() - 1) {
                    deployVerticlesSynchronous(coreCount, deployPosition + 1);
                } else {
                    logger.info("All verticles were deployed successfully");
                }
            } else {
                logger.error("Failed to deploy verticle", result.cause());
            }
        });
    }

    private void deployVerticle(VerticleBean vb, int coreCount, Handler<AsyncResult<String>> completionHandler) {
        DeploymentOptions options = new DeploymentOptions();
        JsonObject config = extractConfig(vb);

        if (config != null) {
            options.setConfig(config);
        }
        if (vb.isScale()) {
            options.setInstances(coreCount);
        }

        logger.info("Deploying " + vb.toString());
        vertx.deployVerticle(vb.getBeanName(), options, completionHandler);
    }

    private MessageConsumer<Boolean> setupHealthcheckCunsumer() {
        return vertx.eventBus().localConsumer("checks.verticles", event -> {
            if (verticles.size() == deployedVerticles.size()) {
                event.reply(true);
            } else {
                JsonObject data = new JsonObject();
                verticles.forEach(verticleBean -> {
                    data.put(verticleBean.getBeanName(), deployedVerticles.contains(verticleBean.getBeanName()));
                });
                event.fail(919, data.encodePrettily());
            }
        });
    }

    private JsonObject extractConfig(VerticleBean vb) {
        if (vb.getConfigName() == null) {
            return null;
        }
        return config.getJsonObject(vb.getConfigName());
    }

    public void undeploy() {
        healthConsumer.unregister();

        CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        vertx.close(event -> {
            completableFuture.complete(null);
        });

        try {
            completableFuture.get();
        } catch (InterruptedException e) {
            logger.error("Failed", e);
        } catch (ExecutionException e) {
            logger.error("Failed", e);
        }
    }

}
