// PrivateMessageController.java
package com.example.tomatomall.controller;

import com.example.tomatomall.util.Result;
import com.example.tomatomall.service.PrivateMessageService;
import com.example.tomatomall.vo.PrivateMessageVO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class PrivateMessageController {

    private final PrivateMessageService privateMessageService;

    public PrivateMessageController(PrivateMessageService privateMessageService) {
        this.privateMessageService = privateMessageService;
    }    @PostMapping("/send")
    public Result<PrivateMessageVO> sendMessage(@RequestBody Map<String, Object> request) {
        Integer senderId = (Integer) request.get("senderId");
        Integer receiverId = (Integer) request.get("receiverId");
        String content = (String) request.get("content");
        String contentType = (String) request.get("contentType");
        
        PrivateMessageVO message = privateMessageService.sendMessage(senderId, receiverId, content, contentType);
        return Result.success(message);
    }

    @GetMapping("/conversation")
    public Result<Page<PrivateMessageVO>> getConversation(
            @RequestParam Integer userId,
            @RequestParam Integer contactId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<PrivateMessageVO> conversation = privateMessageService.getConversation(userId, contactId, pageable);
        return Result.success(conversation);
    }

    @GetMapping("/contacts")
    public Result<List<Integer>> getContacts(@RequestParam Integer userId) {
        List<Integer> contacts = privateMessageService.getContacts(userId);
        return Result.success(contacts);
    }

    @GetMapping("/latest")
    public Result<List<PrivateMessageVO>> getLatestMessages(
            @RequestParam Integer userId,
            @RequestParam Integer contactId,
            @PageableDefault(size = 5) Pageable pageable) {
        List<PrivateMessageVO> messages = privateMessageService.getLatestMessages(userId, contactId, pageable);
        return Result.success(messages);
    }

    @GetMapping("/unread/count")
    public Result<Integer> getUnreadMessageCount(@RequestParam Integer userId) {
        Integer count = privateMessageService.getUnreadMessageCount(userId);
        return Result.success(count);
    }

    @GetMapping("/unread/count/user")
    public Result<Integer> getUnreadMessageCountFromUser(
            @RequestParam Integer userId,
            @RequestParam Integer senderId) {
        Integer count = privateMessageService.getUnreadMessageCountFromUser(userId, senderId);
        return Result.success(count);
    }

    @PostMapping("/mark-as-read")
    public Result<Void> markMessagesAsRead(
            @RequestParam Integer userId,
            @RequestParam Integer senderId) {
        privateMessageService.markMessagesAsRead(userId, senderId);
        return Result.success();
    }
}