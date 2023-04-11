package web;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages={"core","web"})
public class WebAppApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(WebAppApplication.class).run(args);
    }
}
