package org.example.pay.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import lombok.extern.slf4j.Slf4j;
import org.example.pay.common.AliParam;
import org.example.pay.common.PayChannel;
import org.example.pay.common.PayCode;
import org.example.pay.service.PayService;
import org.example.pay.vo.QueryParam;
import org.example.pay.vo.QueryResult;
import org.example.pay.vo.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author gwd
 * @date 2023/11/6 9:39
 */
@Slf4j
@Service("aliPay")
public class AliPayServiceImpl implements PayService {

    @Resource
    private AliParam aliParam;

    @Resource
    private DefaultAlipayClient client;

    private static final String ALI_H5_PAY_CODE = "QUICK_WAP_WAY";

    private static final String ALI_JSAPI_PAY_CODE = "JSAPI_PAY";

    private static final String ALI_PC_PAY_CODE = "FAST_INSTANT_TRADE_PAY";

    private static final String CALL_BACK_SUCCESS = "success";

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private static final String TRADE_FINISHED = "TRADE_FINISHED";

    @Override
    public PayResult<String> pay(PayParam payParam) {
        log.info("支付宝支付入参:" + JSONUtil.toJsonStr(payParam));
        switch (payParam.getPayChannel()) {
            case PayChannel.PC:
                return pcPay(payParam);
            case PayChannel.H5:
                return h5Pay(payParam);
            case PayChannel.SCENE:
                return scenePay(payParam);
            case PayChannel.APP:
                return miniPay(payParam);
            case PayChannel.MINI:
                return jsApiPay();
            default:
                PayResult<String> result = new PayResult<>();
                result.fail(PayCode.BUSINESS_FAIL, "发起支付失败，请联系管理员");
                return result;
        }
    }

    private PayResult<String> jsApiPay() {
        PayResult<String> result = new PayResult<>();
        result.fail(PayCode.BUSINESS_FAIL, "该方式暂不支持");
        return result;
    }

    private PayResult<String> miniPay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        AlipayTradeCreateRequest request = new AlipayTradeCreateRequest();
        AlipayTradeCreateModel model = new AlipayTradeCreateModel();
        request.setNotifyUrl(aliParam.getNotifyUrl());
        model.setProductCode(ALI_JSAPI_PAY_CODE);
        model.setTotalAmount(payParam.getTotalFee().toString());
        model.setSubject(payParam.getTitle());
        model.setOpAppId(aliParam.getOpAppId());
        model.setOutTradeNo(payParam.getTransNum());
        model.setBody(JSON.toJSONString(payParam.getExtraData()));
        model.setTimeExpire(DateUtil.format(payParam.getTimeExpire(), DatePattern.NORM_DATETIME_PATTERN));
        request.setBizModel(model);
        AlipayTradeCreateResponse response;
        try {
            log.info("支付宝调用小程序支付参数：{}", JSON.toJSONString(request));
            response = client.pageExecute(request);
            log.info("支付宝调用小程序支付返回：{}", JSON.toJSONString(response));
            if (response.isSuccess()) {
                result.success(response.getBody());
            } else {
                result.fail(PayCode.BUSINESS_FAIL, response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getErrMsg(), e);
        }
        return result;
    }

