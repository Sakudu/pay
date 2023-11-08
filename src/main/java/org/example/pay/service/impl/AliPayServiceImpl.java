package org.example.pay.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.*;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.*;
import com.alipay.api.response.*;
import lombok.extern.slf4j.Slf4j;
import org.example.pay.common.AliParam;
import org.example.pay.common.PayChannel;
import org.example.pay.common.PayCode;
import org.example.pay.common.PayType;
import org.example.pay.service.PayService;
import org.example.pay.service.VerifySignService;
import org.example.pay.vo.QueryParam;
import org.example.pay.vo.QueryResult;
import org.example.pay.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private VerifySignService verifySignService;

    @Resource
    private DefaultAlipayClient client;

    private static final String ALI_H5_PAY_CODE = "QUICK_WAP_WAY";

    private static final String ALI_JSAPI_PAY_CODE = "JSAPI_PAY";

    private static final String ALI_SCENE_PAY_CODE = "bar_code";

    private static final String ALI_PC_PAY_CODE = "FAST_INSTANT_TRADE_PAY";

    private static final String CALL_BACK_SUCCESS = "success";

    private static final String TRADE_SUCCESS = "TRADE_SUCCESS";

    private static final String TRADE_FINISHED = "TRADE_FINISHED";

    private static final String WAIT_BUYER_PAY = "WAIT_BUYER_PAY";

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
        request.setNotifyUrl(aliParam.getNotifyUrl());
        AlipayTradePayModel model = new AlipayTradePayModel();
        model.setOutTradeNo(payParam.getTransNum());
        model.setTotalAmount(payParam.getTotalFee().toString());
        model.setScene(ALI_SCENE_PAY_CODE);
        model.setSubject(payParam.getTitle());
        model.setBody(JSON.toJSONString(payParam.getExtraData()));
        model.setAuthCode(payParam.getAuthCode());
        model.setTimeoutExpress("1m");
        request.setBizModel(model);
        ResultNotify resultNotify = new ResultNotify();
        try {
            log.info("支付宝调用主扫支付参数：{}", JSON.toJSONString(request));
            AlipayTradePayResponse response = client.pageExecute(request);
            log.info("支付宝调用主扫支付返回：{}", JSON.toJSONString(response));
            if (response.isSuccess()) {
                //返回 10000 代表成功
                if ("10000".equals(response.getCode())) {
                    resultNotify.setPayType(PayType.ALI);
                    resultNotify.setTransNum(response.getOutTradeNo());
                    resultNotify.setOutTradeNo(response.getTradeNo());
                    resultNotify.setPayTime(response.getGmtPayment());
                    resultNotify.setPayAmount(new BigDecimal(response.getPayAmount()));
                    resultNotify.setExtraData(payParam.getExtraData());
                    result.success(JSON.toJSONString(resultNotify));
                } else if ("10003".equals(response.getCode()) || "20000".equals(response.getCode())) {
                    PayResult<QueryResult> queryResult;
                    QueryParam queryParam = new QueryParam();
                    queryParam.setTransNum(payParam.getTransNum());
                    for (int i = 0; i < 14; i++) {
                        queryResult = payQuery(queryParam);
                        if (queryResult.isSuccess()) {
                            resultNotify.setPayType(PayType.ALI);
                            resultNotify.setTransNum(queryResult.getResult().getTransNum());
                            resultNotify.setOutTradeNo(queryResult.getResult().getOutTradeNum());
                            resultNotify.setPayTime(queryResult.getResult().getSuccessTime());
                            resultNotify.setPayAmount(payParam.totalFee);
                            resultNotify.setExtraData(payParam.getExtraData());
                            result.success(JSON.toJSONString(resultNotify));
                            break;
                        } else {
                            if (!queryResult.getCode().equals(PayCode.UNKNOWN_STATUS) || !queryResult.getMsg().equals(WAIT_BUYER_PAY)) {
                                result.fail(PayCode.BUSINESS_FAIL, "用户取消支付");
                                break;
                            }
                        }
                        Thread.sleep(5000);
                    }
                } else {
                    result.fail(PayCode.BUSINESS_FAIL, response.getSubMsg());
                }
            } else {
                result.fail(PayCode.BUSINESS_FAIL, response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getErrMsg(), e);
        } catch (InterruptedException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getMessage(), e);
        }
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
            AlipayTradeRefundResponse response = client.certificateExecute(request);
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
            log.info("支付宝调用查询参数：{}", JSON.toJSONString(request));
            AlipayTradeQueryResponse response = client.certificateExecute(request);
            log.info("支付宝调用查询返回：{}", JSON.toJSONString(response));
            if (response.isSuccess()) {
                if (TRADE_SUCCESS.equals(response.getTradeStatus()) || TRADE_FINISHED.equals(response.getTradeStatus())) {
                    QueryResult queryResult = new QueryResult();
                    queryResult.setTransNum(response.getOutTradeNo());
                    queryResult.setSuccessTime(response.getSendPayDate());
                    queryResult.setStatus(response.getTradeStatus());
                    queryResult.setOutTradeNum(response.getTradeNo());
                    result.success(queryResult);
                }
            } else {
                result.fail(PayCode.BUSINESS_FAIL, response.getTradeStatus().equals(WAIT_BUYER_PAY) ? WAIT_BUYER_PAY : response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getErrMsg(), e);
        }
        return result;
    }

    @Override
    public PayResult<QueryResult> refundQuery(QueryParam param) {
        PayResult<QueryResult> result = new PayResult<>();
        AlipayTradeFastpayRefundQueryRequest request = new AlipayTradeFastpayRefundQueryRequest();
        AlipayTradeFastpayRefundQueryModel model = new AlipayTradeFastpayRefundQueryModel();
        model.setOutRequestNo(param.getTransNum());
        model.setOutTradeNo(param.getRefundNo());
        List<String> list = new ArrayList<>();
        list.add("gmt_refund_pay");
        model.setQueryOptions(list);
        request.setBizModel(model);
        try {
            log.info("支付宝调用退款查询参数：{}", JSON.toJSONString(request));
            AlipayTradeFastpayRefundQueryResponse response = client.certificateExecute(request);
            log.info("支付宝调用退款查询返回：{}", JSON.toJSONString(response));
            if (response.isSuccess()) {
                QueryResult queryResult = new QueryResult();
                queryResult.setOutTradeNum(response.getTradeNo());
                queryResult.setTransNum(response.getOutTradeNo());
                queryResult.setRefundNo(response.getOutRequestNo());
                queryResult.setSuccessTime(response.getGmtRefundPay());
                queryResult.setRefundFee(new BigDecimal(response.getRefundAmount()));
                result.success(queryResult);
            } else {
                result.fail(PayCode.BUSINESS_FAIL, response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getErrMsg(), e);
        }
        return result;
    }

    @Override
    public PayResult<ResultNotify> notify(HttpServletRequest request, HttpServletResponse response) {
        PayResult<ResultNotify> result = new PayResult<>();
        Map<String, String[]> map = request.getParameterMap();
        log.info("支付宝异步回调返回数据：{}", JSON.toJSONString(map));
        Map<String, String> conversionParams = new HashMap<>();
        for (String key : map.keySet()) {
            String[] values = map.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用。如果mySign和sign不相等也可以使用这段代码转化
            // valueStr = new String(valueStr.getBytes("ISO-8859-1"), "UTF-8");
            conversionParams.put(key, valueStr);
        }
        log.info("支付宝异步回调返回转化后数据：{}", JSON.toJSONString(conversionParams));
        String responseMsg = "";
        PrintWriter writer = null;
        try {
            String aliPayPublicKey = getAliPayPublicKey();
            boolean signVerified = AlipaySignature.rsaCertCheckV1(conversionParams, aliPayPublicKey, aliParam.getCharSet(), aliParam.getSignType());
            if (signVerified) {
                responseMsg = CALL_BACK_SUCCESS;
                if (!conversionParams.get("seller_id").equals(aliParam.getUId())
                        || !conversionParams.get("app_id").equals(aliParam.getAppId())
                        || !verifySignService.verifySign(conversionParams)) {
                    result.fail(PayCode.BUSINESS_FAIL, "验签失败");
                    return result;
                }
                //如果这个值存在代表是退款的异步回调
                String outBizNo = conversionParams.get("out_biz_no");
                if (StrUtil.isNotEmpty(outBizNo)) {
                    result.fail(PayCode.BUSINESS_FAIL, "非支付异步回调");
                } else {
                    ResultNotify notify = new ResultNotify();
                    notify.setTransNum(conversionParams.get("out_trade_no"));
                    notify.setPayTime(DateUtil.parseTime(conversionParams.get("gmt_payment")));
                    notify.setPayAmount(new BigDecimal(conversionParams.get("receipt_amount")));
                    notify.setOutTradeNo(conversionParams.get("trade_no"));
                    notify.setExtraData(JSON.parseObject(conversionParams.get("body"), PayExtraData.class));
                    notify.setPayType(PayType.ALI);
                    result.success(notify);
                }
            } else {
                result.fail(PayCode.BUSINESS_FAIL, "验签失败");
            }
            writer = response.getWriter();
        } catch (IOException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getMessage(), e);
        } catch (AlipayApiException e) {
            result.error(PayCode.EXCEPTION_ERROR, e.getErrMsg(), e);
        } finally {
            if (writer != null) {
                writer.write(responseMsg);
                writer.flush();
                writer.close();
            }
        }
        return result;
    }

    private String getAliPayPublicKey() throws FileNotFoundException {
        String osName = System.getProperty("os.name");
        String aliPayPublicKey;
        if (osName.startsWith("Windows")) {
            aliPayPublicKey = ResourceUtils.getURL("classpath:").getPath();
        } else {
            aliPayPublicKey = System.getProperty("user.dir");
        }
        aliPayPublicKey = aliPayPublicKey + aliParam.getAliPayCertPath();
        return aliPayPublicKey;
    }
}
