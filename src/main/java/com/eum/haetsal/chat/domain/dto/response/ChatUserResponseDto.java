package com.eum.haetsal.chat.domain.dto.response;

import com.eum.haetsal.chat.domain.dto.request.ChatUserRequestDto;
import com.eum.haetsal.chat.domain.model.Message;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
public class ChatUserResponseDto {

    List<ChatUserRequestDto> userProfiles;

    List<MessageResponseDTO> messages;

    public ChatUserResponseDto(List<ChatUserRequestDto> userProfiles, List<MessageResponseDTO> messages) {
        this.userProfiles = userProfiles;
        this.messages = messages;
    }
}
