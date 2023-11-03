package org.example.pay.service;

import org.example.pay.vo.PayParam;
import org.example.pay.vo.PayResult;
import org.example.pay.vo.RefundParam;
import org.example.pay.vo.RefundResult;

/**
 * @author gaowende
 * @date 2023/11/2 16:25
 */
public interface PayService {

    PayResult<?> pay(PayParam payParam);

    PayResult<RefundResult> refund(RefundParam refundParam);
}
