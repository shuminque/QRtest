package com.depository_manage.entity;

import lombok.Data;

@Data
public class ProductId {
    private String boxText;
    private String boxNumber;
    private Integer quantity; // 具体装箱数
    private Integer depositoryId; // 厂区ID

    // Lombok @Data 注解会自动生成构造函数、getter、setter、toString等
}
