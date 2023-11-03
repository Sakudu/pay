package org.example.pay.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author gaowende
 * @date 2023/11/2 16:26
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayResult<T> {

    private T result;

    private String msg;

    private Integer code;

    private Exception e;

    private Boolean status = Boolean.FALSE;

    public void success(T result){
        this.result = result;
        this.status = Boolean.TRUE;
    }

    public Boolean isSuccess(){
        return status;
    }

    public void fail(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public void unknown(Integer code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public void error(Integer code, String msg, Exception e){
        this.code = code;
        this.msg = msg;
        this.e = e;
    }
}
