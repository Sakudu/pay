package org.example.pay.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.example.pay.common.Constants;
import org.example.pay.service.AfterPayService;
import org.example.pay.service.AsyncSearchPayService;
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
@Slf4j
@Service
public class AfterPayServiceImpl implements AfterPayService {

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private AsyncSearchPayService searchPayService;

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
            if (lock) {
                redisUtil.releaseLock(Constants.ASYNC_SEARCH_LOCK, uuId);
            }
        }
    }

    private void payQueryAsync() {
        boolean flag;
        for (int i = 0; i < 99; i++) {
            flag = redisUtil.zSetIsEmpty(Constants.ASYNC_SEARCH);
            if (flag) {
                break;
            }
            String value = redisUtil.getZSetValue(Constants.ASYNC_SEARCH);
            if (StrUtil.isNotEmpty(value)) {
                PayParam param = JSON.parseObject(value, PayParam.class);
                if (ObjectUtil.isEmpty(param)) {
                    searchPayService.executeAsync(param);
                }
            }
        }

    }
}
