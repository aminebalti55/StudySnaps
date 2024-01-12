package com.example.studysnaps.dto;

import com.example.studysnaps.entities.Room;
import com.example.studysnaps.entities.User;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
@Service
public class EntityToDtoMapper {

    public UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setUserClass(user.getUserClass());
        dto.setUniversity(user.getUniversity());
        return dto;
    }

    public RoomDTO toRoomDTO(Room room) {
        RoomDTO dto = new RoomDTO();
        dto.setRoomId(room.getRoomId());
        dto.setIsActive(room.getIsActive());
        dto.setOwnerId(room.getOwnerId());
        dto.setUsers(room.getUsers().stream().map(this::toUserDTO).collect(Collectors.toList()));
        return dto;
    }
}
