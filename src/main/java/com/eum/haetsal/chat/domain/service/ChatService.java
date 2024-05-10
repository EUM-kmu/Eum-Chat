package com.eum.haetsal.chat.domain.service;

import com.eum.haetsal.chat.domain.client.HaetsalClient;
import com.eum.haetsal.chat.domain.controller.dto.request.HaetsalRequestDto;
import com.eum.haetsal.chat.domain.controller.dto.response.*;
import com.eum.haetsal.chat.domain.model.ChatRoom;
import com.eum.haetsal.chat.domain.model.Message;
import com.eum.haetsal.chat.domain.base.BaseResponseEntity;
import com.eum.haetsal.chat.domain.repository.ChatRepository;
import com.eum.haetsal.chat.domain.repository.ChatRoomRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ChatService implements DisposableBean {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final BroadcastService broadcastService;

    private final HaetsalClient haetsalClient;

    private static final ConcurrentHashMap<String, ConcurrentLinkedQueue<Message>> messageMap = new ConcurrentHashMap<>();
    private static final int transactionMessageSize = 15;
    private static final int messagePageableSize = 15;

    private final MongoTemplate mongoTemplate;

    @Override
    public void destroy() {
        System.out.println("서버가 종료되고 있습니다. 모든 메시지 큐를 처리합니다...");
        messageMap.forEach((roomId, messageQueue) -> commitMessageQueue(messageQueue));
    }

    public BaseResponseEntity<?> saveMessage(String content, String userId, String chatRoomId) {

        Message message = Message.from(chatRoomId,userId, Message.MessageType.CHAT, content);

        try {
            saveInCacheOrDB(chatRoomId, message);
            broadcastService.broadcastMessage(message, chatRoomId);

            return new BaseResponseEntity<>(HttpStatus.OK);
        } catch (Exception e){
            return new BaseResponseEntity<>(e);
        }

    }

    private void saveInCacheOrDB(String chatRoomId, Message message) {

        ConcurrentLinkedQueue<Message> messageQueue = messageMap.get(chatRoomId);

        if(messageMap.get(chatRoomId) == null){
            messageQueue = new ConcurrentLinkedQueue<>();
        }
        messageQueue.add(message);

        if(messageQueue.size() > transactionMessageSize + messagePageableSize){
            ConcurrentLinkedQueue<Message> q = new ConcurrentLinkedQueue<>();

            for(int i =0; i< transactionMessageSize; i++){
                q.add(messageQueue.poll());
            }
            commitMessageQueue(q);
        }

        messageMap.put(chatRoomId, messageQueue);
    }

    private void commitMessageQueue(Queue<Message> messageQueue) {
        int size = messageQueue.size();
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            messages.add(messageQueue.poll());
        }
        bulkInsertMessages(messages);
    }

    public void bulkInsertMessages(List<Message> messages) {
        mongoTemplate.insertAll(messages);
    }

    public BaseResponseEntity<?> getMessagesAndUserInfo(String chatRoomId, int pageNumber, int pageSize) {
        try {

            ChatRoom chatRoom = chatRoomRepository.findChatRoomById(chatRoomId);

            HaetsalResponseDto.PostInfo postInfo = haetsalClient.getChatPost(
                    new HaetsalRequestDto.PostIdList(Collections.singletonList(chatRoom.getPostId()))
                    ).get(0);

            List<HaetsalResponseDto.UserInfo> userInfos = haetsalClient.getChatUser(
                    new HaetsalRequestDto.UserIdList(chatRoom.getMembers())
            );

            // userId를 키로 하고 UserInfo를 값으로 하는 맵 생성
            Map<Long, MessageResponseDTO.SenderInfo> userInfoMap = userInfos.stream()
                    .collect(Collectors.toMap(HaetsalResponseDto.UserInfo::getUserId,
                            userInfo -> new MessageResponseDTO.SenderInfo( // 값 매퍼: UserInfo를 SenderInfo로 변환
                                    userInfo.getUserId(),
                                    userInfo.getProfileImage(),
                                    userInfo.getNickName(),
                                    userInfo.isDeleted()
                            )));

            ConcurrentLinkedQueue<Message> messageQueue = messageMap.get(chatRoomId);

            Queue<Message> messages = null;

            if(messageQueue != null && pageNumber == 0){
                //Cache Hit
                LinkedList<Message> reversedQueue = new LinkedList<>();
                for (Message message : messageQueue) {
                    reversedQueue.addFirst(message);
                }
                messages =reversedQueue;
            }else{
                if(messageQueue != null){
                    pageNumber--;
                }
                Pageable pageable = PageRequest.of(pageNumber, pageSize);
                //Cache Miss
                messages = chatRepository.findAllByChatRoomIdOrderByCreatedAtDesc(chatRoomId, pageable);
            }

            // 메시지와 사용자 정보를 결합하여 MessageResponseDTO 리스트 생성
            List<MessageResponseDTO> messageWithUserInfo = messages.stream()
                    .map(message -> {
                        Long userId = Optional.ofNullable(message.getUserId())  // message.getUserId()가 null일 수 있음
                                .map(Long::parseLong)  // null이 아니면 Long으로 파싱
                                .orElse(null);  // null이면 null 반환

                        MessageResponseDTO.SenderInfo senderInfo = userId != null ? userInfoMap.get(userId) : null;  // userId가 null이 아니면 맵에서 정보 검색, null이면 null

                        return new MessageResponseDTO(
                                senderInfo,  // userInfoMap에서 검색된 SenderInfo 객체 또는 null
                                message.getType(),
                                message.getMessage(),
                                message.getCreatedAt());
                    })
                    .collect(Collectors.toList());

            return new BaseResponseEntity<>(HttpStatus.OK, new ChatUserResponseDto(userInfos, postInfo, messageWithUserInfo));
        }catch (FeignException e) {
            return new BaseResponseEntity<>(HttpStatus.BAD_GATEWAY, "채팅 외부 서비스(햇살 서버의 /chat/users 혹은 /chat/posts)를 불러오는 데 실패했습니다:  " + e.getMessage());
        } catch (Exception e) {
            return new BaseResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR, "채팅 서버 에러: " + e.getMessage());
        }
    }

    @Scheduled(cron = "0 0 3 * * *")  // 매일 새벽 3시에 실행
    public void cleanupOldMessages(){

        LocalDateTime oneWeekAgo = LocalDateTime.now().minus(1, ChronoUnit.WEEKS);

        Iterator<Map.Entry<String, ConcurrentLinkedQueue<Message>>> iterator = messageMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, ConcurrentLinkedQueue<Message>> entry = iterator.next();
            ConcurrentLinkedQueue<Message> queue = entry.getValue();

            System.out.println(entry.getKey());

            Message lastMessage = queue.peek();
            if (lastMessage != null && lastMessage.getCreatedAt().isBefore(oneWeekAgo)) {
                commitMessageQueue(queue);
                iterator.remove();
            }
        }
    }

}
