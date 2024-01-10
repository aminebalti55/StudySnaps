package com.example.studysnaps.services;

import com.example.studysnaps.Repositories.PDFDocumentRepository;
import com.example.studysnaps.Repositories.RoomRepository;
import com.example.studysnaps.Repositories.UserRepository;
import com.example.studysnaps.dto.EntityToDtoMapper;
import com.example.studysnaps.dto.RoomDTO;
import com.example.studysnaps.entities.Room;
import com.example.studysnaps.entities.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RoomService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    PdfDocumentService pdfDocumentService;
    @Autowired
    RoomRepository roomRepository;

    @Autowired
    TagService tagService;

    @Autowired
    PDFDocumentRepository pdfDocumentRepository;

    @Transactional
    public RoomDTO createRoom(String opponentUsername) {
        Room room = new Room();

        UserDetails userDetails =
                (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userEmail = userDetails.getUsername();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));


        User opponentUser = userRepository.findByUsername(opponentUsername);
        if (opponentUser == null) {
            throw new UsernameNotFoundException("Opponent not found");
        }

        List<User> usersInRoom = new ArrayList<>();
        usersInRoom.add(currentUser);
        usersInRoom.add(opponentUser);
        room.setUsers(usersInRoom);

        room.setOwnerId(currentUser.getUserId());

        room.setIsActive(true);
        room.setWinner(null);

        roomRepository.save(room);

        return new EntityToDtoMapper().toRoomDTO(room);
    }

}
