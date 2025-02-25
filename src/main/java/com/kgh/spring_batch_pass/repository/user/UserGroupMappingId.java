package com.kgh.spring_batch_pass.repository.user;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UserGroupMappingId implements Serializable {
    private String userGroupId;
    private String userId;

}
