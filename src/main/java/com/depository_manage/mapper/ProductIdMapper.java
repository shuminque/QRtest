package com.depository_manage.mapper;

import com.depository_manage.entity.ProductId;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductIdMapper {
    ProductId selectBoxNumberByBoxText(String boxText);
    List<ProductId> selectAllBoxNumbers();
    void insertOrUpdateBoxNumber(ProductId productId);
}
