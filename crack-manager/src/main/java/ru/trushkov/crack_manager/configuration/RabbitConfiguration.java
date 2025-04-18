package ru.trushkov.crack_manager.configuration;

import lombok.Setter;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MarshallingMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
@Setter
public class RabbitConfiguration {

    @Value("${queue.request.worker1}")
    private String requestQueueName1;

    @Value("${queue.request.worker2}")
    private String requestQueueName2;

    @Value("${queue.request.worker3}")
    private String requestQueueName3;

    @Value("${exchange.name}")
    private String exchangeName;

    @Bean("requestQueue1")
    public Queue requestQueue1() {
        return new Queue(requestQueueName1, true);
    }

    @Bean
    public DirectExchange exchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding1(Queue requestQueue1, DirectExchange exchange) {
        return BindingBuilder.bind(requestQueue1).to(exchange).with("task.worker1");
    }


    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("rabbitmq1", 5672);
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory());
        rabbitAdmin.declareExchange(exchange());
        return rabbitAdmin;
    }



    @Bean
    public MessageConverter converter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        return converter;
    }


    @Bean
    public AmqpTemplate amqpTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(converter());
        return rabbitTemplate;
    }

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(ru.nsu.ccfit.schema.crack_hash_response.CrackHashWorkerResponse.class);
        return marshaller;
    }

    @Bean
    public MarshallingMessageConverter xmlMessageConverter(Jaxb2Marshaller marshaller) {
        return new MarshallingMessageConverter(marshaller, marshaller);
    }

    @Bean(name = "receiveConnectionFactory")
    public CachingConnectionFactory receiveConnectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory("rabbitmq2", 5672);
        return factory;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            @Qualifier("receiveConnectionFactory") CachingConnectionFactory receiveConnectionFactory) {

        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(receiveConnectionFactory);
        factory.setMessageConverter(xmlMessageConverter(jaxb2Marshaller()));
        return factory;
    }

}