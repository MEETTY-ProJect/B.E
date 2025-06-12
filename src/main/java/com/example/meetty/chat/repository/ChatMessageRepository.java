package com.example.meetty.chat.repository;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {
    void deleteAllBySender(UserEntity userEntity);
}
