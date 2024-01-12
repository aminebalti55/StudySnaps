package com.example.studysnaps.services;

import com.example.studysnaps.Repositories.PDFDocumentRepository;
import com.example.studysnaps.Repositories.RoomRepository;
import com.example.studysnaps.Repositories.UserRepository;
import com.example.studysnaps.dto.EntityToDtoMapper;
import com.example.studysnaps.dto.RoomDTO;
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

        Map<String, List<String>> usersByEmailAndClass = new HashMap<>();

        while (!waitingUsers.isEmpty()) {
            String email = waitingUsers.poll();
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                String key = user.getUniversity() + "#" + user.getUserClass();
                usersByEmailAndClass.computeIfAbsent(key, k -> new ArrayList<>()).add(email);
                System.out.println("Added user '" + email + "' to usersByEmailAndClass for key: " + key);
            } else {
                System.out.println("User with email '" + email + "' not found in the userRepository.");
            }
        }

        System.out.println("Users by University and Class:");
        usersByEmailAndClass.forEach((key, userList) -> {
            System.out.println("Key: " + key);
            System.out.println("User List: " + userList);
        });

        // Create matches for users in the same university and class.
        usersByEmailAndClass.forEach((key, usersList) -> {
            while (usersList.size() >= 2) {
                String user1Email = usersList.remove(0);
                String user2Email = usersList.remove(0);

                Optional<User> user1Optional = userRepository.findByEmail(user1Email);
                Optional<User> user2Optional = userRepository.findByEmail(user2Email);

                if (user1Optional.isPresent() && user2Optional.isPresent()) {
                    User user1 = user1Optional.get();
                    User user2 = user2Optional.get();

                    System.out.println("User 1: " + user1);
                    System.out.println("User 2: " + user2);

                    if (user1.getUniversity().equals(user2.getUniversity()) && user1.getUserClass().equals(user2.getUserClass())) {
                        System.out.println("Creating room for User 1: " + user1.getEmail() + " and User 2: " + user2.getEmail());
                        createRoom(user1.getEmail(), user2);
                    }
                }
            }
        });
    }

    public void createRoom(String userEmail, User user2) {
        Optional<User> user1Optional = userRepository.findByEmail(userEmail);
        if (user1Optional.isPresent()) {
            User user1 = user1Optional.get();
            String user1University = user1.getUniversity();
            String user1Class = user1.getUserClass();

            System.out.println("Creating room for User 1: " + user1.getEmail() + " (" + user1University + ", " + user1Class + ") and User 2: " + user2.getEmail());

            Room room = new Room();

            room.setUsers(Arrays.asList(user1, user2));
            room.setIsActive(true);
            room.setOwnerId(user1.getUserId());

            user1.setRoom(room);
            user2.setRoom(room);

            userRepository.save(user1);
            userRepository.save(user2);
            roomRepository.save(room);
           // notifyUsers(room);
        } else {
            System.out.println("User with email '" + userEmail + "' not found in the userRepository.");
        }
    }


    private void notifyUsers(Room room) {
        // Iterate over the users in the room and send them the room details
        for (User user : room.getUsers()) {
            webSocketNotificationService.notifyUserOfMatch(user.getUsername(), room);
        }
    }

}
