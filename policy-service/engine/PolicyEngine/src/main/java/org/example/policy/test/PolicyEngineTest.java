package org.example.policy.test;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.example.policy.service.PolicyRequest;
import org.example.policy.service.PolicyResponse;
import org.example.policy.service.PolicyServiceGrpc;

public class PolicyEngineTest {
    private static final String HOST = "localhost";
    private static final int PORT = 9090;

    public static void main(String[] args) {
        // 创建gRPC通道
        ManagedChannel channel = ManagedChannelBuilder.forAddress(HOST, PORT)
            .usePlaintext()
            .build();

        try {
            // 创建策略服务客户端
            PolicyServiceGrpc.PolicyServiceBlockingStub stub = PolicyServiceGrpc.newBlockingStub(channel);

            // 测试策略验证
            testPolicyValidation(stub);

            // 测试策略评估
            testPolicyEvaluation(stub);

        } finally {
            // 关闭通道
            channel.shutdown();
        }
    }

    private static void testPolicyValidation(PolicyServiceGrpc.PolicyServiceBlockingStub stub) {
        System.out.println("Testing policy validation...");
        
        String testPolicy = getTestPolicy();
        PolicyRequest request = PolicyRequest.newBuilder()
            .setPolicy(testPolicy)
            .build();

        try {
            PolicyResponse response = stub.validatePolicy(request);
            System.out.println("Policy validation result: " + response.getValid());
            System.out.println("Message: " + response.getMessage());
        } catch (Exception e) {
            System.err.println("Error validating policy: " + e.getMessage());
        }
    }

    private static void testPolicyEvaluation(PolicyServiceGrpc.PolicyServiceBlockingStub stub) {
        System.out.println("\nTesting policy evaluation...");
        
        String testRequest = getTestRequest();
        PolicyRequest request = PolicyRequest.newBuilder()
            .setRequestContent(testRequest)
            .build();

        try {
            PolicyResponse response = stub.evaluatePolicy(request);
            System.out.println("Policy evaluation result: " + response.getDecision());
            System.out.println("Response content: " + response.getResponseContent());
        } catch (Exception e) {
            System.err.println("Error evaluating policy: " + e.getMessage());
        }
    }

    private static String getTestPolicy() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<Policy xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"\n" +
               "        PolicyId=\"test-policy\"\n" +
               "        RuleCombiningAlgId=\"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable\"\n" +
               "        Version=\"1.0\">\n" +
               "    <Description>Test Policy</Description>\n" +
               "    <Target/>\n" +
               "    <Rule Effect=\"Permit\" RuleId=\"permit-rule\">\n" +
               "        <Target>\n" +
               "            <AnyOf>\n" +
               "                <AllOf>\n" +
               "                    <Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\">\n" +
               "                        <AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">read</AttributeValue>\n" +
               "                        <AttributeDesignator\n" +
               "                            AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\"\n" +
               "                            Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\"\n" +
               "                            DataType=\"http://www.w3.org/2001/XMLSchema#string\"\n" +
               "                            MustBePresent=\"true\"/>\n" +
               "                    </Match>\n" +
               "                </AllOf>\n" +
               "            </AnyOf>\n" +
               "        </Target>\n" +
               "    </Rule>\n" +
               "</Policy>";
    }

    private static String getTestRequest() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\">\n" +
               "    <Subject>\n" +
               "        <Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:subject:subject-id\">\n" +
               "            <AttributeValue>user1</AttributeValue>\n" +
               "        </Attribute>\n" +
               "    </Subject>\n" +
               "    <Resource>\n" +
               "        <Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:resource:resource-id\">\n" +
               "            <AttributeValue>resource1</AttributeValue>\n" +
               "        </Attribute>\n" +
               "    </Resource>\n" +
               "    <Action>\n" +
               "        <Attribute AttributeId=\"urn:oasis:names:tc:xacml:1.0:action:action-id\">\n" +
               "            <AttributeValue>read</AttributeValue>\n" +
               "        </Attribute>\n" +
               "    </Action>\n" +
               "</Request>";
    }
}
