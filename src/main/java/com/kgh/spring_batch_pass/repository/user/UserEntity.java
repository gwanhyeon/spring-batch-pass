package com.kgh.spring_batch_pass.repository.user;

import com.vladmihalcea.hibernate.type.json.JsonStringType;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.util.Map;

@Getter
@Setter
@Entity
@Table(name="user")
@TypeDef(name="json", typeClass = JsonStringType.class)
public class UserEntity {

    @Id
    private String userId;

    private String userName;
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    private String phone;

    @Type(type="json")
    private Map<String, Object> meta;


    public String getUuid(){
        String uuid = null;

        if(meta.containsKey("uuid")){
            uuid = String.valueOf(meta.get("uuid"));
        }
        return uuid;
    }
}
