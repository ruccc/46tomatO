package com.example.tomatomall.controller;

import com.example.tomatomall.service.MessageService;
import com.example.tomatomall.util.Result;
import com.example.tomatomall.vo.PrivateMessageVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public Result<PrivateMessageVO> sendMessage(
            @RequestParam Integer senderId,
            @RequestParam Integer receiverId,
            @RequestParam String content,
            @RequestParam String contentType) {
        PrivateMessageVO message = messageService.sendPrivateMessage(senderId, receiverId, content, contentType);
        return Result.success(message);
    }

    @GetMapping("/conversation")
    public Result<Page<PrivateMessageVO>> getConversation(
            @RequestParam Integer userId,
            @RequestParam Integer contactId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PrivateMessageVO> conversation = messageService.getPrivateMessages(userId, contactId,
                pageable.getPageNumber(), pageable.getPageSize());
        return Result.success(conversation);
    }

    @GetMapping("/contacts")
    public Result<List<Integer>> getContacts(@RequestParam Integer userId) {
        List<Integer> contacts = messageService.getContacts(userId);
        return Result.success(contacts);
    }

    @GetMapping("/latest")
    public Result<List<PrivateMessageVO>> getLatestMessages(
            @RequestParam Integer userId,
            @RequestParam Integer contactId,
            @RequestParam(defaultValue = "5") int size) {
        List<PrivateMessageVO> messages = messageService.getLatestMessages(userId, contactId, 0, size);
        return Result.success(messages);
    }

    @GetMapping("/unread/count")
    public Result<Integer> getUnreadMessageCount(@RequestParam Integer userId) {
        Integer count = messageService.getUnreadMessageCount(userId);
        return Result.success(count);
    }

    @GetMapping("/unread/count/user")
    public Result<Integer> getUnreadMessageCountFromUser(
            @RequestParam Integer userId,
            @RequestParam Integer senderId) {
        Integer count = messageService.getUnreadMessageCountFromUser(userId, senderId);
        return Result.success(count);
    }

    @PostMapping("/mark-as-read")
    public Result<Void> markMessagesAsRead(
            @RequestParam Integer userId,
            @RequestParam Integer senderId) {
        messageService.markMessagesAsRead(userId, senderId);
        return Result.success();
    }
}