package com.example.tomatomall.controller;

import com.example.tomatomall.TomatoException.BusinessException;
import com.example.tomatomall.service.CustomerServiceService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.CustomerServiceSessionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customer-service")
@RequiredArgsConstructor
public class CustomerServiceController {

    private final CustomerServiceService customerServiceService;

//    @PostMapping("/sessions")
//    public Result<CustomerServiceSessionVO> createSession(
//            @RequestAttribute Integer userId,
//            @RequestParam String questionType,
//            @RequestParam String content,
//            @RequestParam(required = false) String orderId) {
//        try {
//            CustomerServiceSessionVO session = customerServiceService.createCustomerServiceSession(
//                    userId, questionType, content, orderId);
//            return Result.success(session);
//        } catch (BusinessException e) {
//            return Result.fail(400, e.getMessage());
//        } catch (Exception e) {
//            log.error("创建客服会话失败", e);
//            return Result.fail(500, "创建客服会话失败");
//        }
//    }

    @GetMapping("/sessions")
    public Result<List<CustomerServiceSessionVO>> getSessions(
            @RequestAttribute Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<CustomerServiceSessionVO> sessions = customerServiceService.getCustomerServiceSessions(userId, page, size);
            return Result.success(sessions);
        } catch (Exception e) {
            log.error("获取客服会话列表失败", e);
            return Result.fail(500, "获取客服会话列表失败");
        }
    }

    @GetMapping("/sessions/{sessionId}")
    public Result<CustomerServiceSessionVO> getSession(
            @RequestAttribute Integer userId,
            @PathVariable String sessionId) {
        try {
            CustomerServiceSessionVO session = customerServiceService.getCustomerServiceSession(sessionId, userId);
            return Result.success(session);
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("获取客服会话详情失败", e);
            return Result.fail(500, "获取客服会话详情失败");
        }
    }

    @PostMapping("/sessions/{sessionId}/messages")
    public Result<CustomerServiceSessionVO> sendMessage(
            @RequestAttribute Integer userId,
            @PathVariable String sessionId,
            @RequestParam String content,
            @RequestParam(defaultValue = "text") String contentType) {
        try {
            CustomerServiceSessionVO session = customerServiceService.sendCustomerServiceMessage(
                    sessionId, userId, content, contentType);
            return Result.success(session);
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("发送客服消息失败", e);
            return Result.fail(500, "发送客服消息失败");
        }
    }

    @PostMapping("/sessions/{sessionId}/rate")
    public Result<CustomerServiceSessionVO> rateSession(
            @RequestAttribute Integer userId,
            @PathVariable String sessionId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment) {
        try {
            CustomerServiceSessionVO session = customerServiceService.rateCustomerService(
                    sessionId, rating, comment);
            return Result.success(session);
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("评价客服会话失败", e);
            return Result.fail(500, "评价客服会话失败");
        }
    }

    @PostMapping("/sessions/{sessionId}/close")
    public Result<Void> closeSession(
            @RequestAttribute Integer userId,
            @PathVariable String sessionId) {
        try {
            customerServiceService.closeCustomerServiceSession(sessionId, userId);
            return Result.success();
        } catch (BusinessException e) {
            return Result.fail(400, e.getMessage());
        } catch (Exception e) {
            log.error("关闭客服会话失败", e);
            return Result.fail(500, "关闭客服会话失败");
        }
    }
}