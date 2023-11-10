package org.example.pay.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author gwd
 * @date 2023/11/2 16:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayParam {

    /** 实体id 如：订单id */
    private Long id;

    /** 总金额 */
    private BigDecimal totalFee;

    /** 交易号 */
    private String transNum;

    /** 附加信息 */
    private PayExtraData extraData;

    /** 超时时间 */
    private Date timeExpire;

    /** 支付方式 */
    private String payType;

    /** 支付渠道 */
    private String payChannel;

    /** 微信openId */
    private String wxOpenId;

    /** 描述 */
    private String description;

    /** title */
    private String title;

    /** 二维码号 */
    private String authCode;

    /** 已查询次数 */
    private Integer queryCount = 0;

}
