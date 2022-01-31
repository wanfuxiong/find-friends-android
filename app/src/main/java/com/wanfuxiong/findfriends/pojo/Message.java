package com.wanfuxiong.findfriends.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Integer toUserID;
    private Integer fromUserID;
    private String messageData;
    private Date date;
}
