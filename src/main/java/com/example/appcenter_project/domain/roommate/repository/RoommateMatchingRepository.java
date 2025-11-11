package com.example.appcenter_project.domain.roommate.repository;


import com.example.appcenter_project.domain.roommate.entity.RoommateMatching;
import com.example.appcenter_project.domain.user.entity.User;
import com.example.appcenter_project.domain.roommate.enums.MatchingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoommateMatchingRepository extends JpaRepository<RoommateMatching, Long> {

    boolean existsBySenderAndReceiver(User sender, User receiver);
    List<RoommateMatching> findAllByReceiverAndStatus(User receiver, MatchingStatus status);
    boolean existsBySenderAndStatus(User sender, MatchingStatus status);
    boolean existsByReceiverAndStatus(User receiver, MatchingStatus status);

    Optional<RoommateMatching> findBySenderAndReceiverAndStatus(User sender, User receiver, MatchingStatus status);

}
