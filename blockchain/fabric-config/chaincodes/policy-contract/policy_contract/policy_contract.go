package main

import (
    "encoding/json"
    "fmt"
    "log"
    "os"
    "time"
    "github.com/hyperledger/fabric-contract-api-go/contractapi"
)

// PolicyContract 策略管理智能合约
type PolicyContract struct {
    contractapi.Contract
    policyClient *PolicyEngineClient
}

func NewPolicyContract() *PolicyContract {
    return &PolicyContract{
        policyClient: NewPolicyEngineClient("http://localhost:9090", RetryConfig{
            MaxAttempts: 3,
            Interval:    time.Second,
        }),
    }
}

// Policy 策略文件结构
type Policy struct {
    ID              string    `json:"id"`               // 策略ID
    Name            string    `json:"name"`             // 策略名称
    Content         string    `json:"content"`          // XACML策略内容
    Version         string    `json:"version"`          // 策略版本
    Status          string    `json:"status"`           // 策略状态(active/inactive)
    CreatedAt       time.Time `json:"createdAt"`        // 创建时间
    UpdatedAt       time.Time `json:"updatedAt"`        // 更新时间
    CreatedBy       string    `json:"createdBy"`        // 创建者
}

// PolicyDecision 策略决策结果
type PolicyDecision struct {
    RequestID       string    `json:"requestId"`        // 请求ID
    PolicyID        string    `json:"policyId"`         // 策略ID
    Decision        string    `json:"decision"`         // 决策结果
    Timestamp       time.Time `json:"timestamp"`        // 决策时间
    RequestContent  string    `json:"requestContent"`   // 请求内容
    ResponseContent string    `json:"responseContent"`  // 响应内容
}

// 添加获取交易时间戳的辅助函数
func getTxTimestamp(ctx contractapi.TransactionContextInterface) (time.Time, error) {
    txTimestamp, err := ctx.GetStub().GetTxTimestamp()
    if err != nil {
        return time.Time{}, fmt.Errorf("failed to get transaction timestamp: %v", err)
    }
    return time.Unix(txTimestamp.Seconds, int64(txTimestamp.Nanos)), nil
}


// CreatePolicy 创建新策略，仅使用交易时间戳
func (pc *PolicyContract) CreatePolicy(ctx contractapi.TransactionContextInterface, id string, name string, content string, version string) error {
    // 检查策略是否已存在
    exists, err := pc.PolicyExists(ctx, id)
    if err != nil {
        return fmt.Errorf("failed to check policy existence: %v", err)
    }
    if exists {
        return fmt.Errorf("policy already exists: %s", id)
    }

    // 验证XACML格式
    if (!validateXACMLFormat(content)) {
        return fmt.Errorf("invalid XACML format")
    }

    // 获取交易时间戳
    txTime, err := getTxTimestamp(ctx)
    if err != nil {
        return fmt.Errorf("failed to get transaction timestamp: %v", err)
    }

    // 获取创建者ID
    creatorID, err := ctx.GetClientIdentity().GetID()
    if err != nil {
        return fmt.Errorf("failed to get creator ID: %v", err)
    }

    // 创建策略对象
    policy := Policy{
        ID:        id,
        Name:      name,
        Content:   content,
        Version:   version,
        Status:    "active",
        CreatedAt: txTime,
        UpdatedAt: txTime,
        CreatedBy: creatorID,
    }

    // 将策略序列化为JSON
    policyJSON, err := json.Marshal(policy)
    if err != nil {
        return fmt.Errorf("failed to marshal policy: %v", err)
    }

    // 存储策略
    err = ctx.GetStub().PutState(id, policyJSON)
    if err != nil {
        return fmt.Errorf("failed to store policy: %v", err)
    }

    // 发出事件
    err = ctx.GetStub().SetEvent("PolicyCreated", policyJSON)
    if err != nil {
        return fmt.Errorf("failed to emit PolicyCreated event: %v", err)
    }

    return nil
}

