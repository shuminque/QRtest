package com.depository_manage.entity;

import lombok.Data;

@Data
public class ProductId {
    private String boxText;
    private String boxNumber;
    private Integer quantity;

    // Lombok @Data 注解会自动生成构造函数、getter、setter、toString等
}
