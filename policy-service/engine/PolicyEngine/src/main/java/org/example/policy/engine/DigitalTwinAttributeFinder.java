package org.example.policy.engine;

import org.wso2.balana.attr.*;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.EvaluationCtx;
import org.wso2.balana.finder.AttributeFinderModule;
import java.net.URI;
import java.util.*;

public class DigitalTwinAttributeFinder extends AttributeFinderModule {
    private static Map<String, List<String>> userAttributes;
    
    static {
        userAttributes = new HashMap<>();
        // 初始化示例用户属性
        List<String> adminAttrs = new ArrayList<>();
        adminAttrs.add("admin");
        userAttributes.put("admin", adminAttrs);
        
        List<String> operatorAttrs = new ArrayList<>();
        operatorAttrs.add("operator");
        userAttributes.put("operator", operatorAttrs);
    }
    
    @Override
    public boolean isDesignatorSupported() {
        return true;
    }
    
    @Override
    public Set<String> getSupportedCategories() {
        Set<String> categories = new HashSet<>();
        categories.add("urn:oasis:names:tc:xacml:1.0:subject-category:access-subject");
        return categories;
    }
    
    @Override
    public EvaluationResult findAttribute(URI attributeType, URI attributeId,
            String issuer, URI category, EvaluationCtx context) {
        // Get attribute values
        EvaluationResult result = context.getAttribute(attributeId, category, issuer, null);
        if (result == null || result.indeterminate() || result.getAttributeValue() == null) {
            return new EvaluationResult(BagAttribute.createEmptyBag(attributeType));
        }
        
        String userId = result.getAttributeValue().encode();
        
        // Find user attributes
        List<String> attrs = userAttributes.get(userId);
        if (attrs == null) {
            return new EvaluationResult(BagAttribute.createEmptyBag(attributeType));
        }
        
        // Create attribute value list
        List<AttributeValue> attrValues = new ArrayList<>();
        for (String attr : attrs) {
            attrValues.add(new StringAttribute(attr));
        }
        
        return new EvaluationResult(new BagAttribute(attributeType, attrValues));
    }
}