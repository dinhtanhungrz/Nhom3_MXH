package com.be_mxh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "friendships",
        indexes = {
                @Index(name = "idx_friendships_requester", columnList = "requester_id"),
                @Index(name = "idx_friendships_addressee", columnList = "addressee_id"),
                @Index(name = "idx_friendships_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_friend_pair",
                        columnNames = {"requester_id", "addressee_id"}
                )
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
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
