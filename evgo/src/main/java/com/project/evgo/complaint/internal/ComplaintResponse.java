package com.project.evgo.complaint.internal;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * ComplaintResponse entity - admin responses to complaints.
 * Internal - not accessible by other modules.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "complaint_responses")
public class ComplaintResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    // Id of admin or staff who responses to the complaint
    @Column(name = "response_by", nullable = false)
    private Long responseBy;

    @Column(nullable = false, length = 2000)
    private String message;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
