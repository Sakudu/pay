package org.example.pay.service;

import org.example.pay.vo.PayParam;

/**
 * @author gwd
 * @date 2023/11/9 16:25
 */
public interface AfterPayService {

    /** 支付后发起查询动作 */
    void afterPayHandler(PayParam param);
}
