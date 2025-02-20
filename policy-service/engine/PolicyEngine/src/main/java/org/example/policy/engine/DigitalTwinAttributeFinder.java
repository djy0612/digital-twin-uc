package org.example.policy.engine;

import org.wso2.balana.attr.*;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.finder.AttributeFinderModule;
import java.net.URI;
import java.util.*;

public class DigitalTwinAttributeFinder extends AttributeFinderModule {

    // 模拟动态属性存储（实际应从数据库或上下文获取）
    private static final Map<String, Map<String, AttributeValue>> attributeStore = new HashMap<>();

    static {
        // 策略需要的核心属性定义
        Map<String, AttributeValue> user1Attrs = new HashMap<>();
        user1Attrs.put("urn:oasis:names:tc:xacml:1.0:subject:subject-id", new StringAttribute("user1"));
        user1Attrs.put("urn:oasis:names:tc:xacml:1.0:action:action-id", new StringAttribute("read"));

        Map<String, AttributeValue> resource1Attrs = new HashMap<>();
        resource1Attrs.put("urn:oasis:names:tc:xacml:1.0:resource:resource-id", new StringAttribute("resource1"));
        resource1Attrs.put("http://kmarket.com/id/amount", new IntegerAttribute(5));
        resource1Attrs.put("http://kmarket.com/id/totalAmount", new IntegerAttribute(500));

        attributeStore.put("user1", user1Attrs);
        attributeStore.put("resource1", resource1Attrs);
    }

    @Override
    public boolean isDesignatorSupported() {
        return true;
    }

    @Override
    public Set<String> getSupportedCategories() {
        return Set.of(
            "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject",
            "urn:oasis:names:tc:xacml:3.0:attribute-category:resource",
            "urn:oasis:names:tc:xacml:3.0:attribute-category:action",
            "http://kmarket.com/category"
        );
    }

    @Override
    public EvaluationResult findAttribute(URI attributeType, URI attributeId,
            String issuer, URI category, EvaluationCtx context) {
        
        String categoryUri = category.toString();
        String attributeIdStr = attributeId.toString();

        // 精确匹配策略中定义的 AttributeDesignator 属性
        switch (categoryUri) {
            case "urn:oasis:names:tc:xacml:1.0:subject-category:access-subject":
                return handleSubjectAttributes(attributeIdStr);
            case "urn:oasis:names:tc:xacml:3.0:attribute-category:resource":
                return handleResourceAttributes(attributeIdStr);
            case "http://kmarket.com/category":
                return handleCustomAttributes(attributeIdStr);
            case "urn:oasis:names:tc:xacml:3.0:attribute-category:action":
                return handleActionAttributes(attributeIdStr);
            default:
                return emptyBagResult();
        }
    }

    // 用户属性处理（匹配策略中的 subject-id）
    private EvaluationResult handleSubjectAttributes(String attributeId) {
        Map<String, AttributeValue> attrs = attributeStore.get("user1");
        return getAttributeResult(attrs, attributeId, "string");
    }

    // 资源属性处理（匹配策略中的 resource-id）
    private EvaluationResult handleResourceAttributes(String attributeId) {
        Map<String, AttributeValue> attrs = attributeStore.get("resource1");
        return getAttributeResult(attrs, attributeId, "string");
    }

    // 自定义业务属性（如 KMarket 的 amount 和 totalAmount）
    private EvaluationResult handleCustomAttributes(String attributeId) {
        Map<String, AttributeValue> attrs = attributeStore.get("resource1");
        return getAttributeResult(attrs, attributeId, "integer");
    }

    // 新增操作属性处理（匹配策略中的 action-id）
    private EvaluationResult handleActionAttributes(String attributeId) {
        Map<String, AttributeValue> attrs = attributeStore.get("user1");
        return getAttributeResult(attrs, attributeId, "string");
    }

    // 统一属性结果处理
    private EvaluationResult getAttributeResult(Map<String, AttributeValue> attrs, String attributeId, String dataType) {
        if (attrs != null && attrs.containsKey(attributeId)) {
            BagAttribute bag = new BagAttribute(
                URI.create("http://www.w3.org/2001/XMLSchema#" + dataType),
                Collections.singletonList(attrs.get(attributeId))
            );
            return new EvaluationResult(bag);
        }
        return emptyBagResult();
    }

    private EvaluationResult emptyBagResult() {
        return new EvaluationResult(BagAttribute.createEmptyBag(URI.create("string")));
    }
}