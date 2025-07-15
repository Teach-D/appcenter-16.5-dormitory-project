package com.example.appcenter_project.repository.roommate;


import com.example.appcenter_project.entity.roommate.RoommateMatching;
import com.example.appcenter_project.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoommateMatchingRepository extends JpaRepository<RoommateMatching, Long> {

    boolean existsBySenderAndReceiver(User sender, User receiver);

}
