package org.denglg.hyperledgerfabric.app.javademo.pojo.request;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class EvaluationRequest {
    @NotBlank(message = "请求内容不能为空")
    private String requestContent;
}
