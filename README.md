# 规则引擎 Drools Demo

    Springboot 整合 Drools + redis
    
    食用此demo 请务必 确保 你已经安装了redis
    在application.properties 需要填入redis的相关配置

    此demo 仅供学习 如有不合理的地方 还望 提出 一起交流学习。
    

#### 软件架构
<p align="center">
 <img src="https://img.shields.io/badge/Spring%20Boot-2.4.5-blue.svg" alt="Downloads">
 <img src="https://img.shields.io/badge/drools-7.53.0.Final-blue">
 <img src="https://img.shields.io/badge/spring--data--redis-2.4.8-blue">
</p>

## 1.规则引擎概述

    规则引擎，全称为业务规则管理系统，英文名为BRMS(即Business Rule Management System)。
    规则引擎的主要思想是将应用程序中的业务决策部分分离出来，并使用预定义的语义模块编写业务决策（业务规则），
    由用户或开发者在需要时进行配置、管理。
    
    需要注意的是规则引擎并不是一个具体的技术框架，而是指的一类系统，即业务规则管理系统。
    目前市面上具体的规则引擎产品有：drools、VisualRules、iLog等。
    
    规则引擎实现了将业务决策从应用程序代码中分离出来，接收数据输入，解释业务规则，并根据业务规则做出业务决策。
    规则引擎其实就是一个输入输出平台。

## 2. 使用规则引擎的优势
    使用规则引擎的优势如下：
    
    1、业务规则与系统代码分离，实现业务规则的集中管理
    
    2、在不重启服务的情况下可随时对业务规则进行扩展和维护
    
    3、可以动态修改业务规则，从而快速响应需求变更
    
    4、规则引擎是相对独立的，只关心业务规则，使得业务分析人员也可以参与编辑、维护系统的业务规则
    
    5、减少了硬编码业务规则的成本和风险
    
    6、使用规则引擎提供的规则编辑工具，使复杂的业务规则实现变得的简单


###  食用方法

1.  在src/main/resources/static/ 目录下 有 rule1.drl 和rule2.drl 两个规则文件

    可使用redis管理工具将这两个文件的内容添加到自己的redis中 类型为string
    
    键值取为 drools:rule1 和drools:rule2 即可 或者drools:xxx  xxx为任意
    
    确保为drools: 开头即可
    
    src/main/resources/rules/  中的 demo.drl 无需关心
    
    它的存在只是为了证明 Working Memory中 相同的ruleName 会被覆盖
    


2. 打开IndexController 可查看相应的demo

    demo1
        http://127.0.0.1:8080/test
    
    demo2
        http://127.0.0.1:8080/test2
    
    动态修改规则
    
    添加规则
        http://127.0.0.1:8080/add
    
    删除规则
        http://127.0.0.1:8080/del


3. 入门案例

   通过一个Drools入门案例来让大家初步了解Drools的使用方式、对Drools有一个整体概念

创建Employee 模型
```aidl

@Getter
@Setter
public class Employee {

    private Long id;

    private String name; //名字
    private int age;     //年龄
    private String sex;  //性别
    private int year;    //工作年限
    private int salary;  //工资
    public Employee(){}
    public Employee(Long id, String name, int age, String sex, int year, int salary) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.year = year;
        this.salary = salary;
    }
}


```

创建DroolsConfig

