package org.example.pay.common;

/**
 * @author gwd
 * @date 2023/11/3 9:53
 */

public enum PayEnum {

    WX_PAY(PayType.WX, "wxPay"),

    ALI_PAY(PayType.ALI,"aliPay");

    private final Integer payType;

    private final String beanName;

    PayEnum(Integer payType, String beanName) {
        this.payType = payType;
        this.beanName = beanName;
    }

    public static PayEnum getEnum(Integer payType) {
        PayEnum[] payEnums = PayEnum.values();
        if (payType != null) {
            for (PayEnum payEnum : payEnums) {
                if (payEnum.payType.equals(payType)) {
                    return payEnum;
                }
            }
        }
        return null;
    }

    public String getBeanName() {
        return beanName;
    }
}
