/*
SPDX-License-Identifier: Apache-2.0
*/

package main

import (
	"log"
	"github.com/hyperledger/fabric-contract-api-go/contractapi"
	"github.com/hyperledger/fabric-samples/asset-transfer-basic/chaincode-go/chaincode"
)

func main() {
	// 实例化链码
	smartContract := new(chaincode.SmartContract)

	// 创建链码服务
	chaincode, err := contractapi.NewChaincode(smartContract)
	if err != nil {
		log.Panicf("Error creating chaincode: %v", err)
	}

	// 启动链码
	if err := chaincode.Start(); err != nil {
		log.Panicf("Error starting chaincode: %v", err)
	}
}
