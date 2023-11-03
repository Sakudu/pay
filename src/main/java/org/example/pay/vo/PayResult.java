package org.example.pay.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author gwd
 * @date 2023/11/2 16:26
 */
@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class PayResult<T> {

    private T result;

    private String msg;

    private Integer code;

    private Exception e;

    private Boolean status = Boolean.FALSE;

    public void success(T result) {
        this.result = result;
        this.status = Boolean.TRUE;
    }

    public Boolean isSuccess() {
        return status;
    }

    public void fail(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public void unknown(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public void error(Integer code, String msg, Exception e) {
        this.code = code;
        this.msg = msg;
        this.e = e;
        log.error("支付异常：{}", e.getMessage(), e);
    }
}
