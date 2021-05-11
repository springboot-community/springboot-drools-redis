package io.springboot.drools.controller;

import com.alibaba.fastjson.JSON;
import io.springboot.drools.model.Employee;
import io.springboot.drools.util.DroolsUtil;
import lombok.extern.slf4j.Slf4j;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;


/**
 * @author wxm
 * @version 1.0
 * @since 2021/5/11 9:20
 */
@Slf4j
@RestController
public class IndexController {
    @Autowired
    private KieBase kieBase;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * demo1
     * @return
     */
    @RequestMapping("/test")
    public String demo1(){
        Employee xm = new Employee(1L,"小明",26,"男",3,8000);

        Employee xg1 = new Employee(2L,"小刚",36,"男",8,20000);

        Employee xg2 = new Employee(3L,"小刚",27,"男",3,9000);

        KieSession session = kieBase.newKieSession();

        //将对象插入Working Memory中
        session.insert(xm);
        session.insert(xg1);
        session.insert(xg2);

        QueryResults results1 = session.getQueryResults("query_1");
        for (QueryResultsRow row : results1) {
            Employee employee = (Employee) row.get("$employee");
            System.out.println("query_1查询到符合条件的人 ：" + employee.getName());
        }
        //调用规则文件中的查询 检查 是否有小刚 大于35岁
        QueryResults results2 = session.getQueryResults("query_2","小刚");
        for (QueryResultsRow row : results2) {
            Employee employee = (Employee) row.get("$employee");
            System.out.println("query_2 查询到：" +JSON.toJSONString(employee));
        }
        //指定议程分组，只有获取焦点的组中的规则才有可能触发
        session.getAgenda().getAgendaGroup("group-rule1").setFocus();
        session.fireAllRules();
        session.dispose();

        return "ok";
    }


    /**
     * demo2
     * @return
     */
    @RequestMapping("/test2")
    public String test2(){
        Employee xm = new Employee(1L,"小明",26,"男",3,8000);

        Employee xg1 = new Employee(2L,"小刚",36,"男",8,20000);

        Employee xg2 = new Employee(3L,"小刚",27,"男",3,9000);

        KieSession session = kieBase.newKieSession();

        //将对象插入Working Memory中
        session.insert(xm);
        session.insert(xg1);
        session.insert(xg2);

        //指定议程分组，只有获取焦点的组中的规则才有可能触发
        session.getAgenda().getAgendaGroup("group-rule2").setFocus();
        session.fireAllRules();
        System.out.println(JSON.toJSONString(xm));
        System.out.println(JSON.toJSONString(xg1));
        System.out.println(JSON.toJSONString(xg2));

        session.dispose();
        return "ok";
    }

    @RequestMapping("/index")
    public String index() throws Exception{

        KieSession session = kieBase.newKieSession();
        session.fireAllRules();
        session.dispose();

        return "ok";
    }


    @Value("${drools.redis.keys:drools:}")
    private String keyPrefix;

    /**
     * 添加规则
     * @param content
     * @return
     */
    @RequestMapping("/add/content")
    public String addRuleContent(@RequestParam(value = "content") String content){
        DroolsUtil.addRules(content, kieBase);
        return "ok";
    }

    /**
     * 添加规则
     * @param packageName
     * @param ruleName
     * @return
     * @throws Exception
     */
    @RequestMapping("/add")
    public String addRule(@RequestParam(value = "packageName", defaultValue = "helloworld", required = false) String packageName,
                          @RequestParam(value = "ruleName", defaultValue = "helloworld", required = false) String ruleName) throws Exception {
        String temp = UUID.randomUUID().toString();
        String content = "package " + packageName + ";"
                + "\n"
                + "rule \"" + ruleName + "\"\n"
                + "when\n"
                + "eval(true)\n"
                + "then \n"
                + " System.out.println(\"-------" + temp + "---\");\n"
                + "end";

        //可以存到redis中 下次服务重启 会自动初始化规则
        //划重点 -- packageName 下的 ruleName 务必唯一 如果redis中存在多个ruleName相同 将无法初始化规则
        //如果持久化到redis 删除的时候也添加上 redis的操作即可
        // stringRedisTemplate.opsForValue().set(keyPrefix + packageName + ":"+ ruleName, drlStr); //添加
        // stringRedisTemplate.delete(keyPrefix + packageName + ":"+ ruleName);  //删除
        String key = keyPrefix + packageName + ":"+ ruleName;

        stringRedisTemplate.opsForValue().set(key, content);

        DroolsUtil.addRules(content, kieBase);

        //匹配一次规则查看 是否添加上了
        index();

        return "ok";
    }



    @RequestMapping("/del")
    public String delRule(@RequestParam(value = "packageName", defaultValue = "helloworld", required = false)String packageName,
                          @RequestParam(value = "ruleName", defaultValue = "helloworld", required = false)String ruleName) throws Exception{

        DroolsUtil.delRule(packageName, ruleName, kieBase);
        String key = keyPrefix + packageName + ":"+ ruleName;
        stringRedisTemplate.delete(key);
        //匹配一次规则查看 是否添加上了
        index();
        return "ok";
    }
}
