package com.example.meetty.chat.repository;

import com.example.meetty.chat.entity.ChatMessages;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessages, Long> {
}
