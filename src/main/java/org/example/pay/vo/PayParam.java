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

    public BigDecimal totalFee;

    private String transNum;

    private PayExtraData extraData;

    private Date timeExpire;

    private String payType;

    private String payChannel;

    private String wxOpenId;

    private String description;

}
