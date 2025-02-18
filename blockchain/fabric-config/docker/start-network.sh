#!/bin/bash

# 定义 docker-compose 文件路径
DIR="$(dirname "$0")"  # 获取脚本所在目录
COMPOSE_FILES=(
    "$DIR/docker-compose-orderer.yaml"
    "$DIR/docker-compose-org1.yaml"
    "$DIR/docker-compose-org2.yaml"
)

# 启动所有 Docker Compose 服务
echo "Starting all Docker Compose services..."
for FILE in "${COMPOSE_FILES[@]}"; do
    echo "Starting $FILE..."
    docker-compose -f "$FILE" up -d
done
echo "All services started successfully!"

# 等待一段时间，确保服务启动完成
echo "Waiting for services to initialize..."
sleep 10

# 定义通道名称和其他环境变量
APP_CHANNEL="businesschannel"
TIMEOUT=30
ORDERER_ADDRESS="orderer0.example.com:7050"
ORDERER_CA="/etc/hyperledger/fabric/crypto-config/ordererOrganizations/example.com/orderers/orderer0.example.com/msp/tlscacerts/tlsca.example.com-cert.pem"

# 创建通道
echo "Creating channel $APP_CHANNEL..."
docker exec fabric-cli bash -c "
export APP_CHANNEL=$APP_CHANNEL
export TIMEOUT=$TIMEOUT
export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp

peer channel create -o $ORDERER_ADDRESS -c \$APP_CHANNEL -f \"/tmp/channel-artifacts/\$APP_CHANNEL.tx\" --timeout \"\${TIMEOUT}s\" --tls --cafile $ORDERER_CA
"
# 检查通道文件是否生成
if ! docker exec fabric-cli bash -c "[ -f /opt/gopath/src/github.com/hyperledger/fabric/peer/$APP_CHANNEL.block ]"; then
    echo "Channel block file not found in container. Exiting..."
    exit 1
fi

# 将生成的通道文件移动到本地目录
echo "Moving channel block file to local directory..."
docker exec fabric-cli bash -c "
mv $APP_CHANNEL.block /tmp/channel-artifacts/
"
# 加入通道
echo "Joining peers to the channel $APP_CHANNEL..."

# Org1 的 peer0 加入通道
docker exec fabric-cli bash -c "
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051

peer channel join -b /tmp/channel-artifacts/$APP_CHANNEL.block
"

# Org1 的 peer1 加入通道
docker exec fabric-cli bash -c "
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer1.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer1.org1.example.com:8051

peer channel join -b /tmp/channel-artifacts/$APP_CHANNEL.block
"

# Org2 的 peer0 加入通道
docker exec fabric-cli bash -c "
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID=Org2MSP
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=peer0.org2.example.com:9051

peer channel join -b /tmp/channel-artifacts/$APP_CHANNEL.block
"

# Org2 的 peer1 加入通道
docker exec fabric-cli bash -c "
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID=Org2MSP
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer1.org2.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=peer1.org2.example.com:10051

peer channel join -b /tmp/channel-artifacts/$APP_CHANNEL.block
"

# 更新锚节点
echo "Updating anchor peers..."

# Org1 更新锚节点
docker exec fabric-cli bash -c "
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID=Org1MSP
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=peer0.org1.example.com:7051

peer channel update -o $ORDERER_ADDRESS -c $APP_CHANNEL -f /tmp/channel-artifacts/Org1MSPanchors.tx --tls --cafile $ORDERER_CA
"

# Org2 更新锚节点
docker exec fabric-cli bash -c "
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID=Org2MSP
export CORE_PEER_TLS_ROOTCERT_FILE=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=/etc/hyperledger/fabric/crypto-config/peerOrganizations/org2.example.com/users/Admin@org2.example.com/msp
export CORE_PEER_ADDRESS=peer0.org2.example.com:9051

peer channel update -o $ORDERER_ADDRESS -c $APP_CHANNEL -f /tmp/channel-artifacts/Org2MSPanchors.tx --tls --cafile $ORDERER_CA
"

echo "All operations completed successfully!"