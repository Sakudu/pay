package org.example.pay.service;

import java.util.Map;

/**
 * @author gwd
 * @date 2023/11/8 15:24
 */
public interface VerifySignService {

    /** 验签 true代表验签通过 false代表验签失败 */
    Boolean verifySign(Map<String, String> map);
}
