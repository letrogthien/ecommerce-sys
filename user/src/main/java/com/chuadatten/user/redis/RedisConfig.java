package com.chuadatten.user.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.chuadatten.user.entity.TrustingDevice;
import com.chuadatten.user.entity.WhiteList;
import com.chuadatten.user.otp.OtpModel;


@Configuration
public class RedisConfig {
    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6379);
        return new LettuceConnectionFactory(config);
    }

    @Bean
    public WhiteListTemplate whiteListTemplate(LettuceConnectionFactory connectionFactory) {
        WhiteListTemplate template = new WhiteListTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(WhiteList.class));
        return template;
    }

    @Bean
    public DeviceRedisTemplate deviceRedisTemplate(LettuceConnectionFactory connectionFactory) {
        DeviceRedisTemplate template = new DeviceRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(TrustingDevice.class));
        return template;
    }

    @Bean
    public OtpRedisTemplate otpRedisTemplate(LettuceConnectionFactory connectionFactory) {
        OtpRedisTemplate template = new OtpRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(OtpModel.class));
        return template;
    }
}
