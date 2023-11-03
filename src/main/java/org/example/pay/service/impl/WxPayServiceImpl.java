package org.example.pay.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;
import org.example.pay.common.PayChannel;
import org.example.pay.common.PayCode;
import org.example.pay.common.WxParam;
import org.example.pay.service.PayService;
import org.example.pay.vo.PayParam;
import org.example.pay.vo.PayResult;
import org.example.pay.vo.RefundParam;
import org.example.pay.vo.RefundResult;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author gwd
 * @date 2023/11/2 16:26
 */
@Service("wxPay")
public class WxPayServiceImpl implements PayService {

    @Resource
    private RSAAutoCertificateConfig config;

    @Resource
    private WxParam wxParam;

    @Override
    public PayResult<?> pay(PayParam payParam) {
        switch (payParam.getPayChannel()) {
            case PayChannel.PC:
                return nativePay(payParam);
            case PayChannel.JSAPI:
                return jsApiPay(payParam);
            case PayChannel.SCENE:
                return scenePay(payParam);
            case PayChannel.MINI:
                return miniPay(payParam);
            case PayChannel.APP:
                return appPay(payParam);
            case PayChannel.H5:
                return h5Pay(payParam);
            default:
                PayResult<?> result = new PayResult<>();
                result.fail(PayCode.BUSINESS_FAIL, "发起支付失败，请联系管理员");
                return result;
        }
    }

    @Override
    public PayResult<RefundResult> refund(RefundParam refundParam) {
        PayResult<RefundResult> result = new PayResult<>();
        try {
            RefundService service = new RefundService
                    .Builder()
                    .config(config)
                    .build();
            CreateRequest request = new CreateRequest();
            request.setTransactionId(refundParam.getOutTradeNo());
            request.setOutTradeNo(refundParam.getTransNum());
            request.setOutRefundNo(refundParam.getRefundNo());
            AmountReq amount = new AmountReq();
            amount.setTotal(refundParam.getTotalFee().multiply(new BigDecimal("100")).longValue());
            amount.setRefund(refundParam.getRefundFee().multiply(new BigDecimal("100")).longValue());
            request.setAmount(amount);
            Refund refund = service.create(request);
            if (refund.getStatus().equals(Status.SUCCESS)){
                RefundResult refundResult = new RefundResult();
                refundResult.setOutRefundNo(refund.getRefundId());
                refundResult.setRefundNo(refund.getOutRefundNo());
                refundResult.setOutTradeNo(refund.getTransactionId());
                refundResult.setTransNum(refund.getOutTradeNo());
                refundResult.setRefundTime(DateUtil.parse(refund.getSuccessTime(), DatePattern.UTC_WITH_XXX_OFFSET_PATTERN));
                result.success(refundResult);
            } else if (refund.getStatus().equals(Status.PROCESSING)){
                result.unknown(PayCode.UNKNOWN_STATUS, "退款中，请稍后");
            } else{
                result.fail(PayCode.BUSINESS_FAIL, "退款失败");
            }
        } catch (Exception e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getMessage(), e);
        }
        return result;
    }

    private PayResult<?> h5Pay(PayParam payParam) {
        return null;
    }

    private PayResult<?> appPay(PayParam payParam) {
        return null;
    }

    private PayResult<?> miniPay(PayParam payParam) {
        return null;
    }

    private PayResult<?> scenePay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        return null;
    }

    private PayResult<?> jsApiPay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        try {
            JsapiServiceExtension service = new JsapiServiceExtension
                    .Builder()
                    .config(config)
                    .build();
            com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest request = new com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest();
            request.setAppid(wxParam.getAppId());
            request.setMchid(wxParam.getMchId());
            request.setDescription(payParam.getDescription());
            request.setOutTradeNo(payParam.getTransNum());
            request.setTimeExpire(DateUtil.format(payParam.getTimeExpire(), DatePattern.UTC_WITH_XXX_OFFSET_PATTERN));
            request.setAttach(JSONUtil.toJsonStr(payParam.getExtraData()));
            request.setNotifyUrl(wxParam.getNotifyUrl());
            com.wechat.pay.java.service.payments.jsapi.model.Amount amount = new com.wechat.pay.java.service.payments.jsapi.model.Amount();
            amount.setTotal(payParam.getTotalFee().multiply(new BigDecimal("100")).intValue());
            request.setAmount(amount);
            Payer payer = new Payer();
            payer.setOpenid(payParam.getWxOpenId());
            request.setPayer(payer);
            PrepayWithRequestPaymentResponse response = service.prepayWithRequestPayment(request);
            result.success(JSONUtil.toJsonStr(response));
        } catch (Exception e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getMessage(), e);
        }
        return result;
    }

    private PayResult<?> nativePay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        try {
            NativePayService service = new NativePayService
                    .Builder()
                    .config(config)
                    .build();
            PrepayRequest request = new PrepayRequest();
            request.setAppid(wxParam.getAppId());
            request.setMchid(wxParam.getMchId());
            request.setDescription(payParam.getDescription());
            request.setOutTradeNo(payParam.getTransNum());
            request.setTimeExpire(DateUtil.format(payParam.getTimeExpire(), DatePattern.UTC_WITH_XXX_OFFSET_PATTERN));
            request.setAttach(JSONUtil.toJsonStr(payParam.getExtraData()));
            request.setNotifyUrl(wxParam.getNotifyUrl());
            Amount amount = new Amount();
            amount.setTotal(payParam.getTotalFee().multiply(new BigDecimal("100")).intValue());
            request.setAmount(amount);
            PrepayResponse response = service.prepay(request);
            String codeUrl = response.getCodeUrl();
            result.success(codeUrl);
        } catch (Exception e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getMessage(), e);
        }
        return result;
    }

}
