package com.kgh.spring_batch_pass.repository.user;


import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserGroupMappingRepository extends JpaRepository<UserGroupMappingEntity, Integer> {
    List<UserGroupMappingEntity> findByUserGroupId(String userGroupId);
}
