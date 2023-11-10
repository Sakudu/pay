package org.example.pay.service;

import org.example.pay.vo.PayParam;

/**
 * @author gwd
 * @date 2023/11/10 8:53
 */
public interface AsyncSearchPayService {

    /** 实现该接口通过线程池asyncServiceExecutor异步查询订单信息 */
    void executeAsync(PayParam param);
}