    private PayResult<String> scenePay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        AlipayTradePayRequest request = new AlipayTradePayRequest();
        AlipayTradePayModel model = new AlipayTradePayModel();
        model.setOutTradeNo(payParam.getTransNum());
        model.setTotalAmount(payParam.getTotalFee().toString());
        model.setScene("bar_code");
        model.setSubject(payParam.getTitle());
        model.setBody(JSON.toJSONString(payParam.getExtraData()));
        model.setAuthCode(payParam.getAuthCode());
        model.setTimeoutExpress("1m");
        request.setBizModel(model);
        return result;
    }

    private PayResult<String> h5Pay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        AlipayTradeWapPayModel model = new AlipayTradeWapPayModel();
        request.setNotifyUrl(aliParam.getNotifyUrl());
        request.setReturnUrl(aliParam.getH5ReturnUrl());
        model.setTotalAmount(payParam.getTotalFee().toString());
        model.setSubject(payParam.getTitle());
        model.setProductCode(ALI_H5_PAY_CODE);
        model.setOutTradeNo(payParam.getTransNum());
        model.setBody(JSON.toJSONString(payParam.getExtraData()));
        model.setTimeExpire(DateUtil.format(payParam.getTimeExpire(), DatePattern.NORM_DATETIME_PATTERN));
        request.setBizModel(model);
        AlipayTradeWapPayResponse response;
        try {
            log.info("支付宝调用h5支付参数：{}", JSON.toJSONString(request));
            response = client.pageExecute(request);
            log.info("支付宝调用h5支付返回：{}", JSON.toJSONString(response));
            if (response.isSuccess()) {
                result.success(response.getBody());
            } else {
                result.fail(PayCode.BUSINESS_FAIL, response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getErrMsg(), e);
        }
        return result;
    }

    private PayResult<String> pcPay(PayParam payParam) {
        PayResult<String> result = new PayResult<>();
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        request.setNotifyUrl(aliParam.getNotifyUrl());
        request.setReturnUrl(aliParam.getPcReturnUrl());
        model.setTotalAmount(payParam.getTotalFee().toString());
        model.setSubject(payParam.getTitle());
        model.setProductCode(ALI_PC_PAY_CODE);
        model.setBody(JSON.toJSONString(payParam.getExtraData()));
        model.setOutTradeNo(payParam.getTransNum());
        model.setTimeExpire(DateUtil.format(payParam.getTimeExpire(), DatePattern.NORM_DATETIME_PATTERN));
        request.setBizModel(model);
        AlipayTradePagePayResponse response;
        try {
            log.info("支付宝调用pc支付参数：{}", JSON.toJSONString(request));
            response = client.pageExecute(request);
            log.info("支付宝调用pc支付返回：{}", JSON.toJSONString(response));
            if (response.isSuccess()) {
                result.success(response.getBody());
            } else {
                result.fail(PayCode.BUSINESS_FAIL, response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getErrMsg(), e);
        }
        return result;
    }

    @Override
    public PayResult<RefundResult> refund(RefundParam refundParam) {
        PayResult<RefundResult> result = new PayResult<>();
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        AlipayTradeRefundModel model = new AlipayTradeRefundModel();
        model.setOutTradeNo(refundParam.getTransNum());
        model.setOutRequestNo(refundParam.getRefundNo());
        model.setRefundReason("正常退款");
        model.setRefundAmount(refundParam.getRefundFee().toString());
        request.setBizModel(model);
        try {
            log.info("支付宝调用退款参数：{}", JSON.toJSONString(request));
            AlipayTradeRefundResponse response = client.pageExecute(request);
            log.info("支付宝调用退款返回：{}", JSON.toJSONString(response));
            if (response.isSuccess()) {
                RefundResult refundResult = new RefundResult();
                refundResult.setRefundTime(DateUtil.date());
                refundResult.setTransNum(response.getOutTradeNo());
                refundResult.setRefundNo(refundParam.getRefundNo());
                refundResult.setOutTradeNo(response.getTradeNo());
                result.success(refundResult);
            } else {
                result.fail(PayCode.BUSINESS_FAIL, response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getErrMsg(), e);
        }
        return result;
    }

    @Override
    public PayResult<QueryResult> payQuery(QueryParam queryParam) {
        PayResult<QueryResult> result = new PayResult<>();
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(queryParam.getTransNum());
        request.setBizModel(model);
        try {
            AlipayTradeQueryResponse response = client.pageExecute(request);
            if (response.isSuccess()){
                if (TRADE_SUCCESS.equals(response.getTradeStatus()) || TRADE_FINISHED.equals(response.getTradeStatus())){
                    QueryResult queryResult = new QueryResult();
                    queryResult.setTransNum(response.getOutTradeNo());
                    queryResult.setSuccessTime(response.getSendPayDate());
                    queryResult.setStatus(response.getTradeStatus());
                    queryResult.setOutTradeNum(response.getTradeNo());
                    result.success(queryResult);
                }
            } else {
                result.fail(PayCode.BUSINESS_FAIL, response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getErrMsg(), e);
        }
        return result;
    }
}
