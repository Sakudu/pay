package org.example.pay.common;

/**
 * @author gwd
 * @date 2023/11/3 10:04
 */
public interface PayChannel {

    /** pc */
    String PC = "00";

    /** h5 */
    String H5 = "01";

    /** 公众号 */
    String JSAPI = "02";

    /** 现场扫码 */
    String SCENE = "03";

    /** 小程序 */
    String MINI = "04";

    /** app */
    String APP = "05";
}
