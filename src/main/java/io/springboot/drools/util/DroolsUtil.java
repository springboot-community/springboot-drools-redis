package io.springboot.drools.util;

import lombok.extern.slf4j.Slf4j;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

/**
 * @author wxm
 * @version 1.0
 * @since 2021/5/11 9:20
 */
@Slf4j
public class DroolsUtil {

    /**
     * 删除规则
     * @param packageName
     * @param ruleName
     * @param kieBase
     */
    public static void delRule( String packageName,  String ruleName , KieBase kieBase){
        try {
            kieBase.removeRule(packageName, ruleName);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 添加规则
     * 会覆盖本地规则中 包名和规则名 相同的规则
     * @param content
     * @param kieBase
     */
    public static void addRules(String content, KieBase kieBase) {
        try {
            KnowledgeBaseImpl knowledgeBase =(KnowledgeBaseImpl) kieBase;
            KnowledgeBuilder kb = KnowledgeBuilderFactory.newKnowledgeBuilder();
            kb.add(ResourceFactory.newByteArrayResource(content.getBytes("utf-8")), ResourceType.DRL);
            if (kb.hasErrors()) {
                log.info("ADD RULE FILE ERROR: " + kb.getErrors().toString());
            }
            knowledgeBase.addPackages(kb.getKnowledgePackages());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
