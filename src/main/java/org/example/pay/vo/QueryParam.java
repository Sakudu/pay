package org.example.pay.vo;

import lombok.Data;

/**
 * @author gwd
 * @date 2023/11/3 15:07
 */
@Data
public class QueryParam {

    /** 交易号 */
    private String transNum;

    /** 退款单号 */
    private String refundNo;
}
