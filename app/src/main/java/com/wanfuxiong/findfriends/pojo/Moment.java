package com.wanfuxiong.findfriends.pojo;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Moment {
    private Integer userID;
    private String momentContent;
    private byte[] momentImage;
    private Date sendDate;
}
