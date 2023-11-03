package org.example.pay.factory;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.pay.common.PayEnum;
import org.example.pay.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gwd
 * @date 2023/11/3 9:50
 */
@Slf4j
@Component
public class PayFactory {

    private final ConcurrentHashMap<String, PayService> strategyMap = new ConcurrentHashMap<>();

    @Autowired
    public PayFactory(Map<String, PayService> map) {
        if (CollectionUtil.isNotEmpty(map)) {
            for (Map.Entry<String, PayService> entry : map.entrySet()) {
                if (StrUtil.isEmpty(entry.getKey()) || entry.getValue() == null) {
                    return;
                }
                strategyMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public PayService getPayStrategy(Integer code) {
        if (code == null || CollectionUtil.isEmpty(strategyMap)) {
            log.error(String.format("支付获取PaysService异常 参数:%d map:%s", code, strategyMap));
        }
        PayEnum payEnum = PayEnum.getEnum(code);
        if (payEnum == null) {
            log.error(String.format("支付获取beanName异常 参数:%s", code));
            throw new RuntimeException("");
        }
        return strategyMap.get(payEnum.getBeanName());
    }
}
