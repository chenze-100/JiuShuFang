package com.JiuShuFang.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
}
