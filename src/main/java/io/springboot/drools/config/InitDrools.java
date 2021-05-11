package io.springboot.drools.config;

import io.springboot.drools.util.DroolsUtil;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

/**
 * @author wxm
 * @version 1.0
 * @since 2021/5/11 9:20
 */
@Slf4j
@Component
public class InitDrools {

    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Autowired
    private KieBase kieBase;

    @Value("${drools.redis.keys:drools:}")
    private String matchKey;

    /**
     * 初始化 redis 中的drools规则 [这里可以覆盖规则]
     * redis 中的 规则 如果和 本地demo.drl文件中的包名规则名相同是会覆盖的
     * <p>
     * DroolsConfig 配置中 KieFileSystem.write写入的规则文件如果规则名相同 则启动容器失败
     * {@link io.springboot.drools.config.DroolsConfig}
     */
    @PostConstruct
    public void initRedisDroolsRules() {
        Set<String> keys = scan(stringRedisTemplate, matchKey);
       // List<String> keys = Arrays.asList("drools:rule1".split(","));
        List<String> contents = this.stringRedisTemplate.opsForValue().multiGet(keys);
        for (String content : contents) {
            log.info("Get Redis Rules content :  {}", content);
            if (content != null) {
                DroolsUtil.addRules(content, kieBase);
            }
        }

    }

    /**
     * scan 全部的key
     * @param stringRedisTemplate
     * @param matchKey
     * @return
     */
    public Set<String> scan(StringRedisTemplate stringRedisTemplate, String matchKey) {
        Set<String> keys = stringRedisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> temp = new HashSet<>();
            Cursor<byte[]> cursor = connection.scan(new ScanOptions.ScanOptionsBuilder().match(matchKey + "*").count(Integer.MAX_VALUE).build());
            while (cursor.hasNext()) {
                temp.add(new String(cursor.next()));
            }
            try {
                cursor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return temp;
        });

        return keys;
    }





}