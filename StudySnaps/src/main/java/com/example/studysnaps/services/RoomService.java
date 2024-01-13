package com.example.studysnaps.services;

import com.example.studysnaps.Repositories.PDFDocumentRepository;
import com.example.studysnaps.Repositories.RoomRepository;
import com.example.studysnaps.Repositories.UserRepository;
import com.example.studysnaps.dto.EntityToDtoMapper;
import com.example.studysnaps.dto.RoomDTO;
import com.example.studysnaps.dto.UserDTO;
import com.example.studysnaps.entities.Room;
import com.example.studysnaps.entities.User;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@EnableScheduling

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
@Autowired
EntityToDtoMapper entityToDtoMapper;
    @Autowired
    private WebSocketNotificationService webSocketNotificationService;
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);


  /*  @Transactional
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
    }*/

    private final Queue<String> waitingUsers = new ConcurrentLinkedQueue<>();

    public void addToQuickMatchQueue(String username) {
        waitingUsers.add(username);
        System.out.println("Added user '" + username + "' to the quick match queue.");
    }

    @Transactional
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    public void processQuickMatchQueue() {
        if (waitingUsers.size() < 2) {
            return;
        }

        System.out.println("Scheduled task is running...");

        List<User> users = new ArrayList<>();
        for (String email : waitingUsers) {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                users.add(userOptional.get());
            } else {
                System.out.println("User with email '" + email + "' not found in the userRepository.");
            }
        }
        waitingUsers.clear();

        Map<String, List<UserDTO>> usersByEmailAndClass = new HashMap<>();
        for (User user : users) {
            String key = user.getUniversity() + "#" + user.getUserClass();
            UserDTO dto = entityToDtoMapper.toUserDTO(user);
            usersByEmailAndClass.computeIfAbsent(key, k -> new ArrayList<>()).add(dto);
            System.out.println("Added user '" + dto.getEmail() + "' to usersByEmailAndClass for key: " + key);
        }

        System.out.println("Users by University and Class:");
        usersByEmailAndClass.forEach((key, userList) -> {
            System.out.println("Key: " + key);
            System.out.println("User List: " + userList);
        });

        usersByEmailAndClass.forEach((key, userList) -> {
            while (userList.size() >= 2) {
                UserDTO user1 = userList.remove(0);
                UserDTO user2 = userList.remove(0);

                System.out.println("User 1: " + user1.getEmail());
                System.out.println("User 2: " + user2.getEmail());

                if (user1.getUniversity().equals(user2.getUniversity()) && user1.getUserClass().equals(user2.getUserClass())) {
                    System.out.println("Creating room for User 1: " + user1.getEmail() + " and User 2: " + user2.getEmail());
                    createRoom(user1, user2);
                }
            }
        });
    }

    public void createRoom(UserDTO user1, UserDTO user2) {
        String user1University = user1.getUniversity();
        String user1Class = user1.getUserClass();

        System.out.println("Creating room for User 1: " + user1.getEmail() + " (" + user1University + ", " + user1Class + ") and User 2: " + user2.getEmail());

        Room room = new Room();

        User user1Entity = convertDtoToEntity(user1);
        User user2Entity = convertDtoToEntity(user2);

        room.setUsers(Arrays.asList(user1Entity, user2Entity));
        room.setIsActive(true);
        room.setOwnerId(user1Entity.getUserId());

        roomRepository.save(room);

        user1Entity.setRoom(room);
        user2Entity.setRoom(room);

        User existingUser1 = userRepository.findById(user1Entity.getUserId()).orElse(null);
        User existingUser2 = userRepository.findById(user2Entity.getUserId()).orElse(null);

        if (existingUser1 != null) {
            user1Entity.setPassword(existingUser1.getPassword());
        }

        if (existingUser2 != null) {
            user2Entity.setPassword(existingUser2.getPassword());
        }

        userRepository.save(user1Entity);
        userRepository.save(user2Entity);
        // notifyUsers(room);
    }


    private void notifyUsers(Room room) {

        for (User user : room.getUsers()) {
            webSocketNotificationService.notifyUserOfMatch(user.getUsername(), room);
        }
    }

    private User convertDtoToEntity(UserDTO dto) {
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        return user;
    }

}
