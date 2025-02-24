package org.example.policy.engine;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.*;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.ResponseCtx;
import org.wso2.balana.finder.*;
import org.xml.sax.InputSource;
import org.wso2.balana.ctx.EvaluationCtx;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
public class DigitalTwinPolicyEngine {
    private Balana balana;
    private PDP pdp;
    private AttributeFinder attributeFinder;
    private PolicyFinder policyFinder;
    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinPolicyEngine.class);
    public DigitalTwinPolicyEngine() {
        initBalana();
    }

    private void initBalana() {
        try {
            // 初始化 Balana
            balana = Balana.getInstance();

            // 配置策略查找器
            policyFinder = new PolicyFinder();
            ChainPolicyFinderModule chainFinder = new ChainPolicyFinderModule();
            policyFinder.setModules(Collections.singleton(chainFinder));

            // 配置属性查找器
            attributeFinder = new AttributeFinder();
            List<AttributeFinderModule> finderModules = new ArrayList<>();
            finderModules.add(new DigitalTwinAttributeFinder());
            attributeFinder.setModules(finderModules);

            // 创建 PDP 配置
            PDPConfig pdpConfig = new PDPConfig(attributeFinder, policyFinder, null, true);
            pdp = new PDP(pdpConfig);
        } catch (Exception e) {
            throw new RuntimeException("策略查找器初始化失败", e);
        }
    }

private String fetchPolicyFromFabric(String policyId) {
    try {
        URL url = new URL("http://localhost:8080/policies/" + policyId);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");

        int responseCode = conn.getResponseCode();
        logger.info("Fetching policy from Fabric-Client, policyId: {}, responseCode: {}", policyId, responseCode);
        if (responseCode != 200) {
            throw new RuntimeException("Failed to fetch policy, response code: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
        }
        conn.disconnect();

        String jsonResponse = response.toString();
        logger.debug("Fabric-Client response: {}", jsonResponse);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(jsonResponse);
        JsonNode dataNode = root.path("data");
        if (dataNode.isMissingNode()) {
            throw new RuntimeException("Data node not found in response: " + jsonResponse);
        }
        JsonNode contentNode = dataNode.path("content");
        if (contentNode.isMissingNode()) {
            throw new RuntimeException("Policy content not found in response: " + jsonResponse);
        }
        String policyContent = contentNode.asText();
        logger.debug("Extracted policyContent: {}", policyContent);

        // 验证是否为有效 XML
        if (policyContent.trim().isEmpty() || !policyContent.startsWith("<")) {
            throw new RuntimeException("Invalid policy content: " + policyContent);
        }

        return policyContent;
    } catch (Exception e) {
        logger.error("Failed to fetch policy from Fabric-Client: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to fetch policy from Fabric-Client: " + e.getMessage(), e);
    }
}
    public String evaluate(String request) {
        if (pdp == null) {
            initBalana();
        }

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(request)));
            Element rootElement = doc.getDocumentElement();

            String policyId = extractPolicyIdFromRequest(rootElement);
            if (policyId == null) {
                throw new IllegalArgumentException("Policy ID not found in request");
            }

            String policyContent = fetchPolicyFromFabric(policyId);
            ((ChainPolicyFinderModule) policyFinder.getModules().iterator().next()).updatePolicy(policyContent);

            AbstractRequestCtx requestCtx = RequestCtxFactory.getFactory().getRequestCtx(rootElement);
            ResponseCtx responseCtx = pdp.evaluate(requestCtx);

            return responseCtx.encode();
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating request", e);
        }
    }

    public static boolean validatePolicy(String policyContent) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            dbf.setNamespaceAware(true);

            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(policyContent)));

            Element root = doc.getDocumentElement();
            String localName = root.getLocalName();
            String namespace = root.getNamespaceURI();

            if (!"Policy".equals(localName) && !"PolicySet".equals(localName)) {
                return false;
            }
            if (!"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17".equals(namespace)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String uploadPolicy(String policyContent) {
        if (!validatePolicy(policyContent)) {
            throw new IllegalArgumentException("Invalid policy format");
        }

        try {
            URL url = new URL("http://localhost:8080/policies/uploadPolicy");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/xml");
            conn.setDoOutput(true);

            logger.info("Uploading policy to Fabric-Client: {}", policyContent.substring(0, Math.min(50, policyContent.length())));

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = policyContent.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            String responseMessage = conn.getResponseMessage();
            logger.info("Fabric-Client response: code={}, message={}", responseCode, responseMessage);

            String policyId = null;
            if (responseCode == 200) {
                // 读取响应内容以提取 policyId
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    String responseBody = response.toString();
                    logger.info("Fabric-Client response body: {}", responseBody);

                    // 提取 policyId，假设响应格式为 "Policy uploaded successfully with ID: <policyId>"
                    int idStart = responseBody.indexOf("ID: ") + 4;
                    if (idStart > 3) { // 确保找到了 "ID: "
                        policyId = responseBody.substring(idStart).trim();
                        logger.info("Extracted policyId: {}", policyId);
                    } else {
                        logger.warn("Could not extract policyId from response: {}", responseBody);
                    }
                }
            } else {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    logger.error("Upload failed with error response: {}", errorResponse.toString());
                    throw new RuntimeException("Upload failed with status " + responseCode + ": " + errorResponse.toString());
                }
            }

            conn.disconnect();

            if (policyId == null) {
                throw new RuntimeException("Failed to extract policyId from Fabric-Client response");
            }

            return policyId;
        } catch (Exception e) {
            logger.error("Failed to upload policy to Fabric-Client: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload policy to Fabric-Client", e);
        }
    }

    private String extractPolicyIdFromRequest(Element rootElement) {
        org.w3c.dom.NodeList attributes = rootElement.getElementsByTagNameNS(
            "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", "Attribute");
        for (int i = 0; i < attributes.getLength(); i++) {
            Element attr = (Element) attributes.item(i);
            String attrId = attr.getAttribute("AttributeId");
            if ("policyId".equals(attrId)) {
                Element value = (Element) attr.getElementsByTagNameNS(
                    "urn:oasis:names:tc:xacml:3.0:core:schema:wd-17", "AttributeValue").item(0);
                return value.getTextContent();
            }
        }
        return null;
    }

    private class ChainPolicyFinderModule extends PolicyFinderModule {
        private AbstractPolicy policy;

        public void updatePolicy(String policyContent) throws Exception {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            Document doc = dbf.newDocumentBuilder().parse(new InputSource(new StringReader(policyContent)));
            Element rootElement = doc.getDocumentElement();

            // 使用 Policy.getInstance 解析策略
            policy = Policy.getInstance(rootElement);
        }

        @Override
        public void init(PolicyFinder finder) {
            // 初始化方法
        }

        @Override
        public PolicyFinderResult findPolicy(EvaluationCtx context) { // 修正参数类型为 EvaluationCtx
            if (policy == null) {
                return new PolicyFinderResult();
            }
            return new PolicyFinderResult(policy);
        }

        @Override
        public boolean isRequestSupported() {
            return true;
        }
    }
}