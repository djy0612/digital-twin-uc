package main

import (
    "bytes"
    "encoding/json"
    "fmt"
    "net/http"
    "time"
)

type PolicyEngineClient struct {
    baseURL     string
    client      *http.Client
    retryConfig RetryConfig
}

type RetryConfig struct {
    MaxAttempts int
    Interval    time.Duration
}

type PolicyResponse struct {
    Success  bool   `json:"success"`
    Decision string `json:"decision"`
    Response string `json:"response"`
}

func NewPolicyEngineClient(baseURL string, retryConfig RetryConfig) *PolicyEngineClient {
    return &PolicyEngineClient{
        baseURL: baseURL,
        client: &http.Client{
            Timeout: time.Second * 30,
        },
        retryConfig: retryConfig,
    }
}

func (c *PolicyEngineClient) ValidatePolicy(content string) (bool, error) {
    resp, err := c.doWithRetry("POST", "/api/policy/validate", content)
    if err != nil {
        return false, err
    }
    defer resp.Body.Close()

    var result struct {
        Valid   bool   `json:"valid"`
        Message string `json:"message"`
    }
    if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
        return false, err
    }
    return result.Valid, nil
}

func (c *PolicyEngineClient) EvaluatePolicy(policyContent, requestContent string) (*PolicyResponse, error) {
    request := map[string]string{
        "policyContent":  policyContent,
        "requestContent": requestContent,
    }
    
    resp, err := c.doWithRetry("POST", "/api/policy/evaluate", request)
    if err != nil {
        return nil, err
    }
    defer resp.Body.Close()

    var result PolicyResponse
    if err := json.NewDecoder(resp.Body).Decode(&result); err != nil {
        return nil, err
    }
    return &result, nil
}

// 添加重试逻辑
func (c *PolicyEngineClient) doWithRetry(method, path string, body interface{}) (*http.Response, error) {
    var lastErr error
    for attempt := 0; attempt < c.retryConfig.MaxAttempts; attempt++ {
        resp, err := c.doRequest(method, path, body)
        if err == nil {
            return resp, nil
        }
        lastErr = err
        time.Sleep(c.retryConfig.Interval)
    }
    return nil, fmt.Errorf("after %d attempts: %v", c.retryConfig.MaxAttempts, lastErr)
}

// 实际发送请求
func (c *PolicyEngineClient) doRequest(method, path string, body interface{}) (*http.Response, error) {
    jsonBody, err := json.Marshal(body)
    if err != nil {
        return nil, err
    }

    req, err := http.NewRequest(method, c.baseURL+path, bytes.NewBuffer(jsonBody))
    if err != nil {
        return nil, err
    }
    req.Header.Set("Content-Type", "application/json")

    return c.client.Do(req)
}
