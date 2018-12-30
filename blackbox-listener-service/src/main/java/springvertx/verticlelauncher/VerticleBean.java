package springvertx.verticlelauncher;

public class VerticleBean {
    private String beanName;
    private boolean scale;
    private String configName;


    public VerticleBean() {}

    public VerticleBean(String name, boolean scale) {
        this.beanName = name;
        this.scale = scale;
    }

    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    public boolean isScale() {
        return scale;
    }

    public void setScale(boolean scale) {
        this.scale = scale;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    @Override
    public String toString() {
        return "VerticleBean{" +
                "beanName='" + beanName + '\'' +
                ", scale=" + scale +
                ", configName='" + configName + '\'' +
                '}';
    }
}
