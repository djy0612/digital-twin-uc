package main

import (
    "encoding/json"
    "fmt"
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
        policyClient: NewPolicyEngineClient("localhost:9090", RetryConfig{
            MaxAttempts: 3,
            Interval:    time.Second,
        }),
    }
}

// Policy 策略文件结构
type Policy struct {
    ID        string    `json:"id"`
    Name      string    `json:"name"metadata:",optional"`
    Content   string    `json:"content"metadata:",optional"`
    Version   string    `json:"version"metadata:",optional"`
    Status    string    `json:"status"metadata:",optional"`
    CreatedAt time.Time `json:"createdAt"metadata:",optional"`
    UpdatedAt time.Time `json:"updatedAt"metadata:",optional"`
    CreatedBy string    `json:"createdBy"metadata:",optional"`
}

// PolicyDecision 策略决策结果
type PolicyDecision struct {
    RequestID       string    `json:"requestId"`
    PolicyID        string    `json:"policyId"`
    Decision        string    `json:"decision"`
    Timestamp       time.Time `json:"timestamp"`
    RequestContent  string    `json:"requestContent"`
    ResponseContent string    `json:"responseContent"`
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
func (pc *PolicyContract) CreatePolicy(ctx contractapi.TransactionContextInterface, id string, name string, content string, version string) (string, error) {
    // 检查策略是否已存在
    exists, err := pc.PolicyExists(ctx, id)
    if err != nil {
        return "", fmt.Errorf("failed to check policy existence: %v", err)
    }
    if exists {
        return "", fmt.Errorf("policy already exists: %s", id)
    }

    // 验证XACML格式
    valid, err := pc.policyClient.ValidatePolicy(content)
    if err != nil {
        return "", fmt.Errorf("failed to validate policy: %v", err)
    }
    if !valid {
        return "", fmt.Errorf("invalid XACML format")
    }

    // 获取交易时间戳
    txTime, err := getTxTimestamp(ctx)
    if err != nil {
        return "", fmt.Errorf("failed to get transaction timestamp: %v", err)
    }

    // 获取创建者ID
    creatorID, err := ctx.GetClientIdentity().GetID()
    if err != nil {
        return "", fmt.Errorf("failed to get creator ID: %v", err)
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
        return "", fmt.Errorf("failed to marshal policy: %v", err)
    }

    // 存储策略
    err = ctx.GetStub().PutState(id, policyJSON)
    if err != nil {
        return "", fmt.Errorf("failed to store policy: %v", err)
    }

    // 发出事件
    err = ctx.GetStub().SetEvent("PolicyCreated", policyJSON)
    if err != nil {
        return "", fmt.Errorf("failed to emit PolicyCreated event: %v", err)
    }
    // 包装为数组并返回字符串
    policyArray := []Policy{policy}
    policyArrayJSON, err := json.Marshal(policyArray)
    if err != nil {
        return "", fmt.Errorf("failed to marshal policy array: %v", err)
    }

    return string(policyArrayJSON), nil
}

// UpdatePolicy 更新策略，仅使用交易时间戳
func (pc *PolicyContract) UpdatePolicy(ctx contractapi.TransactionContextInterface, id string, content string, version string) error {
    // 检查策略是否存在
    policyJSON, err := pc.GetPolicy(ctx, id)
    if err != nil {
        return fmt.Errorf("failed to get policy: %v", err)
    }

    // 反序列化 JSON 数组
    var policies []Policy
    err = json.Unmarshal([]byte(policyJSON), &policies)
    if err != nil {
        return fmt.Errorf("failed to unmarshal policy array: %v", err)
    }
    if len(policies) == 0 {
        return fmt.Errorf("policy not found: %s", id)
    }
    policy := policies[0] // 取第一个策略

    // 验证 XACML 格式
    valid, err := pc.policyClient.ValidatePolicy(content)
    if err != nil {
        return fmt.Errorf("failed to validate policy: %v", err)
    }
    if !valid {
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
    policyJSONBytes, err := json.Marshal(policy)
    if err != nil {
        return fmt.Errorf("failed to marshal policy: %v", err)
    }

    err = ctx.GetStub().PutState(id, policyJSONBytes)
    if err != nil {
        return fmt.Errorf("failed to update policy: %v", err)
    }

    // 发出事件
    err = ctx.GetStub().SetEvent("PolicyUpdated", policyJSONBytes)
    if err != nil {
        return fmt.Errorf("failed to emit PolicyUpdated event: %v", err)
    }

    return nil
}

// GetPolicy 获取策略
func (pc *PolicyContract) GetPolicy(ctx contractapi.TransactionContextInterface, policyId string) (string, error) {
    if policyId == "" {
        return "", fmt.Errorf("policyId cannot be empty")
    }

    policyJSON, err := ctx.GetStub().GetState(policyId)
    if err != nil {
        return "", fmt.Errorf("failed to get policy: %v", err)
    }
    if policyJSON == nil {
        return "", fmt.Errorf("policy not found: %s", policyId)
    }

    var policy Policy
    err = json.Unmarshal(policyJSON, &policy)
    if err != nil {
        return "", fmt.Errorf("failed to unmarshal policy: %v", err)
    }

    // 返回数组格式
    policyArray := []Policy{policy}
    policyArrayJSON, err := json.Marshal(policyArray)
    if err != nil {
        return "", fmt.Errorf("failed to marshal policy array: %v", err)
    }

    return string(policyArrayJSON), nil
}

// EvaluatePolicy 评估策略，修改函数以使用交易时间戳
func (pc *PolicyContract) EvaluatePolicy(ctx contractapi.TransactionContextInterface, policyId string, requestContent string) (string, error) {
    if policyId == "" || requestContent == "" {
        return "", fmt.Errorf("policyId or requestContent cannot be empty")
    }

    // 获取策略
    policyJSON, err := ctx.GetStub().GetState(policyId)
    if err != nil {
        return "", fmt.Errorf("failed to get policy: %v", err)
    }
    if policyJSON == nil {
        return "", fmt.Errorf("policy not found: %s", policyId)
    }

    var policy Policy
    err = json.Unmarshal(policyJSON, &policy)
    if err != nil {
        return "", fmt.Errorf("failed to unmarshal policy: %v", err)
    }

    // 调用 Policy-Engine 评估
    response, err := pc.policyClient.EvaluatePolicy(policy.Content, requestContent)
    if err != nil {
        return "", fmt.Errorf("policy evaluation failed: %v", err)
    }

    // 获取交易时间戳
    txTime, err := getTxTimestamp(ctx)
    if err != nil {
        return "", fmt.Errorf("failed to get transaction timestamp: %v", err)
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

    // 序列化为 JSON
    decisionJSON, err := json.Marshal(decision)
    if err != nil {
        return "", fmt.Errorf("failed to marshal decision: %v", err)
    }

    // 存储决策结果
    err = ctx.GetStub().PutState(decision.RequestID, decisionJSON)
    if err != nil {
        return "", fmt.Errorf("failed to store decision: %v", err)
    }

    // 发出事件
    err = ctx.GetStub().SetEvent("PolicyEvaluated", decisionJSON)
    if err != nil {
        return "", fmt.Errorf("failed to emit PolicyEvaluated event: %v", err)
    }

    return string(decisionJSON), nil
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