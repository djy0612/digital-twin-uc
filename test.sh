#!/bin/bash

# 测试脚本，用于验证 Fabric 网络、Fabric-Client 和 Policy-Engine 的交互

# 配置
FABRIC_CLIENT_URL="http://localhost:8080"
POLICY_ENGINE_HOST="localhost:9090"
CHAINCODE_NAME="policy_contract"
CHANNEL_NAME="businesschannel"
PEER_ADDRESS="localhost:7051"
SCRIPT_DIR="$(dirname "$0")"
SAMPLES_DIR="$SCRIPT_DIR/samples"
POLICY_FILE="$SAMPLES_DIR/test_policy.xml"
REQUEST_FILE="$SAMPLES_DIR/test_request.xml"

# 工具函数：处理XML字符串
format_xml() {
    echo "$1"
}

# 工具函数：构建JSON请求
build_json_request() {
    local content="$1"
    local field="$2"
    echo "$content" | jq -Rs --arg field "$field" '{ ($field): . }'
}

# 清理 BOM 头
sed -i '1s/^\xEF\xBB\xBF//' "$POLICY_FILE"
sed -i '1s/^\xEF\xBB\xBF//' "$REQUEST_FILE"

echo -e "\nLoading test data from files..."
TEST_POLICY=$(cat "$POLICY_FILE")
TEST_REQUEST=$(cat "$REQUEST_FILE")

FORMATTED_POLICY=$(format_xml "$TEST_POLICY")


# 使用正确的字段名 "policy_content"（根据实际 proto 文件调整）
JSON_POLICY=$(jq -Rs --arg field "policy_content" '{ ($field): . }' "$POLICY_FILE")

FORMATTED_REQUEST=$(format_xml "$TEST_REQUEST")
JSON_REQUEST=$(build_json_request "$FORMATTED_REQUEST" "requestContent")

# 测试 1：通过 Policy-Engine 上传策略
echo "Testing Policy Upload..."
UPLOAD_RESPONSE=$(grpcurl -plaintext -d "$JSON_POLICY" $POLICY_ENGINE_HOST org.example.policy.service.PolicyService/uploadPolicy)
if [ $? -ne 0 ]; then
  echo "Failed to upload policy via Policy-Engine"
  exit 1
fi

# 提取上传结果
SUCCESS=$(echo "$UPLOAD_RESPONSE" | grep -o '"success": true')
if [ -z "$SUCCESS" ]; then
  echo "Policy upload failed: $UPLOAD_RESPONSE"
  exit 1
else
  echo "Policy uploaded successfully: $UPLOAD_RESPONSE"
fi

# 获取策略 ID
POLICY_ID=$(echo "$UPLOAD_RESPONSE" | grep -o '"message": "Policy uploaded successfully with ID: [^"]*' | sed 's/.*ID: //')
if [ -z "$POLICY_ID" ]; then
  POLICY_ID="test_policy"
  echo "Could not extract policyId, using default: $POLICY_ID"
else
  echo "Extracted policyId: $POLICY_ID"
fi

# 格式化并替换 REQUEST 中的 policyId
FORMATTED_REQUEST=$(format_xml "$TEST_REQUEST")
FORMATTED_REQUEST=$(echo "$FORMATTED_REQUEST" | sed "s/\$POLICY_ID/$POLICY_ID/")
JSON_REQUEST=$(build_json_request "$FORMATTED_REQUEST" "requestContent")

echo "Formatted Request Content:"
echo "$JSON_REQUEST"

# 测试 2：通过 Fabric-Client 评估策略
echo "Testing Policy Evaluation..."
EVAL_RESPONSE=$(curl -s -X POST "$FABRIC_CLIENT_URL/policies/$POLICY_ID/evaluate" \
  -H "Content-Type: application/json" \
  -d "$JSON_REQUEST")
if [ $? -ne 0 ]; then
  echo "Failed to evaluate policy via Fabric-Client"
  exit 1
fi

# 检查评估结果
DECISION=$(echo "$EVAL_RESPONSE" | jq -r '.data.decision')
if [ "$DECISION" == "Permit" ]; then
  echo "Policy evaluation succeeded: $EVAL_RESPONSE"
else
  echo "Policy evaluation failed or denied: $EVAL_RESPONSE"
  exit 1
fi

echo "All tests passed successfully!"