package com.example.meetty.image.repository;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.image.entity.UserImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserImageRepository extends JpaRepository<UserImageEntity, Long> {
    void deleteByUserEntity(UserEntity userEntity);
}
