package main

import (
    "context"
    "fmt"
    "time"
    pb "github.com/digital-twin-uc/policy_contract/pb"
    "google.golang.org/grpc"
    "google.golang.org/grpc/credentials/insecure"
)

// PolicyEngineClient 定义
type PolicyEngineClient struct {
    client      pb.PolicyServiceClient
    retryConfig RetryConfig
}

// RetryConfig 重试配置
type RetryConfig struct {
    MaxAttempts int
    Interval    time.Duration
}

// PolicyResponse 响应结构
type PolicyResponse struct {
    Decision        string
    ResponseContent string
}

func NewPolicyEngineClient(baseURL string, retryConfig RetryConfig) *PolicyEngineClient {
    conn, err := grpc.Dial(baseURL, grpc.WithTransportCredentials(insecure.NewCredentials()))
    if err != nil {
        fmt.Printf("Failed to connect to Policy-Engine: %v\n", err)
        return nil
    }
    client := pb.NewPolicyServiceClient(conn)
    return &PolicyEngineClient{
        client:      client,
        retryConfig: retryConfig,
    }
}

func (c *PolicyEngineClient) ValidatePolicy(content string) (bool, error) {
    req := &pb.PolicyRequest{
        Policy: content,
    }

    var lastErr error
    for attempt := 0; attempt < c.retryConfig.MaxAttempts; attempt++ {
        resp, err := c.client.ValidatePolicy(context.Background(), req)
        if err == nil {
            return resp.GetValid(), nil
        }
        lastErr = err
        time.Sleep(c.retryConfig.Interval)
    }
    return false, fmt.Errorf("after %d attempts: %v", c.retryConfig.MaxAttempts, lastErr)
}

func (c *PolicyEngineClient) EvaluatePolicy(policyContent, requestContent string) (*PolicyResponse, error) {
    req := &pb.PolicyRequest{
        Policy:        policyContent,
        RequestContent: requestContent,
    }

    var lastErr error
    for attempt := 0; attempt < c.retryConfig.MaxAttempts; attempt++ {
        resp, err := c.client.EvaluatePolicy(context.Background(), req)
        if err == nil {
            return &PolicyResponse{
                Decision:        resp.GetDecision(),
                ResponseContent: resp.GetResponseContent(),
            }, nil
        }
        lastErr = err
        time.Sleep(c.retryConfig.Interval)
    }
    return nil, fmt.Errorf("after %d attempts: %v", c.retryConfig.MaxAttempts, lastErr)
}