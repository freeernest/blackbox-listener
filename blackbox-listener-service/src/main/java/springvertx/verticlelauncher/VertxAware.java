package springvertx.verticlelauncher;

import io.vertx.core.Vertx;

public interface VertxAware {
    void setVertx(Vertx vertx);
}
