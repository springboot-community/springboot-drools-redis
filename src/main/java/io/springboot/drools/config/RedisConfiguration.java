package io.springboot.drools.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author wxm
 * @version 1.0
 * @since 2021/5/11 9:20
 */
@Configuration
public class RedisConfiguration {

    @Bean("redisConnectionFactory")
    public RedisConnectionFactory configRedisConnectionFactory0(@Value("${spring.redis.host}") String host,
                                                                @Value("${spring.redis.port}") int port,
                                                                @Value("${spring.redis.password}") String password,
                                                                @Value("${spring.redis.database}") int database) {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        configuration.setPassword(password);
        configuration.setDatabase(database);
        JedisConnectionFactory connectionFactory = new JedisConnectionFactory(configuration);
        //System.err.println(connectionFactory.getPoolConfig().getMaxIdle());
        return connectionFactory;
    }

    @Bean("stringRedisTemplate")
    public StringRedisTemplate configStringRedisTemplate0(RedisConnectionFactory redisConnectionFactory) {
        return new StringRedisTemplate(redisConnectionFactory);
    }

}
