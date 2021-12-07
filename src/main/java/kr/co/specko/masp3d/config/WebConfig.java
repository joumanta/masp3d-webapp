package kr.co.specko.masp3d.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("pages/index");
        registry.addViewController("/vmtech/vmtech").setViewName("pages/vmtech/vmtech");
        registry.addViewController("/service/basic").setViewName("pages/service/basic");
        registry.addViewController("/service/professional").setViewName("pages/service/professional");
        registry.addViewController("/service/special").setViewName("pages/service/special");
        registry.addViewController("/service/maps3dez").setViewName("pages/service/maps3dez");
        registry.addViewController("/charge/charge").setViewName("pages/charge/charge");
    }
}
