package org.example.pay.service;

import org.example.pay.vo.*;

/**
 * @author gwd
 * @date 2023/11/2 16:25
 */
public interface PayService {

    PayResult<String> pay(PayParam payParam);

    PayResult<RefundResult> refund(RefundParam refundParam);

    PayResult<QueryResult> payQuery(QueryParam queryParam);
}
