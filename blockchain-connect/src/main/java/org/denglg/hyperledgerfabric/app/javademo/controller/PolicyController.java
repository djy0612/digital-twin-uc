package org.denglg.hyperledgerfabric.app.javademo.controller;

import org.apache.commons.codec.binary.StringUtils;
import org.denglg.hyperledgerfabric.app.javademo.pojo.BaseResponse;
import org.denglg.hyperledgerfabric.app.javademo.pojo.constant.ErrorCode;
import org.denglg.hyperledgerfabric.app.javademo.pojo.constant.*;
import org.denglg.hyperledgerfabric.app.javademo.pojo.request.*;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.sdk.Peer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;            // 解决 List 问题
import jakarta.validation.Valid;  // 解决 @Valid 问题
import org.springframework.http.ResponseEntity;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
@Slf4j
@RestController
@RequestMapping("/policies")
public class PolicyController {

    @Autowired
    public Contract contract;
    @Autowired
    public Network network;

    // 通过 Spring 自动注入
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    public PolicyController(Contract contract, Network network) {
        this.contract = contract;
        this.network = network;
    }

    /**
     * 创建策略接口
     */
    @PostMapping
    public BaseResponse<Policy> createPolicy(
            @Valid @RequestBody PolicyRequest request) 
            throws InterruptedException, TimeoutException {
        
        try {
            byte[] result = contract.createTransaction("CreatePolicy")
                .setEndorsingPeers(network.getChannel().getPeers(EnumSet.of(Peer.PeerRole.ENDORSING_PEER)))
                .submit(
                    request.getId(),
                    request.getName(),
                    request.getContent(),
                    request.getVersion()
                );
            
            Policy policy = parsePolicyResult(result);
            return BaseResponse.success(policy);
            
        } catch (ContractException e) {
            return BaseResponse.failure(
                ErrorCode.POLICY_CREATE_ERROR,
                e.getMessage()
            );
        }
    }

    /**
     * 更新策略接口
     */
    @PutMapping("/{policyId}")
    public BaseResponse<Policy> updatePolicy(
            @PathVariable String policyId,
            @Valid @RequestBody PolicyRequest request) 
            throws InterruptedException, TimeoutException {
        
        try {
            byte[] result = contract.createTransaction("UpdatePolicy")
                .setEndorsingPeers(network.getChannel().getPeers(EnumSet.of(Peer.PeerRole.ENDORSING_PEER)))
                .submit(
                    policyId,
                    request.getContent(),
                    request.getVersion()
                );
            
            Policy policy = parsePolicyResult(result);
            return BaseResponse.success(policy);
            
        } catch (ContractException e) {
            return BaseResponse.failure(
                ErrorCode.POLICY_UPDATE_ERROR,
                e.getMessage()
            );
        }
    }

    /**
     * 策略评估接口
     */
    @PostMapping("/{policyId}/evaluate")
    public BaseResponse<PolicyDecision> evaluatePolicy(
            @PathVariable String policyId,
            @Valid @RequestBody EvaluationRequest request) {
        log.info("Evaluating policy with ID: {}, request content: {}", policyId, request != null ? request.getRequestContent() : "null");
        try {
            if (policyId == null || policyId.trim().isEmpty()) {
                log.error("Policy ID is null or empty");
                return BaseResponse.failure(ErrorCode.POLICY_EVALUATE_ERROR, "Policy ID cannot be empty");
            }
            if (request == null || request.getRequestContent() == null || request.getRequestContent().trim().isEmpty()) {
                log.error("Request content is null or empty");
                return BaseResponse.failure(ErrorCode.POLICY_EVALUATE_ERROR, "Request content cannot be empty");
            }
            byte[] result = contract.createTransaction("EvaluatePolicy")
                .setEndorsingPeers(network.getChannel().getPeers(EnumSet.of(Peer.PeerRole.ENDORSING_PEER)))
                .submit(policyId, request.getRequestContent());
            
            PolicyDecision decision = parseDecisionResult(result);
            log.info("Chaincode evaluation result: {}", decision);
            return BaseResponse.success(decision);
            
        } catch (ContractException | InterruptedException | TimeoutException e) {
            log.error("Failed to evaluate policy: {}", e.getMessage(), e);
            return BaseResponse.failure(
                ErrorCode.POLICY_EVALUATE_ERROR,
                e.getMessage()
            );
        }
    }

