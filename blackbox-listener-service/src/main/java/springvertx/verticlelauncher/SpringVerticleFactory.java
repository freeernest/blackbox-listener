package springvertx.verticlelauncher;

import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.spi.VerticleFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class SpringVerticleFactory implements VerticleFactory, ApplicationContextAware, InitializingBean {
    public static final String PREFIX = "spring";

    private ApplicationContext applicationContext;
    private Vertx vertx;

    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        vertx.registerVerticleFactory(this);
    }

    @Override
    public void init(Vertx vertx) {
        this.setVertx(vertx);
    }

    @Override
    public String prefix() {
        return PREFIX;
    }

    @Override
    public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
        verticleName = VerticleFactory.removePrefix(verticleName);
        return (Verticle) applicationContext.getBean(verticleName);
    }
}

