package main

import (
    "fmt"
    "log"
    "os"
    "gopkg.in/yaml.v3"
)

type Config struct {
    PolicyEngine struct {
        URL     string `yaml:"url"`
        Timeout string `yaml:"timeout"`
        Retry   struct {
            MaxAttempts int    `yaml:"max_attempts"`
            Interval    string `yaml:"interval"`
        } `yaml:"retry"`
    } `yaml:"policy_engine"`
}

var config *Config

func loadConfig(configPath string) error {
    // 尝试直接读取配置文件
    data, err := os.ReadFile(configPath)
    if err != nil {
        // 如果读取失败，尝试在不同的相对路径查找
        paths := []string{
            configPath,
            "../" + configPath,
            "../../" + configPath,
            "./config/engine-config.yaml",
            "../config/engine-config.yaml",
        }

        for _, path := range paths {
            if data, err = os.ReadFile(path); err == nil {
                // 找到配置文件
                log.Printf("Found config file at: %s", path)
                break
            }
        }

        if err != nil {
            return fmt.Errorf("could not find config file in any location: %v", err)
        }
    }

    config = &Config{}
    return yaml.Unmarshal(data, config)
}

func getConfigValue(key string, defaultValue string) string {
    // 优先从环境变量获取
    if value := os.Getenv(key); value != "" {
        return value
    }

    // 从配置文件获取
    if config != nil {
        switch key {
        case "POLICY_ENGINE_URL":
            if config.PolicyEngine.URL != "" {
                return config.PolicyEngine.URL
            }
        }
    }

    return defaultValue
}
