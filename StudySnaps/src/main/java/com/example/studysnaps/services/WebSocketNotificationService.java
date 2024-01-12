package com.example.studysnaps.services;

import com.example.studysnaps.dto.EntityToDtoMapper;
import com.example.studysnaps.dto.RoomDTO;
import com.example.studysnaps.entities.Room;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service

public class WebSocketNotificationService {

    @Autowired
    private  EntityToDtoMapper entityToDtoMapper;



    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();


    public void notifyUserOfMatch(String username, Room room) {
        WebSocketSession userSession = userSessions.get(username);
        if (userSession != null) {
            if (userSession.isOpen()) {
                try {
                    String roomDetailsMessage = createRoomDetailsMessage(room);
                    userSession.sendMessage(new TextMessage(roomDetailsMessage));
                } catch (IOException e) {
                    // Log the error and rethrow it as a runtime exception
                    System.err.println("Failed to send message to user: " + username);
                    e.printStackTrace();
                    throw new RuntimeException("Failed to send message to user: " + username, e);
                }
            } else {
                // Log the error and throw a runtime exception
                System.err.println("WebSocket session is not open for user: " + username);
                throw new RuntimeException("WebSocket session is not open for user: " + username);
            }
        } else {
            // Log the error and throw a runtime exception
            System.err.println("No WebSocket session found for user: " + username);
            throw new RuntimeException("No WebSocket session found for user: " + username);
        }
    }


    private String createRoomDetailsMessage(Room room) {
        ObjectMapper objectMapper = new ObjectMapper();

        // Convert the Room entity to RoomDTO
        RoomDTO roomDTO = entityToDtoMapper.toRoomDTO(room);

        try {
            // Convert roomDTO object to JSON string
            return objectMapper.writeValueAsString(roomDTO);
        } catch (Exception e) {
            // Handle exception: log it, inform the user, etc.
            e.printStackTrace();
            return "{\"error\":\"Unable to process room details.\"}";
        }
    }
}