// UpdatePolicy 更新策略，仅使用交易时间戳
func (pc *PolicyContract) UpdatePolicy(ctx contractapi.TransactionContextInterface, id string, content string, version string) error {
    // 检查策略是否存在
    policy, err := pc.GetPolicy(ctx, id)
    if err != nil {
        return fmt.Errorf("failed to get policy: %v", err)
    }

    // 验证XACML格式
    if (!validateXACMLFormat(content)) {
        return fmt.Errorf("invalid XACML format")
    }

    // 获取交易时间戳
    txTime, err := getTxTimestamp(ctx)
    if err != nil {
        return fmt.Errorf("failed to get transaction timestamp: %v", err)
    }

    // 更新策略内容
    policy.Content = content
    policy.Version = version
    policy.UpdatedAt = txTime

    // 序列化并存储
    policyJSON, err := json.Marshal(policy)
    if err != nil {
        return fmt.Errorf("failed to marshal policy: %v", err)
    }

    err = ctx.GetStub().PutState(id, policyJSON)
    if err != nil {
        return fmt.Errorf("failed to update policy: %v", err)
    }

    // 发出事件
    err = ctx.GetStub().SetEvent("PolicyUpdated", policyJSON)
    if err != nil {
        return fmt.Errorf("failed to emit PolicyUpdated event: %v", err)
    }

    return nil
}

// GetPolicy 获取策略
func (pc *PolicyContract) GetPolicy(ctx contractapi.TransactionContextInterface, id string) (*Policy, error) {
    policyJSON, err := ctx.GetStub().GetState(id)
    if err != nil {
        return nil, fmt.Errorf("failed to read policy: %v", err)
    }
    if policyJSON == nil {
        return nil, fmt.Errorf("policy does not exist: %s", id)
    }

    var policy Policy
    err = json.Unmarshal(policyJSON, &policy)
    if err != nil {
        return nil, fmt.Errorf("failed to unmarshal policy: %v", err)
    }

    return &policy, nil
}

// EvaluatePolicy 评估策略，修改函数以使用交易时间戳
func (pc *PolicyContract) EvaluatePolicy(ctx contractapi.TransactionContextInterface, policyId string, requestContent string) (*PolicyDecision, error) {
    // 获取策略
    policy, err := pc.GetPolicy(ctx, policyId)
    if (err != nil) {
        return nil, fmt.Errorf("failed to get policy: %v", err)
    }

    // 调用策略引擎评估
    response, err := evaluatePolicyWithEngine(policy.Content, requestContent)
    if err != nil {
        return nil, fmt.Errorf("policy evaluation failed: %v", err)
    }

    // 获取交易时间戳
    txTime, err := getTxTimestamp(ctx)
    if err != nil {
        return nil, fmt.Errorf("failed to get transaction timestamp: %v", err)
    }

    // 创建决策结果
    decision := &PolicyDecision{
        RequestID:       fmt.Sprintf("req_%d", txTime.Unix()),
        PolicyID:        policyId,
        Decision:        response.Decision,
        Timestamp:       txTime,
        RequestContent:  requestContent,
        ResponseContent: response.ResponseContent,
    }

    // 存储决策结果
    decisionJSON, err := json.Marshal(decision)
    if err != nil {
        return nil, fmt.Errorf("failed to marshal decision: %v", err)
    }

    err = ctx.GetStub().PutState(decision.RequestID, decisionJSON)
    if err != nil {
        return fmt.Errorf("failed to store decision: %v", err)
    }

    // 发出事件
    err = ctx.GetStub().SetEvent("PolicyEvaluated", decisionJSON)
    if err != nil {
        return fmt.Errorf("failed to emit PolicyEvaluated event: %v", err)
    }

    return decision, nil
}

// QueryPolicies 查询策略列表
func (pc *PolicyContract) QueryPolicies(ctx contractapi.TransactionContextInterface) ([]*Policy, error) {
    // 创建复合键查询
    resultsIterator, err := ctx.GetStub().GetStateByRange("", "")
    if err != nil {
        return nil, fmt.Errorf("failed to get policy iterator: %v", err)
    }
    defer resultsIterator.Close()

    var policies []*Policy
    for resultsIterator.HasNext() {
        queryResponse, err := resultsIterator.Next()
        if err != nil {
            return nil, fmt.Errorf("failed to get next policy: %v", err)
        }

        var policy Policy
        err = json.Unmarshal(queryResponse.Value, &policy)
        if err != nil {
            continue // Skip invalid entries
        }
        policies = append(policies, &policy)
    }

    return policies, nil
}

