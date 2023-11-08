package org.example.pay.service.impl;

import org.example.pay.service.VerifySignService;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author gwd
 * @date 2023/11/8 15:27
 */
@Service
public class VerifySignServiceImpl implements VerifySignService {

    @Override
    public Boolean verifySign(Map<String, String> map) {
        return Boolean.FALSE;
    }
}
