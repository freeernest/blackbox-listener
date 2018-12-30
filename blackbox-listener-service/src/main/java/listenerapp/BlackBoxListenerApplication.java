package listenerapp;

import com.ulisesbocchio.jasyptspringboot.environment.StandardEncryptableEnvironment;
import logging.SLF4JEnabler;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication
@ImportResource({"classpath:application-context.xml"})
public class BlackBoxListenerApplication {

    public static void main(String[] args) {
        SLF4JEnabler.enable();
        new SpringApplicationBuilder().environment(new StandardEncryptableEnvironment()).sources(BlackBoxListenerApplication.class).run(args);
    }
}
