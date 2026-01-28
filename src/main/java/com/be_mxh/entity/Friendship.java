package com.be_mxh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "friendships",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"requester_id", "addressee_id"})
        })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "requester_id", nullable = false)
        private User requester;

        @ManyToOne
        @JoinColumn(name = "addressee_id", nullable = false)
        private User addressee;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 20)
        @Builder.Default
        private Status status = Status.PENDING;

        public enum Status {
                PENDING,
                ACCEPTED,
                REJECTED,
                BLOCKED
        }
}
