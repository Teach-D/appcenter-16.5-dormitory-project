package com.example.appcenter_project.repository.roommate;


import com.example.appcenter_project.entity.roommate.RoommateMatching;
import com.example.appcenter_project.entity.user.User;
import com.example.appcenter_project.enums.roommate.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoommateMatchingRepository extends JpaRepository<RoommateMatching, Long> {

    boolean existsBySenderAndReceiver(User sender, User receiver);
    List<RoommateMatching> findAllByReceiverAndStatus(User receiver, MatchingStatus status);
    boolean existsBySenderAndStatus(User sender, MatchingStatus status);
    boolean existsByReceiverAndStatus(User receiver, MatchingStatus status);


}
