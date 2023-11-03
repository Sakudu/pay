package org.example.pay.common;

/**
 * @author gaowende
 * @date 2023/11/3 14:07
 */
public interface PayCode {

    /** 业务导致失败 */
    Integer BUSINESS_FAIL = 500;

    /** 状态未知，需调用查询 */
    Integer UNKNOWN_STATUS = 530;

    /** 异常导致失败 */
    Integer EXCEPTION_ERROR = 550;
}