    /**
     * 获取所有策略列表
     */
    @GetMapping
    public BaseResponse<List<Policy>> getAllPolicies() {
        try {
            byte[] result = contract.evaluateTransaction("QueryPolicies");
            List<Policy> policies = parsePolicyList(result);
            return BaseResponse.success(policies);
            
        } catch (ContractException e) {
            return BaseResponse.failure(
                ErrorCode.POLICY_QUERY_ERROR,
                e.getMessage()
            );
        }
    }

    /**
     * 获取策略详情
     */
    @GetMapping("/{policyId}")
    public BaseResponse<Policy> getPolicy(@PathVariable String policyId) {
        try {
            byte[] result = contract.evaluateTransaction("GetPolicy", policyId);
            log.info("getPolicyresult result: {}", new String(result));
            Policy policy = parsePolicyResult(result);
            return BaseResponse.success(policy);
            
        } catch (ContractException e) {
            return BaseResponse.failure(
                ErrorCode.POLICY_GET_ERROR,
                e.getMessage()
            );
        }
    }

    private Policy parsePolicyResult(byte[] result) throws ContractException {
        try {
            log.info("Chaincode result: {}", new String(result));
            if (result == null || result.length == 0) {
                throw new ContractException("Chaincode returned empty result");
            }
            // 解析 JSON 数组并取第一个元素
            Policy[] policies = objectMapper.readValue(new String(result), Policy[].class);
            if (policies == null || policies.length == 0) {
                throw new ContractException("Chaincode returned an empty policy array");
            }
            return policies[0];
        } catch (JsonProcessingException e) {
            log.error("Failed to parse policy result: {}", e.getMessage(), e);
            throw new ContractException("策略数据解析失败: " + e.getMessage());
        }
    }

    // 反序列化策略列表
    private List<Policy> parsePolicyList(byte[] result) throws ContractException {
        try {
            return objectMapper.readValue(
                new String(result),
                new TypeReference<List<Policy>>(){}
            );
        } catch (JsonProcessingException e) {
            throw new ContractException("策略列表解析失败: " + e.getMessage());
        }
    }

    // 反序列化决策结果
    private PolicyDecision parseDecisionResult(byte[] result) throws ContractException {
        try {
            String resultStr = new String(result);
            log.info("Chaincode result: {}", resultStr);
            return objectMapper.readValue(resultStr, PolicyDecision.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse decision result: {}", e.getMessage(), e);
            throw new ContractException("决策结果解析失败: " + e.getMessage());
        }
    }
    @PostMapping("/uploadPolicy")
    public ResponseEntity<String> uploadPolicy(@RequestBody String policyContent) {
        log.info("Received policy content: {}", policyContent.substring(0, Math.min(50, policyContent.length())));
        try {
            if (policyContent == null || policyContent.trim().isEmpty()) {
                log.error("Policy content is empty");
                return ResponseEntity.status(400).body("Policy content cannot be empty");
            }

            String policyId = UUID.randomUUID().toString();
            String policyName = "UploadedPolicy";
            String version = "1.0";

            byte[] result = contract.createTransaction("CreatePolicy")
                .setEndorsingPeers(network.getChannel().getPeers(EnumSet.of(Peer.PeerRole.ENDORSING_PEER)))
                .submit(policyId, policyName, policyContent, version);

            if (result == null || result.length == 0) {
                log.error("Chaincode returned empty result");
                return ResponseEntity.status(500).body("Chaincode returned empty result");
            }

            Policy policy = parsePolicyResult(result);
            log.info("Policy uploaded successfully with ID: {}", policy.getId());
            return ResponseEntity.ok("Policy uploaded successfully with ID: " + policy.getId());
        } catch (ContractException | InterruptedException | TimeoutException e) {
            log.error("Failed to upload policy: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to upload policy: " + e.getMessage());
        }
    }
}