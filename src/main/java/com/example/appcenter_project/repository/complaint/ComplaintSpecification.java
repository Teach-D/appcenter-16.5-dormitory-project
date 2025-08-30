package com.example.appcenter_project.repository.complaint;

import com.example.appcenter_project.entity.complaint.Complaint;
import com.example.appcenter_project.enums.complaint.ComplaintStatus;
import com.example.appcenter_project.enums.complaint.ComplaintType;
import com.example.appcenter_project.enums.user.DormType;
import org.springframework.data.jpa.domain.Specification;

public class ComplaintSpecification {

    public static Specification<Complaint> hasDormType(DormType dormType) {
        return (root, query, cb) ->
                dormType == null ? null : cb.equal(root.get("dormType"), dormType);
    }

    public static Specification<Complaint> hasOfficer(String officer) {
        return (root, query, cb) ->
                (officer == null || officer.isBlank()) ? null : cb.like(root.get("officer"), "%" + officer + "%");
    }

    public static Specification<Complaint> hasCaseNumber(String caseNumber) {
        return (root, query, cb) ->
                (caseNumber == null || caseNumber.isBlank()) ? null : cb.equal(root.get("caseNumber"), caseNumber);
    }

    public static Specification<Complaint> hasStatus(ComplaintStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Complaint> hasKeyword(String keyword) {
        return (root, query, cb) ->
                (keyword == null || keyword.isBlank()) ? null :
                        cb.or(
                                cb.like(root.get("title"), "%" + keyword + "%"),
                                cb.like(root.get("content"), "%" + keyword + "%")
                        );
    }

    public static Specification<Complaint> hasType(ComplaintType type) {
        return (root, query, cb) ->
                type == null ? null : cb.equal(root.get("type"), type);
    }
}