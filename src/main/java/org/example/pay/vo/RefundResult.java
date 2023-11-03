package org.example.pay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author gaowende
 * @date 2023/11/3 13:42
 */
@Data
public class RefundResult {

    /** 第三方退款单号 */
    private String outRefundNo;

    /** 退款单号 */
    private String refundNo;

    /** 第三方交易号 */
    private String outTradeNo;

    /** 交易号 */
    private String transNum;

    /** 退款时间 */
    private Date refundTime;


}
