package com.depository_manage.entity;

import lombok.Data;

@Data
public class ProductId {
    private String boxNumber;
    private String productId;

    // Lombok @Data 注解会自动生成构造函数、getter、setter、toString等
}
