package com.wanfuxiong.findfriends.pojo;

import java.io.InputStream;
import java.io.Serializable;
import java.sql.Blob;
import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data                   // Getter和Setter
@NoArgsConstructor      // 无参构造
@AllArgsConstructor     // 有参构造
public class User implements Serializable {
    private Integer userID;
    private String userUsername;
    private String userPassword;
    private String userSignature;
    private Integer userSex;
    private String userInterest;
    private String userPhoneNumber;
    private String userQQ;
    private byte[] userProfile;
    private Date createDate;
}
