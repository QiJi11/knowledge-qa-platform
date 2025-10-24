package com.design.chat.repository;

import com.design.chat.model.ChatMessage;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChatLogRepository {
  private final NamedParameterJdbcTemplate jdbc;

  public ChatLogRepository(NamedParameterJdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public void saveMessages(String sessionId, List<ChatMessage> messages) {
    if (messages == null || messages.isEmpty()) {
      return;
    }

    MapSqlParameterSource[] batchParams =
      messages
        .stream()
        .map(
          message ->
            new MapSqlParameterSource()
              .addValue("sessionId", sessionId)
              .addValue("role", message.getRole())
              .addValue("content", message.getContent())
        )
        .toArray(MapSqlParameterSource[]::new);

    jdbc.batchUpdate(
      "INSERT INTO chat_log (session_id, role, content) VALUES (:sessionId, :role, :content)",
      batchParams
    );
  }
}
