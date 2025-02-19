package main

type PolicyDocument struct {
    ID      string `json:"id"`
    Content string `json:"content"`
    Version string `json:"version"`
}

type AccessRequest struct {
    Subject    string            `json:"subject"`
    Resource   string            `json:"resource"`
    Action     string            `json:"action"`
    Attributes map[string]string `json:"attributes"`
}

type AccessLog struct {
    Timestamp int64             `json:"timestamp"`
    Subject   string            `json:"subject"`
    Resource  string            `json:"resource"`
    Action    string            `json:"action"`
    Decision  string            `json:"decision"`
    Attributes map[string]string `json:"attributes,omitempty"`
}
