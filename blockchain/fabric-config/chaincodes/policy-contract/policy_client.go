package client

import (
    "context"
    "fmt"
    "google.golang.org/grpc"
    pb "policy/service"
)

type PolicyClient struct {
    client  pb.PolicyServiceClient
    conn    *grpc.ClientConn
}

func NewPolicyClient(address string) (*PolicyClient, error) {
    conn, err := grpc.Dial(address, grpc.WithInsecure())
    if err != nil {
        return nil, fmt.Errorf("failed to connect: %v", err)
    }

    client := pb.NewPolicyServiceClient(conn)
    return &PolicyClient{
        client: client,
        conn:   conn,
    }, nil
}

func (c *PolicyClient) Close() {
    if c.conn != nil {
        c.conn.Close()
    }
}

func (c *PolicyClient) EvaluatePolicy(policyContent, requestContent string) (*pb.PolicyResponse, error) {
    ctx, cancel := context.WithTimeout(context.Background(), defaultTimeout)
    defer cancel()

    request := &pb.PolicyRequest{
        PolicyContent:  policyContent,
        RequestContent: requestContent,
    }

    return c.client.EvaluatePolicy(ctx, request)
}

func (c *PolicyClient) ValidatePolicy(policyContent string) (*pb.ValidateResponse, error) {
    ctx, cancel := context.WithTimeout(context.Background(), defaultTimeout)
    defer cancel()

    request := &pb.ValidateRequest{
        PolicyContent: policyContent,
    }

    return c.client.ValidatePolicy(ctx, request)
}