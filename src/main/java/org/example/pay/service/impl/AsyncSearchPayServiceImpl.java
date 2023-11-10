package org.example.pay.service.impl;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.pay.service.AsyncSearchPayService;
import org.example.pay.vo.PayParam;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * @author gwd
 * @date 2023/11/10 8:53
 */
@Slf4j
@Service
public class AsyncSearchPayServiceImpl implements AsyncSearchPayService {

    @Override
    @Async("asyncServiceExecutor")
    public void executeAsync(PayParam param) {
        log.info("当前线程：{} 查询待支付 获取参数：{}", Thread.currentThread().getName(), JSON.toJSONString(param));
        //TODO 具体业务
    }
}