```aidl

@Slf4j
@Configuration
public class DroolsConfig {

    //指定规则文件存放的目录
    private static final String RULES_PATH = "rules/";
    private final KieServices kieServices = KieServices.Factory.get();

    @Bean
    @ConditionalOnMissingBean
    public KieFileSystem kieFileSystem() throws IOException {
        //这里修改 drools.dateformat 日期格式 为了可以使用 date-effective
        System.setProperty("drools.dateformat","yyyy-MM-dd HH:mm:ss");
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        Resource[] files = resourcePatternResolver.getResources("classpath*:" + RULES_PATH + "*.*");
        for (Resource file : files) {
            kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_PATH + file.getFilename(), "UTF-8"));
        }
        return kieFileSystem;
    }

    @Bean
    @ConditionalOnMissingBean
    public KieContainer kieContainer() throws Exception {
        KieRepository kieRepository = kieServices.getRepository();
        kieRepository.addKieModule(kieRepository::getDefaultReleaseId);
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem());
        kieBuilder.buildAll();
        return kieServices.newKieContainer(kieRepository.getDefaultReleaseId());
    }

    @Bean
    @ConditionalOnMissingBean
    public KieBase kieBase() throws Exception {
        return kieContainer().getKieBase();
    }

    @Bean
    @ConditionalOnMissingBean
    public KModuleBeanFactoryPostProcessor kiePostProcessor() {
        return new KModuleBeanFactoryPostProcessor();
    }

}

```

rule1.drl

```aidl

package helloworld
import io.springboot.drools.model.Employee

rule "rule_1"
    when
        eval(true)
    then
        System.out.println("规则 ：rule1 .");
end
//不带参数的查询
//当前query用于查询Working Memory中salary <= 10000的Employee对象
query "query_1"
    $employee:Employee(salary <= 10000)
end

//带有参数的查询
//当前query用于查询Working Memory中year>3同时name需要和传递的参数name相同的employee对象
query "query_2"(String ename)
    $employee:Employee(year > 3 && name == ename)
end


```

rule2

```aidl

package helloworld

import io.springboot.drools.model.Employee

rule "rule_2"
    salience 10
    when
        eval(true)
    then
        System.out.println("规则 ：rule2 .");
end

rule "rule_employee_dismiss"
    agenda-group "group-rule1"
    when
        $employee:Employee(age > 35)
    then
        System.out.println("ID 为 "+ $employee.getId() + " 的 "+ $employee.getName() + "年龄超过35岁需要被开除了");
end
//no-loop true 很关键 不加就一直循环涨到到工资10100 老板必不可能这么好
//agenda-group "xxx" 议程分组
rule "rule_employee_raise"
    agenda-group "group-rule2"
    no-loop true
    when
        $employee:Employee(year >= 3 && salary <= 10000)
    then
        System.out.println("抠门公司准备给员工ID="+$employee.getId()+" ,名字= "+$employee.getName()+" 加薪");
        $employee.setSalary($employee.getSalary() + 100);
        update($employee);

end

```

编写一个controller

```aidl

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

```


浏览器 访问 http://127.0.0.1:8080/test 即可在控制台看到规则匹配结果

>query_1查询到符合条件的人 ：小明

>query_1查询到符合条件的人 ：小刚

>query_2 查询到：{"age":36,"id":2,"name":"小刚","salary":20000,"sex":"男","year":8}

>ID 为 2 的 小刚年龄超过35岁需要被开除了

>规则 ：rule2 .

>本地规则：HelloWorld ！

>规则 ：rule1 .



其中 来自redis中 rule1.drl 的有以下内容

>query_1查询到符合条件的人 ：小明

>query_1查询到符合条件的人 ：小刚

>query_2 查询到：{"age":36,"id":2,"name":"小刚","salary":20000,"sex":"男","year":8}

>规则 ：rule1 .


来自redis中 rule2.drl 的有以下内容

>ID 为 2 的 小刚年龄超过35岁需要被开除了

>规则 ：rule2 .

来自 本地 demo.drl 的有以下内容

>本地规则：HelloWorld ！


如果 demo1() 中的 session.fireAllRules();注释掉

将只会输出

>query_1查询到符合条件的人 ：小明

>query_1查询到符合条件的人 ：小刚

>query_2 查询到：{"age":36,"id":2,"name":"小刚","salary":20000,"sex":"男","year":8}

因此可知 session.fireAllRules() 是用来触发匹配规则的 

query是 query , rule 是 rule 切勿搞混。
