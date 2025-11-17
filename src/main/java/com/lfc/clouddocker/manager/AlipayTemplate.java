package com.lfc.clouddocker.manager;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.lfc.clouddocker.model.entity.YunOrder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 支付宝支付模板类
 *
 * @author laifucheng
 */
@ConfigurationProperties(prefix = "alipay")
@Component
@Data
@Slf4j
public class AlipayTemplate {
    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    @Value("${alipay.appId}")
    public String appId;

    // 应用私钥，就是工具生成的应用私钥
    @Value("${alipay.merchantPrivateKey}")
    public String merchantPrivateKey;
    // 支付宝公钥,对应APPID下的支付宝公钥。
    @Value("${alipay.alipayPublicKey}")
    public String alipayPublicKey;

    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    @Value("${alipay.notifyUrl}")
    public String notifyUrl;
    //同步通知，支付成功，一般跳转到成功页
    @Value("${alipay.returnUrl}")
    public String returnUrl;

    // 签名方式
    @Value("${alipay.signType}")
    private String signType;

    // 字符编码格式
    @Value("${alipay.charset}")
    private String charset;

    //订单超时时间
    private String timeout = "1m";
    // 支付宝网关；https://openapi-sandbox.dl.alipaydev.com/gateway.do
    @Value("${alipay.gatewayUrl}")
    public String gatewayUrl;


    public String pay(YunOrder order) throws AlipayApiException {

        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new
                DefaultAlipayClient(gatewayUrl, appId, merchantPrivateKey,
                "json", charset, alipayPublicKey, signType);

        //2、创建一个支付请求，并设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(returnUrl);
        alipayRequest.setNotifyUrl(notifyUrl);

        String orderId = order.getOrderId();
        //Long interfaceInfoId = order.getInterfaceInfoId();
        BigDecimal bigDecimal = order.getMoney().setScale(2, BigDecimal.ROUND_UP);
        String money = bigDecimal.toString();
        //String paymentMethod = order.getPaymentMethod();


        /*JSONObject jsonObject = new JSONObject();
        //订单号 商户订单号。由商家自定义，64个字符以内，仅支持字母、数字、下划线且需保证在商户端不重复。
        jsonObject.set("out_trade_no", orderId);
        //订单总金额，单位为元，精确到小数点后两位，取值范围为 [0.01,100000000]。金额不能为0。
        jsonObject.set("total_amount", money);
        //商品名
        jsonObject.set("subject", "积分充值");
        //销售产品码，商家和支付宝签约的产品码。手机网站支付为：QUICK_WAP_WAY
        jsonObject.set("product_code", "QUICK_WAP_PAY");*/
        /**
         * 这里格式化返回的json字符串注意 identFactor参数一定是0,不然
         * 到时候格式化到html就会出现 \n等特殊符号,导致验签失败
         */
        /*alipayRequest.setBizContent(jsonObject.toJSONString(0));*/


        alipayRequest.setBizContent("{\"out_trade_no\":\"" + orderId + "\","
                + "\"total_amount\":\"" + money + "\","
                + "\"subject\":\"" + "积分充值" + "\","
                //+ "\"body\":\"" + paymentMethod + "\","  body是订单备注
                +
                "\"timeout_express\":\"" + timeout + "\","
                +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String result = alipayClient.pageExecute(alipayRequest).getBody();
        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        log.info("支付宝的响应：{}", result);
        return result;
    }
}
