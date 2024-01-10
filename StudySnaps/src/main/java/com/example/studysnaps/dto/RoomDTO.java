package com.example.studysnaps.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class RoomDTO {
    private Integer roomId;
    private List<UserDTO> users;
    private Boolean isActive;
    private Integer ownerId;
}
