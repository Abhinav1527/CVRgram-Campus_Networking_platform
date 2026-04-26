package com.CVRgram.Repository;

import com.CVRgram.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    // Fetch history between two users (Sender A to Recipient B OR Sender B to Recipient A)
    @org.springframework.data.jpa.repository.Query("SELECT m FROM Message m WHERE (m.sender.id = :u1 AND m.recipient.id = :u2) OR (m.sender.id = :u2 AND m.recipient.id = :u1) ORDER BY m.timestamp ASC")
    List<Message> findConversation(@org.springframework.data.repository.query.Param("u1") Long u1, @org.springframework.data.repository.query.Param("u2") Long u2);
}
