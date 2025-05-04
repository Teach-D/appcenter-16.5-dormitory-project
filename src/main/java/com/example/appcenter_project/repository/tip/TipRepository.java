package com.example.appcenter_project.repository.tip;

import com.example.appcenter_project.entity.tip.Tip;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipRepository extends JpaRepository<Tip, Long> {
}
