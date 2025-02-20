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

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
                    request.getPolicyId(),
                    request.getPolicyName(),
                    request.getPolicyContent(),
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
                    request.getPolicyContent(),
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
        
        try {
            byte[] result = contract.createTransaction("EvaluatePolicy")
                .setEndorsingPeers(network.getChannel().getPeers(EnumSet.of(Peer.PeerRole.ENDORSING_PEER)))
                .submit(policyId, request.getRequestContent());
            
            PolicyDecision decision = parseDecisionResult(result);
            return BaseResponse.success(decision);
            
        } catch (ContractException | InterruptedException | TimeoutException e) {
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
            Policy policy = parsePolicyResult(result);
            return BaseResponse.success(policy);
            
        } catch (ContractException e) {
            return BaseResponse.failure(
                ErrorCode.POLICY_GET_ERROR,
                e.getMessage()
            );
        }
    }

    // 反序列化策略数据
    private Policy parsePolicyResult(byte[] result) throws ContractException {
        try {
            return objectMapper.readValue(new String(result), Policy.class);
        } catch (JsonProcessingException e) {
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
            return objectMapper.readValue(new String(result), PolicyDecision.class);
        } catch (JsonProcessingException e) {
            throw new ContractException("决策结果解析失败: " + e.getMessage());
        }
    }
}