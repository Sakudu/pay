package org.example.pay.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author gwd
 * @date 2023/11/8 9:53
 */
@Data
public class ResultNotify {

    /** 交易号 */
    private String transNum;

    /** 交易时间 */
    private Date payTime;

    /** 支付方式 */
    private Integer payType;

    /** 支付金额 */
    private BigDecimal payAmount;

    /** 第三方交易号 */
    private String outTradeNo;

    /** 附加信息 */
    private PayExtraData extraData;

}
