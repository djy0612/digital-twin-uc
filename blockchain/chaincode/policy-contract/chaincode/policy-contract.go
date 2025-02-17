package main

import (
	"encoding/json"
	"fmt"
	"time"
	"strings"
	"unicode/utf8"

	"github.com/hyperledger/fabric-contract-api-go/contractapi"
	"github.com/valyala/fastjson"
)

// PolicyContract 智能合约结构体
type PolicyContract struct {
	contractapi.Contract
}

// PolicyState 策略状态结构
type PolicyState struct {
	PolicyID    string `json:"policyId"`
	XACML       string `json:"xacml"`      // Base64编码的XACML策略
	Version     int    `json:"version"`
	Enabled     bool   `json:"enabled"`
	Creator     string `json:"creator"`
	CreatedAt   int64  `json:"createdAt"`
	LastUpdated int64  `json:"lastUpdated"`
}

// CreatePolicyRequest 创建策略请求结构
type CreatePolicyRequest struct {
	PolicyID  string `json:"policyId"`
	XACML     string `json:"xacml"`
	Overwrite bool   `json:"overwrite"`
}

// 常量定义
const (
	AdminMSP = "Org1MSP"
	MaxPolicySize = 1024 * 1024 // 1MB
)

// CreatePolicy 创建/更新策略方法
func (s *PolicyContract) CreatePolicy(ctx contractapi.TransactionContextInterface, reqJSON string) (*PolicyState, error) {
	// 解析请求
	var req CreatePolicyRequest
	if err := json.Unmarshal([]byte(reqJSON), &req); err != nil {
		return nil, fmt.Errorf("invalid request format: %v", err)
	}

	// 权限验证
	clientMSP, err := ctx.GetClientIdentity().GetMSPID()
	if err != nil || clientMSP != AdminMSP {
		return nil, fmt.Errorf("only admin can create policies")
	}

	// XACML格式校验
	if err := validateXACML(req.XACML); err != nil {
		return nil, fmt.Errorf("invalid XACML policy: %v", err)
	}

	// 检查策略是否存在
	existing, err := s.getPolicyState(ctx, req.PolicyID)
	if err == nil && !req.Overwrite {
		return nil, fmt.Errorf("policy %s already exists", req.PolicyID)
	}

	// 构建新策略状态
	newState := PolicyState{
		PolicyID:    req.PolicyID,
		XACML:       req.XACML,
		Version:     1,
		Enabled:     true,
		Creator:     ctx.GetClientIdentity().GetID(),
		CreatedAt:   time.Now().UnixNano(),
		LastUpdated: time.Now().UnixNano(),
	}

	// 如果存在旧版本则递增版本号
	if existing != nil {
		newState.Version = existing.Version + 1
		newState.CreatedAt = existing.CreatedAt
	}

	// 存储到账本
	stateBytes, err := json.Marshal(newState)
	if err != nil {
		return nil, fmt.Errorf("marshal policy failed: %v", err)
	}

	if err := ctx.GetStub().PutState(req.PolicyID, stateBytes); err != nil {
		return nil, fmt.Errorf("put state failed: %v", err)
	}

	return &newState, nil
}

// GetPolicy 查询策略方法
func (s *PolicyContract) GetPolicy(ctx contractapi.TransactionContextInterface, policyID string) (*PolicyState, error) {
	stateBytes, err := ctx.GetStub().GetState(policyID)
	if err != nil {
		return nil, fmt.Errorf("get state failed: %v", err)
	}
	if stateBytes == nil {
		return nil, fmt.Errorf("policy %s not found", policyID)
	}

	var state PolicyState
	if err := json.Unmarshal(stateBytes, &state); err != nil {
		return nil, fmt.Errorf("unmarshal policy failed: %v", err)
	}

	return &state, nil
}

// validateXACML XACML格式校验
func validateXACML(xacml string) error {
	// 基础校验
	if utf8.RuneCountInString(xacml) > MaxPolicySize {
		return fmt.Errorf("policy size exceeds 1MB")
	}

	// 使用fastjson进行快速语法校验
	var p fastjson.Parser
	if _, err := p.Parse(xacml); err != nil {
		return fmt.Errorf("invalid XML format: %v", err)
	}

	// 基本XACML元素检查
	if !strings.Contains(xacml, "<Policy ") {
		return fmt.Errorf("missing Policy element")
	}
	if !strings.Contains(xacml, "<Rule ") {
		return fmt.Errorf("missing Rule element")
	}

	return nil
}

// getPolicyState 内部获取策略状态方法
func (s *PolicyContract) getPolicyState(ctx contractapi.TransactionContextInterface, policyID string) (*PolicyState, error) {
	stateBytes, err := ctx.GetStub().GetState(policyID)
	if err != nil || stateBytes == nil {
		return nil, err
	}

	var state PolicyState
	if err := json.Unmarshal(stateBytes, &state); err != nil {
		return nil, err
	}

	return &state, nil
}