package com.example.coupon.domain;

import com.example.coupon.utils.EncryptUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "user")
public class User extends AuditLog {
    @MongoId
    private ObjectId id;
    private String username;
    @JsonIgnore
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = EncryptUtils.encrypt(password);
    }

    public String getId() {
        return id.toString();
    }
}
