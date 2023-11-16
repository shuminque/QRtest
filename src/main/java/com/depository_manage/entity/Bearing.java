package com.depository_manage.entity;

import lombok.Data;

@Data
public class Bearing {
    private String boxNumber;
    private String productId;
    private String model;
    private String customer;
    private Integer quantity;

    // 构造函数、getter和setter省略
}

