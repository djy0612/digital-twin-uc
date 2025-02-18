#!/bin/bash

# 设置环境变量
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051

# Org2的TLS证书
ORG2_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt

# Orderer的TLS证书
ORDERER_CA=/etc/hyperledger/fabric/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/msp/tlscacerts/tlsca.example.com-cert.pem

# 颜色设置
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

# 测试函数
test_create_policy() {
    echo -e "${BLUE}测试创建策略...${NC}"
    TIMESTAMP=$(date +%s)
    peer chaincode invoke -o orderer0.example.com:7050 --ordererTLSHostnameOverride orderer0.example.com --tls \
        --cafile ${ORDERER_CA} \
        -C businesschannel -n policy_contract \
        --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles ${CORE_PEER_TLS_ROOTCERT_FILE} \
        --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles ${ORG2_TLS_ROOTCERT_FILE} \
        -c "{\"function\":\"CreatePolicy\",\"Args\":[\"policy1\",\"TestPolicy\",\"<Policy>Test XACML Content</Policy>\",\"1.0\",\"${TIMESTAMP}\"]}"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}创建策略成功${NC}"
    else
        echo -e "${RED}创建策略失败${NC}"
        exit 1
    fi
}

test_get_policy() {
    echo -e "${BLUE}测试查询策略...${NC}"
    peer chaincode query -C businesschannel -n policy_contract -c '{"function":"GetPolicy","Args":["policy1"]}'
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}查询策略成功${NC}"
    else
        echo -e "${RED}查询策略失败${NC}"
        exit 1
    fi
}

test_update_policy() {
    echo -e "${BLUE}测试更新策略...${NC}"
    TIMESTAMP=$(date +%s)
    peer chaincode invoke -o orderer0.example.com:7050 --ordererTLSHostnameOverride orderer0.example.com --tls \
        --cafile ${ORDERER_CA} \
        -C businesschannel -n policy_contract \
        --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles ${CORE_PEER_TLS_ROOTCERT_FILE} \
        --peerAddresses peer0.org2.example.com:9051 --tlsRootCertFiles ${ORG2_TLS_ROOTCERT_FILE} \
        -c "{\"function\":\"UpdatePolicy\",\"Args\":[\"policy1\",\"<Policy>Updated XACML Content</Policy>\",\"1.1\",\"${TIMESTAMP}\"]}"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}更新策略成功${NC}"
    else
        echo -e "${RED}更新策略失败${NC}"
        exit 1
    fi
}

test_evaluate_policy() {
    echo -e "${BLUE}测试评估策略...${NC}"
    peer chaincode invoke -o orderer0.example.com:7050 --ordererTLSHostnameOverride orderer.example.com --tls \
        --cafile ${ORDERER_CA} \
        -C businesschannel -n policy_contract \
        --peerAddresses peer0.org1.example.com:7051 --tlsRootCertFiles $CORE_PEER_TLS_ROOTCERT_FILE \
        -c '{"function":"EvaluatePolicy","Args":["policy1","<Request>Test Request</Request>"]}'
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}评估策略成功${NC}"
    else
        echo -e "${RED}评估策略失败${NC}"
        exit 1
    fi
}

test_query_policies() {
    echo -e "${BLUE}测试查询所有策略...${NC}"
    peer chaincode query -C businesschannel -n policy_contract -c '{"function":"QueryPolicies","Args":[]}'
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}查询所有策略成功${NC}"
    else
        echo -e "${RED}查询所有策略失败${NC}"
        exit 1
    fi
}



# 执行测试
echo -e "${BLUE}开始测试策略合约...${NC}"
test_create_policy
sleep 3
test_get_policy
sleep 3
test_update_policy
sleep 3
test_query_policies
sleep 3
echo -e "${GREEN}所有测试完成${NC}"
