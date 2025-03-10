package ru.trushkov.crack_manager.configuration;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {
    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, "/internal/api/manager/hash/crack/task/*");
    }

    @Bean(name="workers")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema workersSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("WorkersPort");
        wsdl11Definition.setLocationUri("/internal/api/manager/hash/crack/task");
        wsdl11Definition.setTargetNamespace("http://ccfit.nsu.ru/schema/crack-hash-response");
        wsdl11Definition.setSchema(workersSchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema workersSchema() {
        return new SimpleXsdSchema(new ClassPathResource("crack_hash_worker_schema.xsd"));
    }
}
