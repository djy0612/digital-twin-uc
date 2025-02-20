package org.denglg.hyperledgerfabric.app.javademo.pojo.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Policy {
    // 策略基础信息
    private String id;             // 策略唯一标识
    private String name;           // 策略名称
    private String content;        // XACML策略内容
    private String version;        // 策略版本
    
    // 生命周期管理
    private String status;         // 策略状态 (ACTIVE/INACTIVE)
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime createdAt;  // 创建时间
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime updatedAt;  // 更新时间
    
    private String createdBy;      // 创建者身份标识
}
