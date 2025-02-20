package org.denglg.hyperledgerfabric.app.javademo.pojo.constant;

public enum ErrorCode {

    SUCCESS(0, "操作成功"),
    REQ_ERROR(101, "请求参数校验失败"),
    NULL_ERROR(102, "空指针异常"),

    NO_DOC(201, "不存在该did标识对应的文档"),
    EXIST_DOC(202, "已存在该did标识对应的文档"),
    
    REMOTE_ERROR(203,"请求链码出现错误"),
    SYSTEM_ERROR(999,"未知异常"),
    // 策略管理错误码 (1000-1999)
    POLICY_NOT_FOUND(1001, "策略不存在"),
    POLICY_VERSION_CONFLICT(1002, "策略版本冲突"),
    POLICY_XACML_INVALID(1003, "XACML格式校验失败"),
    POLICY_CREATE_ERROR(1004, "策略创建失败"),
    POLICY_UPDATE_ERROR(1005, "策略更新失败"),
    POLICY_EVALUATE_ERROR(1006, "策略评估失败"),
    POLICY_QUERY_ERROR(1007, "策略查询失败"),
    POLICY_GET_ERROR(1008, "获取策略失败"),
    // 区块链操作错误码 (2000-2999)
    FABRIC_TX_TIMEOUT(2001, "区块链交易超时"),
    FABRIC_ENDORSEMENT_FAIL(2002, "节点背书失败");
    /**
     * 错误码
     */
    private int code;

    /**
     * 错误描述
     */
    private String msg;

    ErrorCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return this.code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return this.msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}
