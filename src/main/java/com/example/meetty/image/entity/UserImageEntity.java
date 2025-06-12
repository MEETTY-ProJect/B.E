package com.example.meetty.image.entity;

import com.example.meetty.auth.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_images")
public class UserImageEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long userImageId;

    @Column(name = "url", nullable = false)
    private String url;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity userEntity;

    public UserImageEntity(UserEntity userEntity, String url) {
        this.userEntity = userEntity;
        this.url = url;

        userEntity.setUserImageEntity(this);
    }
}
