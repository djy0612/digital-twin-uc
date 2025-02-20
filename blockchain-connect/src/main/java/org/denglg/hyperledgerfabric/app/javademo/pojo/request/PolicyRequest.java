package org.denglg.hyperledgerfabric.app.javademo.pojo.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class PolicyRequest {
    @NotBlank(message = "策略ID不能为空")
    private String policyId;
    
    @NotBlank(message = "策略内容不能为空")
    private String policyContent;
    
    private String policyName;
    private String version;
    
}
