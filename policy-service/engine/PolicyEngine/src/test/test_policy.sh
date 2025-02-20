#!/bin/bash

# 配置参数
HOST="localhost:9090"
SCRIPT_DIR="$(dirname "$0")"
SAMPLES_DIR="$SCRIPT_DIR/samples"
POLICY_FILE="$SAMPLES_DIR/test_policy.xml"
REQUEST_FILE="$SAMPLES_DIR/test_request.xml"

# 颜色输出配置
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m'

# 工具函数：处理XML字符串
format_xml() {
    # 不处理换行符，保留原始内容
    echo "$1"
}



# 工具函数：构建JSON请求
build_json_request() {
    local content="$1"
    local field="$2"
    # 读取原始输入，不转义特殊字符
    echo "$content" | jq -Rs --arg field "$field" '{ ($field): . }'
}



# 检查grpcurl是否安装
if ! command -v grpcurl &> /dev/null; then
    echo -e "${RED}Error: grpcurl is not installed${NC}"
    echo "Install via: go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest"
    exit 1
fi


# 清理 BOM 头
sed -i '1s/^\xEF\xBB\xBF//' $POLICY_FILE
sed -i '1s/^\xEF\xBB\xBF//' $REQUEST_FILE

# 测试服务是否可用
echo "Testing service availability..."
if ! grpcurl -v -plaintext $HOST list org.example.policy.service.PolicyService > /dev/null 2>&1; then
    echo -e "${RED}Error: Policy service is not available at $HOST${NC}"
    echo "Trying to list all services..."
    grpcurl -v -plaintext $HOST list
    exit 1
fi
echo -e "${GREEN}Service is available${NC}"

# 从文件读取测试数据
echo -e "\nLoading test data from files..."
TEST_POLICY=$(cat "$POLICY_FILE")
TEST_REQUEST=$(cat "$REQUEST_FILE")


# 测试策略验证
echo -e "\nTesting policy validation..."
FORMATTED_POLICY=$(format_xml "$TEST_POLICY")
echo "Formatted Policy Content:"
echo "$FORMATTED_POLICY"
JSON_POLICY=$(jq -Rs --arg field "policy" '{ ($field): . }' "$POLICY_FILE")
echo "JSON Request:"
echo "$JSON_REQUEST"
echo "Sending request: $JSON_POLICY"
grpcurl -plaintext -d "$JSON_POLICY" \
    $HOST org.example.policy.service.PolicyService/validatePolicy

# 测试策略评估
echo -e "\nTesting policy evaluation..."
FORMATTED_REQUEST=$(format_xml "$TEST_REQUEST")
JSON_REQUEST=$(build_json_request "$FORMATTED_REQUEST" "requestContent")
echo "Sending request: $JSON_REQUEST"
grpcurl -plaintext -d "$JSON_REQUEST" \
    $HOST org.example.policy.service.PolicyService/evaluatePolicy

echo -e "\n${GREEN}Tests completed${NC}"
