package org.example.pay.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author gwd
 * @date 2023/11/3 13:21
 */
@Data
public class RefundParam {

    private String outTradeNo;

    private String transNum;

    private String refundNo;

    private BigDecimal refundFee;

    private BigDecimal totalFee;

    private String currency = "CNY";
}
