package com.example.tomatomall.controller;

import com.example.tomatomall.service.MessageService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.PrivateConversationVO;
import com.example.tomatomall.vo.PrivateMessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/private")
    public Result<PrivateMessageVO> sendPrivateMessage(
            @RequestAttribute Integer userId,
            @RequestParam Integer receiverId,
            @RequestParam String content,
            @RequestParam(defaultValue = "text") String contentType) {
        try {
            PrivateMessageVO message = messageService.sendPrivateMessage(userId, receiverId, content, contentType);
            return Result.success(message);
        } catch (Exception e) {
            log.error("发送私信失败", e);
            return Result.fail(500, "发送私信失败");
        }
    }

    @GetMapping("/private/conversations")
    public Result<Page<PrivateConversationVO>> getPrivateConversations(
            @RequestAttribute Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<PrivateConversationVO> conversations = messageService.getPrivateConversations(userId, page, size);
            return Result.success(conversations);
        } catch (Exception e) {
            log.error("获取私信会话列表失败", e);
            return Result.fail(500, "获取私信会话列表失败");
        }
    }

    @GetMapping("/private/messages")
    public Result<Page<PrivateMessageVO>> getPrivateMessages(
            @RequestAttribute Integer userId,
            @RequestParam Integer contactId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Page<PrivateMessageVO> messages = messageService.getPrivateMessages(userId, contactId, page, size);
            return Result.success(messages);
        } catch (Exception e) {
            log.error("获取私信消息失败", e);
            return Result.fail(500, "获取私信消息失败");
        }
    }

    @PostMapping("/private/read")
    public Result<Void> markMessagesAsRead(
            @RequestAttribute Integer userId,
            @RequestParam Integer senderId) {
        try {
            messageService.markMessagesAsRead(userId, senderId);
            return Result.success();
        } catch (Exception e) {
            log.error("标记消息为已读失败", e);
            return Result.fail(500, "标记消息为已读失败");
        }
    }

    @GetMapping("/private/unread-count")
    public Result<Integer> getUnreadMessageCount(@RequestAttribute Integer userId) {
        try {
            int count = messageService.getUnreadMessageCount(userId);
            return Result.success(count);
        } catch (Exception e) {
            log.error("获取未读消息数量失败", e);
            return Result.fail(500, "获取未读消息数量失败");
        }
    }
}