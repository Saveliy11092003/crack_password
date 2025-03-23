package ru.trushkov.crack_manager.configuration;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${url.mappings}")
    private String urlMappings;

    @Value("${port.type.name}")
    private String portTypeName;

    @Value("${location.uri}")
    private String locationUri;

    @Value("${target.namespace}")
    private String targetNamespace;

    @Value("${workers.schema}")
    private String workerSchema;

    @Bean
    public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(ApplicationContext applicationContext) {
        MessageDispatcherServlet servlet = new MessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<>(servlet, urlMappings);
    }

    @Bean(name="workers")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema workersSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName(portTypeName);
        wsdl11Definition.setLocationUri(locationUri);
        wsdl11Definition.setTargetNamespace(targetNamespace);
        wsdl11Definition.setSchema(workersSchema);
        return wsdl11Definition;
    }

    @Bean
    public XsdSchema workersSchema() {
        return new SimpleXsdSchema(new ClassPathResource(workerSchema));
    }
}
