package org.example.pay.service.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSON;
import org.example.pay.common.Constants;
import org.example.pay.service.AfterPayService;
import org.example.pay.util.RedisUtil;
import org.example.pay.vo.PayParam;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author gwd
 * @date 2023/11/9 16:27
 */
@Service
public class AfterPayServiceImpl implements AfterPayService {

    @Resource
    private RedisUtil redisUtil;

    private static final String uuId = UUID.fastUUID().toString(true);

    @Override
    public void afterPayHandler(PayParam param) {
        redisUtil.addZSet(Constants.ASYNC_SEARCH, JSON.toJSONString(param), System.currentTimeMillis());
    }

    @Scheduled(cron = "0/60 * * * * ?")
    private void payPolling() {
        boolean lock = false;
        try {
            lock = redisUtil.getLock(Constants.ASYNC_SEARCH_LOCK, uuId, 10L, TimeUnit.MINUTES);
            if (lock) {
                payQueryAsync();
            }
        } finally {
            if (lock){
                redisUtil.releaseLock(Constants.ASYNC_SEARCH_LOCK, uuId);
            }
        }
    }

    private void payQueryAsync() {

    }
}
