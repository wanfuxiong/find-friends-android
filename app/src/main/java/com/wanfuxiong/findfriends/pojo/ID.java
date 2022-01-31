package com.wanfuxiong.findfriends.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data                   // Getter和Setter
@NoArgsConstructor      // 无参构造
@AllArgsConstructor     // 有参构造
public class ID {
    private Integer userId;
    private Boolean used;
}
