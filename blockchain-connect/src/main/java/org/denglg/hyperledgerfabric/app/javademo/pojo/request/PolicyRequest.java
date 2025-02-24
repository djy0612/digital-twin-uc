package org.denglg.hyperledgerfabric.app.javademo.pojo.request;

import lombok.Data;

@Data
public class PolicyRequest {
    private String id;        // 策略 ID
    private String name;      // 策略名称
    private String content;   // XACML 内容
    private String version;   // 版本
    
    // Lombok 的 @Data 会生成以下 getter 和 setter
    // public String getId() { return id; }
    // public String getName() { return name; }
    // public String getContent() { return content; }
    // public String getVersion() { return version; }
    // public void setId(String id) { this.id = id; }
    // public void setName(String name) { this.name = name; }
    // public void setContent(String content) { this.content = content; }
    // public void setVersion(String version) { this.version = version; }
}