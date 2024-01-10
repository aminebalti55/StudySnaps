package com.example.studysnaps.Repositories;

import com.example.studysnaps.entities.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Integer> {
}