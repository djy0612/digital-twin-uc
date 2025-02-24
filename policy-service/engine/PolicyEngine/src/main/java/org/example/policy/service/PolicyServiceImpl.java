package org.example.policy.service;

import io.grpc.stub.StreamObserver;
import org.example.policy.engine.DigitalTwinPolicyEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PolicyServiceImpl extends PolicyServiceGrpc.PolicyServiceImplBase {
    private static final Logger logger = LoggerFactory.getLogger(PolicyServiceImpl.class);
    private final DigitalTwinPolicyEngine policyEngine;

    public PolicyServiceImpl() {
        this.policyEngine = new DigitalTwinPolicyEngine();
    }

    @Override
    public void evaluatePolicy(PolicyRequest request, StreamObserver<PolicyResponse> responseObserver) {
        try {
            logger.info("Received evaluation request: {}", request.getRequestContent());
            String result = policyEngine.evaluate(request.getRequestContent());  // 修改此处
            PolicyResponse response = PolicyResponse.newBuilder()
                .setResponseContent(result)
                .setDecision(extractDecision(result))
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error evaluating policy", e);
            PolicyResponse response = PolicyResponse.newBuilder()
                .setErrorMessage(e.getMessage())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private String extractDecision(String xacmlResponse) {
        if (xacmlResponse.contains("Permit")) {
            return "Permit";
        } else if (xacmlResponse.contains("Deny")) {
            return "Deny";
        }
        return "NotApplicable";
    }

    @Override
    public void validatePolicy(PolicyRequest request, StreamObserver<PolicyResponse> responseObserver) {
        try {
            String policyContent = request.getPolicy();
            logger.info("validatePolicy 请求 | 策略前50字符: {}", policyContent.substring(0, Math.min(50, policyContent.length())));
            logger.debug("完整策略内容:\n{}", policyContent);  // 需要启用 DEBUG 级别日志
            boolean isValid = DigitalTwinPolicyEngine.validatePolicy(request.getPolicy());
            PolicyResponse response = PolicyResponse.newBuilder()
                .setValid(isValid)
                .setMessage(isValid ? "Policy is valid" : "Policy is invalid")
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            PolicyResponse response = PolicyResponse.newBuilder()
                .setValid(false)
                .setMessage("Error: " + e.getMessage())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    @Override
    public void validateRequest(ValidateRequest request, StreamObserver<ValidateResponse> responseObserver) {
        try {
            String result = policyEngine.evaluate(request.getRequest());
            ValidateResponse response = ValidateResponse.newBuilder()
                .setAllowed(result.contains("Permit"))
                .setMessage(result)
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            ValidateResponse response = ValidateResponse.newBuilder()
                .setAllowed(false)
                .setMessage("Error: " + e.getMessage())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    // 新增 uploadPolicy 方法
    @Override
    public void uploadPolicy(UploadPolicyRequest request, StreamObserver<UploadPolicyResponse> responseObserver) {
        try {
            logger.info("Received upload policy request, policy length: {}", request.getPolicyContent().length());
            String policyId = policyEngine.uploadPolicy(request.getPolicyContent());
            logger.info("Policy uploaded successfully with ID: {}", policyId);
            UploadPolicyResponse response = UploadPolicyResponse.newBuilder()
                .setSuccess(true)
                .setMessage("Policy uploaded successfully with ID: " + policyId)
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            logger.error("Error uploading policy", e);
            UploadPolicyResponse response = UploadPolicyResponse.newBuilder()
                .setSuccess(false)
                .setMessage("Error: " + e.getMessage())
                .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}