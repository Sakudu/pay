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

    private Date successTime;

    private String transNum;

    private String outTradeNum;

    private String tradeType;

    private String status;

    private PayExtraData extraData;

    private BigDecimal totalFee;
}
