package ru.trushkov.crack_manager.configuration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfiguration {
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create("mongodb://mongo1:27017,mongo2:27018,mongo3:27019/manager?replicaSet=rs0");
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(mongoClient(), "manager");
    }
}