// PolicyExists 检查策略是否存在
func (pc *PolicyContract) PolicyExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
    policyJSON, err := ctx.GetStub().GetState(id)
    if err != nil {
        return false, fmt.Errorf("failed to read policy: %v", err)
    }
    return policyJSON != nil, nil
}

// validateXACMLFormat 验证XACML格式
func validateXACMLFormat(content string) bool {
    valid, err := policyEngine.ValidatePolicy(content)
    if err != nil {
        log.Printf("Error validating policy: %v", err)
        return false
    }
    return valid
}

// evaluatePolicyWithEngine 调用策略引擎进行评估
func evaluatePolicyWithEngine(policyContent string, requestContent string) (*struct {
    Decision        string
    ResponseContent string
}, error) {
    response, err := policyEngine.EvaluatePolicy(policyContent, requestContent)
    if err != nil {
        return nil, fmt.Errorf("policy engine error: %v", err)
    }

    if !response.Success {
        return nil, fmt.Errorf("policy evaluation failed: %s", response.Response)
    }

    return &struct {
        Decision        string
        ResponseContent string
    }{
        Decision:        response.Decision,
        ResponseContent: response.Response,
    }, nil
}

// AccessResource 访问资源时进行策略评估
func (c *PolicyContract) AccessResource(ctx contractapi.TransactionContextInterface, requestJSON string) (bool, error) {
    // 获取调用者身份
    clientID, err := ctx.GetClientIdentity().GetID()
    if err != nil {
        return false, fmt.Errorf("failed to get client identity: %v", err)
    }

    // 构造策略评估请求
    var request struct {
        Resource string            `json:"resource"`
        Action   string           `json:"action"`
        Attrs    map[string]string `json:"attributes"`
    }
    
    if err := json.Unmarshal([]byte(requestJSON), &request); err != nil {
        return false, fmt.Errorf("failed to unmarshal request: %v", err)
    }

    // 调用策略引擎进行评估
    policyResponse, err := c.policyClient.EvaluatePolicy(clientID, request.Resource, request.Action, request.Attrs)
    if err != nil {
        return false, fmt.Errorf("policy evaluation failed: %v", err)
    }

    // 记录访问日志
    accessLog := map[string]interface{}{
        "timestamp": time.Now().Unix(),
        "subject":   clientID,
        "resource":  request.Resource,
        "action":    request.Action,
        "decision":  policyResponse.Decision,
    }
    
    accessLogJSON, _ := json.Marshal(accessLog)
    err = ctx.GetStub().PutState("ACCESS_LOG_"+fmt.Sprint(time.Now().UnixNano()), accessLogJSON)
    if err != nil {
        return false, fmt.Errorf("failed to save access log: %v", err)
    }

    return policyResponse.Success && policyResponse.Decision == "Permit", nil
}

// UpdatePolicy 更新策略
func (c *PolicyContract) UpdatePolicy(ctx contractapi.TransactionContextInterface, policyID string, policyContent string) error {
    // 验证调用者权限
    if err := c.validateAdminRole(ctx); err != nil {
        return err
    }

    // 验证策略内容
    valid, err := c.policyClient.ValidatePolicy(policyContent)
    if err != nil {
        return fmt.Errorf("failed to validate policy: %v", err)
    }
    if !valid {
        return fmt.Errorf("invalid policy content")
    }

    // 存储策略
    err = ctx.GetStub().PutState("POLICY_"+policyID, []byte(policyContent))
    if err != nil {
        return fmt.Errorf("failed to store policy: %v", err)
    }

    return nil
}

// validateAdminRole 验证管理员权限
func (c *PolicyContract) validateAdminRole(ctx contractapi.TransactionContextInterface) error {
    // 获取调用者属性
    attrs, err := ctx.GetClientIdentity().GetAttributeValue("role")
    if err != nil {
        return fmt.Errorf("failed to get role attribute: %v", err)
    }

    if attrs != "admin" {
        return fmt.Errorf("only admin can perform this operation")
    }

    return nil
}

func main() {
    chaincode, err := contractapi.NewChaincode(NewPolicyContract())
    if err != nil {
        fmt.Printf("Error creating policy chaincode: %v", err)
        return
    }

    if err := chaincode.Start(); err != nil {
        fmt.Printf("Error starting policy chaincode: %v", err)
    }
}