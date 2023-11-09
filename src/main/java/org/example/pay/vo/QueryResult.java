package org.example.pay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author gwd
 * @date 2023/11/3 15:08
 */
@Data
public class QueryResult {

    /** 交易时间 */
    private Date successTime;

    /** 流水号 */
    private String transNum;

    /** 第三方订单号 */
    private String outTradeNum;

    /** 交易类型 目前适用于微信 */
    private String tradeType;

    /** 状态 */
    private String status;

    /** 附加信息 */
    private PayExtraData extraData;

    /** 总金额 */
    private BigDecimal totalFee;

    /** 退款单号 */
    private String refundNo;

    /** 第三方退款单号 */
    private String outRefundNo;

    /** 退款金额 */
    private BigDecimal refundFee;
}
