package org.example.adapterwebsocket.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.policy.SimpleRetryPolicy;

import java.util.List;

import static org.example.adapterwebsocket.model.constant.RabbitConstants.K_CRYPTO_RATE_LIVE;
import static org.example.adapterwebsocket.model.constant.RabbitConstants.Q_CRYPTO_RATE_LIVE;
import static org.example.adapterwebsocket.model.constant.RabbitConstants.X_DBS_WEBSOCKET;

@Configuration
@Getter
@Setter
public class RabbitConfig {

    @Bean
    public DirectExchange websocketExchange() {
        return new DirectExchange(X_DBS_WEBSOCKET);
    }

    @Bean
    public Queue cryptoRateStreamQueue() {
        return new Queue(Q_CRYPTO_RATE_LIVE);
    }

    @Bean
    public Binding cryptoRateStreamBinding() {
        return BindingBuilder.bind(cryptoRateStreamQueue())
                .to(websocketExchange())
                .with(K_CRYPTO_RATE_LIVE);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        rabbitTemplate.setObservationEnabled(true);
        return rabbitTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return new Jackson2JsonMessageConverter(mapper);
    }
}
