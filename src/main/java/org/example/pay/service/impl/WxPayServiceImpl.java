package org.example.pay.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiServiceExtension;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayWithRequestPaymentResponse;
import com.wechat.pay.java.service.payments.model.Transaction;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import com.wechat.pay.java.service.payments.nativepay.model.QueryOrderByOutTradeNoRequest;
import com.wechat.pay.java.service.refund.RefundService;
import com.wechat.pay.java.service.refund.model.AmountReq;
import com.wechat.pay.java.service.refund.model.CreateRequest;
import com.wechat.pay.java.service.refund.model.Refund;
import com.wechat.pay.java.service.refund.model.Status;
import lombok.extern.slf4j.Slf4j;
import org.example.pay.common.PayChannel;
import org.example.pay.common.PayCode;
import org.example.pay.common.WxParam;
import org.example.pay.service.PayService;
import org.example.pay.vo.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author gwd
 * @date 2023/11/2 16:26
 */
@Slf4j
//@Service("wxPay")
public class WxPayServiceImpl implements PayService {

    private final NativePayService nativeService;

    private final JsapiServiceExtension jsApiService;

    private final RefundService refundService;

    private final WxParam wxParam;

    //@Autowired
    public WxPayServiceImpl(RSAAutoCertificateConfig config, WxParam wxParam) {
        this.nativeService = new NativePayService.Builder().config(config).build();
        this.jsApiService = new JsapiServiceExtension.Builder().config(config).build();
        this.refundService = new RefundService.Builder().config(config).build();
        this.wxParam = wxParam;
    }


    @Override
    public PayResult<String> pay(PayParam payParam) {
        log.info("发起支付参数：{}", JSONUtil.toJsonStr(payParam));
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
                PayResult<String> result = new PayResult<>();
                result.fail(PayCode.BUSINESS_FAIL, "发起支付失败，请联系管理员");
                return result;
        }
    }

    @Override
    public PayResult<RefundResult> refund(RefundParam refundParam) {
        log.info("发起退款参数{}", JSONUtil.toJsonStr(refundParam));
        PayResult<RefundResult> result = new PayResult<>();
        try {
            CreateRequest request = new CreateRequest();
            request.setTransactionId(refundParam.getOutTradeNo());
            request.setOutTradeNo(refundParam.getTransNum());
            request.setOutRefundNo(refundParam.getRefundNo());
            AmountReq amount = new AmountReq();
            amount.setTotal(refundParam.getTotalFee().multiply(new BigDecimal("100")).longValue());
            amount.setRefund(refundParam.getRefundFee().multiply(new BigDecimal("100")).longValue());
            request.setAmount(amount);
            log.info("调用微信退款参数{}", JSONUtil.toJsonStr(request));
            Refund refund = refundService.create(request);
            log.info("调用微信退款返回{}", JSONUtil.toJsonStr(refund));
            if (refund.getStatus().equals(Status.SUCCESS)) {
                RefundResult refundResult = new RefundResult();
                refundResult.setOutRefundNo(refund.getRefundId());
                refundResult.setRefundNo(refund.getOutRefundNo());
                refundResult.setOutTradeNo(refund.getTransactionId());
                refundResult.setTransNum(refund.getOutTradeNo());
                refundResult.setRefundTime(DateUtil.parse(refund.getSuccessTime(), DatePattern.UTC_WITH_XXX_OFFSET_PATTERN));
                result.success(refundResult);
            } else if (refund.getStatus().equals(Status.PROCESSING)) {
                result.unknown(PayCode.UNKNOWN_STATUS, "退款中，请稍后");
            } else {
                result.fail(PayCode.BUSINESS_FAIL, "退款失败");
            }
        } catch (Exception e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getMessage(), e);
        }
        log.info("返回结果{}", JSONUtil.toJsonStr(result));
        return result;
    }

    @Override
    public PayResult<QueryResult> payQuery(QueryParam queryParam) {
        PayResult<QueryResult> result = new PayResult<>();
        QueryResult queryResult = new QueryResult();
        try {
            QueryOrderByOutTradeNoRequest query = new QueryOrderByOutTradeNoRequest();
            query.setOutTradeNo(queryParam.getTransNum());
            query.setMchid(wxParam.getMchId());
            Transaction response = nativeService.queryOrderByOutTradeNo(query);
            queryResult.setStatus(response.getTradeState().name());
            if (Transaction.TradeStateEnum.SUCCESS.equals(response.getTradeState())) {
                queryResult.setSuccessTime(DateUtil.parse(response.getSuccessTime(), DatePattern.UTC_WITH_XXX_OFFSET_PATTERN));
                queryResult.setTransNum(response.getOutTradeNo());
                queryResult.setOutTradeNum(response.getTransactionId());
                queryResult.setTradeType(response.getTradeType().name());
                queryResult.setExtraData(JSONUtil.toBean(response.getAttach(), PayExtraData.class));
                queryResult.setTotalFee(new BigDecimal(response.getAmount().getTotal()).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
                result.success(queryResult);
            } else if (Transaction.TradeStateEnum.USERPAYING.equals(response.getTradeState())) {
                result.unknown(PayCode.UNKNOWN_STATUS, "用户支付中");
            } else {
                result.fail(PayCode.BUSINESS_FAIL, response.getTradeStateDesc());
            }
        } catch (Exception e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getMessage(), e);
        }
        return result;
    }

    @Override
    public PayResult<QueryResult> refundQuery(QueryParam param) {
        PayResult<QueryResult> result = new PayResult<>();
        return null;
    }

    @Override
    public PayResult<ResultNotify> notify(HttpServletRequest request, HttpServletResponse response) {
        return null;
    }

    private PayResult<String> h5Pay(PayParam payParam) {
        return null;
    }

    private PayResult<String> appPay(PayParam payParam) {
        return null;
    }

    private PayResult<String> miniPay(PayParam payParam) {
        return null;
    }

    private PayResult<String> scenePay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        return null;
    }

    private PayResult<String> jsApiPay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        try {
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
            log.info("调用微信jsapi支付参数{}", JSONUtil.toJsonStr(request));
            PrepayWithRequestPaymentResponse response = jsApiService.prepayWithRequestPayment(request);
            log.info("调用微信jsapi支付返回{}", JSONUtil.toJsonStr(response));
            result.success(JSONUtil.toJsonStr(response));
        } catch (Exception e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getMessage(), e);
        }
        return result;
    }

    private PayResult<String> nativePay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        try {
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
            log.info("调用微信native支付参数{}", JSONUtil.toJsonStr(request));
            PrepayResponse response = nativeService.prepay(request);
            log.info("调用微信native支付返回{}", JSONUtil.toJsonStr(response));
            String codeUrl = response.getCodeUrl();
            result.success(codeUrl);
        } catch (Exception e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getMessage(), e);
        }
        return result;
    }

}
