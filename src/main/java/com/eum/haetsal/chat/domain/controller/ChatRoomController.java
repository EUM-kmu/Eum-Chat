package com.eum.haetsal.chat.domain.controller;

import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.controller.dto.request.RoomRequestDto;
import com.eum.haetsal.chat.domain.model.MarketPostStatus;
import com.eum.haetsal.chat.domain.service.ChatRoomService;
import com.eum.haetsal.chat.domain.service.ValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import static com.eum.haetsal.chat.domain.model.MarketPostStatus.RECRUITMENT_COMPLETED;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat-service/api/chatrooms")
@Tag(name = "채팅방" ,description = "채팅방 api")
public class ChatRoomController {

    private final ValidationService validationService;
    private final ChatRoomService chatRoomService;

    @PostMapping("")
    @Operation(summary = "채팅방을 생성합니다.")
    public BaseResponseEntity<?> createChatRoom(
            @RequestBody RoomRequestDto dto,
            @RequestHeader String userId
    ) {
        BaseResponseEntity response = chatRoomService.createChatRoom(dto, userId);
        return response;
    }

    @GetMapping("")
    @Operation(summary = "특정 유저가 속한 채팅방 목록을 불러옵니다.")
    public BaseResponseEntity<?> getMyChatRooms(
            @Schema(description = "게시글 상태." +
                    "\n타입: RECRUITMENT_COMPLETED / TRANSACTION_COMPLETED" +
                    "\n타입 지정안할 시 default=RECRUITMENT_COMPLETED", example = "")
            @RequestParam(value = "status", defaultValue="RECRUITMENT_COMPLETED") MarketPostStatus status,
            @RequestHeader String userId) {
        BaseResponseEntity response = chatRoomService.getChatRooms(userId, status);
        return response;
    }

    @GetMapping("/{chatRoomId}/members")
    @Operation(summary = "유저가 속한 특정 채팅방의 멤버 목록을 불러옵니다.")
    public BaseResponseEntity<?> getMembers(
            @PathVariable String chatRoomId,
            @RequestHeader String userId
    ) {

        BaseResponseEntity<String> validateResponse = validationService.validateChatRoomAccess(chatRoomId, userId);
        if(validateResponse != null){
            return validateResponse;
        }

        BaseResponseEntity response =  chatRoomService.getMembers(chatRoomId);
        return response;
    }


    @PatchMapping("/{chatRoomId}/members")
    @Operation(summary = "특정 채팅방에 새로운 유저를 추가/삭제합니다.")
    public BaseResponseEntity<?> updateMembers(
            @PathVariable String chatRoomId,
            @RequestBody RoomRequestDto dto,
            @RequestHeader String userId
    ) {

        BaseResponseEntity<String> validateResponse = validationService.validateChatRoomAccess(chatRoomId, userId);
        if(validateResponse != null){
            return validateResponse;
        }

        BaseResponseEntity response = chatRoomService.updateMembers(chatRoomId, dto, userId);
        return response;
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "postId -> roomId list ")
    public BaseResponseEntity<?> getRoomIds(
            @PathVariable int postId
    ) {

        BaseResponseEntity response = chatRoomService.getRoomIds(postId);
        return response;
    }
}
