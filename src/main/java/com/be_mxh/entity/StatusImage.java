package com.be_mxh.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "status_images")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatusImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", nullable = false)
    private Status status;

    private String url;

    private String publicId;
}
