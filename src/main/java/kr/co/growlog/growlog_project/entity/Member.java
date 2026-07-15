package kr.co.growlog.growlog_project.entity;

import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
@Table(name = "MEMBER")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT를 사용하기 때문
    @Column(name = "MEMBER_NO")
    private Long memberNO;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "NICKNAME", nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(name = "USER_NAME", nullable = false, length = 50)
    private String userName;

    @Column(name = "PROFILE_IMAGE", length = 255)
    private String profileImage;

    @Column(name = "CREATED_AT", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

}
