package org.denglg.hyperledgerfabric.app.javademo.pojo.constant;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PolicyDecision {
    // 决策标识
    private String requestId;         // 唯一请求标识
    
    // 关联策略信息
    private String policyId;          // 被评估的策略ID
    private String policyVersion;     // 策略版本
    
    // 决策结果
    private String decision;          // 决策结果 (Permit/Deny)
    private String responseContent;   // XACML响应原始内容
    
    // 请求上下文
    private String requestContent;    // XACML请求原始内容
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;  // 决策时间戳
}
