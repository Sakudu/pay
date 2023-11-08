package org.example.pay.service;

import org.example.pay.vo.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author gwd
 * @date 2023/11/2 16:25
 */
public interface PayService {

    PayResult<String> pay(PayParam payParam);

    PayResult<RefundResult> refund(RefundParam refundParam);

    PayResult<QueryResult> payQuery(QueryParam queryParam);

    PayResult<QueryResult> refundQuery(QueryParam param);

    PayResult<ResultNotify> notify(HttpServletRequest request, HttpServletResponse response);
}
