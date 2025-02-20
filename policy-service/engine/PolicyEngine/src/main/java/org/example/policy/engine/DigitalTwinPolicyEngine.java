package org.example.policy.engine;

import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.Policy;
import org.wso2.balana.finder.*;
import org.wso2.balana.finder.impl.*;
import javax.xml.parsers.*;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.xml.XMLConstants;  // 解决 XMLConstants 找不到
import org.w3c.dom.Element;     // 解决 Element 找不到
public class DigitalTwinPolicyEngine {
    private Balana balana;
    private PDP pdp;
    private AttributeFinder attributeFinder;
    
    public DigitalTwinPolicyEngine() {
        initBalana();
    }

    private void initBalana() {
        try {
            // 设置策略路径
            String policyLocation = new File("src/test/samples").getCanonicalPath();
            System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY, policyLocation);

            // 初始化 Balana
            balana = Balana.getInstance();

            // 配置策略查找器
            PolicyFinder policyFinder = new PolicyFinder();
            FileBasedPolicyFinderModule fileFinder = new FileBasedPolicyFinderModule();
            policyFinder.setModules(Collections.singleton(fileFinder));

            // 配置属性查找器（关键修正）
            attributeFinder = new AttributeFinder();
            List<AttributeFinderModule> finderModules = new ArrayList<>();
            finderModules.add(new DigitalTwinAttributeFinder());
            attributeFinder.setModules(finderModules);

            // 创建 PDP 配置
            PDPConfig pdpConfig = new PDPConfig(attributeFinder, policyFinder, null, true);
            pdp = new PDP(pdpConfig);

        } catch (IOException e) {
            throw new RuntimeException("策略存储库路径错误: " + e.getMessage(), e);
        } catch (Exception e) {  
            throw new RuntimeException("策略查找器初始化失败", e);
        }
    }



    public String evaluate(String request) {
        if (pdp == null) {
            initBalana();
        }
        
        try {
            // 1. 配置安全的 XML 解析器
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);  // 关键配置！
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // 2. 解析请求 XML 并获取根元素
            Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(request)));
            Element rootElement = doc.getDocumentElement();  // 获取根元素 <Request>

            // 3. 创建请求上下文
            AbstractRequestCtx requestCtx = RequestCtxFactory.getFactory().getRequestCtx(rootElement);
            ResponseCtx responseCtx = pdp.evaluate(requestCtx);

            return responseCtx.encode();
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating request", e);
        }
    }

    public static boolean validatePolicy(String policyContent) {
        try {
            // ========================
            // 1. 安全配置XML解析器
            // ========================
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            
            // 防止XXE攻击
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            
            // 启用命名空间支持
            dbf.setNamespaceAware(true);

            // ========================
            // 2. 解析XML并验证基础结构
            // ========================
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(policyContent)));
            
            // 检查根元素
            Element root = doc.getDocumentElement();
            String localName = root.getLocalName();
            String namespace = root.getNamespaceURI();
            
            // 必需条件1: 根元素为 Policy 或 PolicySet
            if (!"Policy".equals(localName) && !"PolicySet".equals(localName)) {
                return false;
            }
            
            // 必需条件2: 命名空间为XACML 3.0标准
            if (!"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17".equals(namespace)) {
                return false;
            }

            return true;
            
        } catch (Exception e) {
            // 任何解析错误直接返回验证失败
            return false;
        }
    }

}