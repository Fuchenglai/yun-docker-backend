package com.lfc.clouddocker.controller;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lfc.clouddocker.common.ErrorCode;
import com.lfc.clouddocker.exception.BusinessException;
import com.lfc.clouddocker.manager.AlipayTemplate;
import com.lfc.clouddocker.model.entity.User;
import com.lfc.clouddocker.model.entity.YunOrder;
import com.lfc.clouddocker.model.vo.PayAsyncVO;
import com.lfc.clouddocker.service.UserService;
import com.lfc.clouddocker.service.YunOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 支付宝支付接口
 *
 * @author laifucheng
 */

@Controller
@RequestMapping("/alipay")
@Slf4j
public class AliPayController {

    @Resource
    AlipayTemplate alipayTemplate;

    @Resource
    private YunOrderService orderService;
    @Resource
    private UserService userService;


    //@GetMapping(value = "/pay", produces = "text/html")
    @GetMapping(value = "/pay")
    @ResponseBody
    public String pay(@RequestParam String orderId, HttpServletRequest request) throws AlipayApiException {

        if (orderId == null || orderId.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);

        // 根据订单id和userId查询订单
        QueryWrapper<YunOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id", orderId).eq("buyer_id", loginUser.getId());
        YunOrder order = orderService.getOne(queryWrapper);
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_ERROR);
        }

        return alipayTemplate.pay(order);
    }

    /**
     * 只有状态是TRADE_SUCCESS才会触发通知
     *
     * @param request
     * @return
     * @throws Exception
     */
    @ResponseBody
    @PostMapping("/notify")  // 注意这里必须是POST接口
    public String payNotify(PayAsyncVO vo, HttpServletRequest request) throws Exception {

        //验签
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iterator = requestParams.keySet().iterator(); iterator.hasNext(); ) {
            String key = (String) iterator.next();
            String[] values = (String[]) requestParams.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }

            //乱码解决
            valueStr = new String((valueStr).getBytes("ISO-8859-1"), "utf-8");
            params.put(key, valueStr);
        }

        orderService.handlePayResult(vo);
        return "success";


        /*boolean signVerified = AlipaySignature.rsaCheckV2(params, alipayTemplate.getAlipayPublicKey(), alipayTemplate.getCharset(), alipayTemplate.getSignType());

        if (signVerified) {
            //验签通过
            log.info("验签通过");

            String res = orderService.handlePayResult(vo);
            return "res";
        } else {
            log.info("验签失败");
            return "error";
        }*/
    }
}
