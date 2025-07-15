package com.example.appcenter_project.dto.cache;

import com.example.appcenter_project.entity.groupOrder.GroupOrder;
import com.example.appcenter_project.entity.groupOrder.GroupOrderComment;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.catalina.User;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ParentCommentCacheDto {

    private Long id;
/*    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private String reply;
    private GroupOrder groupOrder;
    private User user;
    private GroupOrderComment parentGroupOrderComment;
    private List<GroupOrderComment> childGroupOrderComments = new ArrayList<>();
    private boolean isDeleted;*/

}
