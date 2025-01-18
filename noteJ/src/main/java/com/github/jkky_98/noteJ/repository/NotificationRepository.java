package com.github.jkky_98.noteJ.repository;

import com.github.jkky_98.noteJ.domain.Notification;
import com.github.jkky_98.noteJ.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("SELECT n FROM Notification n WHERE n.receiver = :user")
    List<Notification> findAllNotificationsByReceiver(@Param("user") User user);

    @Query("SELECT COUNT(n) FROM Notification n WHERE n.receiver = :user AND n.status = false")
    long countUnreadNotificationsByReceiver(@Param("user") User user);

    @Query("SELECT n FROM Notification n WHERE n.receiver = :user AND n.status = false")
    List<Notification> findAllUnreadNotificationsByReceiver(@Param("user") User user);
}
