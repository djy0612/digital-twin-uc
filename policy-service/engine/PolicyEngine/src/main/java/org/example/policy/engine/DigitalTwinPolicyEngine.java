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

public class DigitalTwinPolicyEngine {
    private Balana balana;
    private PDP pdp;
    private AttributeFinder attributeFinder;
    
    public DigitalTwinPolicyEngine() {
        initBalana();
    }

    private void initBalana() {
        // Initialize Balana instance
        balana = Balana.getInstance();
        
        // Configure policy finder
        PolicyFinder policyFinder = new PolicyFinder();
        Set<PolicyFinderModule> policyModules = new HashSet<>();
        FileBasedPolicyFinderModule fileBasedPolicyFinderModule = new FileBasedPolicyFinderModule();
        policyModules.add(fileBasedPolicyFinderModule);
        policyFinder.setModules(policyModules);

        // Configure attribute finder
        attributeFinder = new AttributeFinder();
        Set<AttributeFinderModule> finderModules = new HashSet<>();
        finderModules.add(new DigitalTwinAttributeFinder());
        attributeFinder.setModules(new ArrayList<>(finderModules));

        // Create PDP configuration
        PDPConfig pdpConfig = new PDPConfig(attributeFinder, policyFinder, null, true);
        pdp = new PDP(pdpConfig);
    }

    public String evaluate(String request) {
        if (pdp == null) {
            initBalana();
        }
        
        try {
            // Create DOM document from request string
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(request)));
            
            // Create request context using factory
            AbstractRequestCtx requestCtx = RequestCtxFactory.getFactory().getRequestCtx(doc);
            ResponseCtx responseCtx = pdp.evaluate(requestCtx);
            
            return responseCtx.encode();
        } catch (Exception e) {
            throw new RuntimeException("Error evaluating request", e);
        }
    }

    public static boolean validatePolicy(String policyContent) {
        try {
            // Create InputSource for policy parsing
            InputSource source = new InputSource(new StringReader(policyContent));
            
            // Parse policy
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(source);
            Policy policy = Policy.getInstance(doc);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}