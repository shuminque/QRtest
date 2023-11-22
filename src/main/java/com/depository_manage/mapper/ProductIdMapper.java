package com.depository_manage.mapper;

import com.depository_manage.entity.ProductId;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ProductIdMapper {
    ProductId selectProductIdByBoxNumber(String boxNumber);
    List<ProductId> selectAllProductIds();
    void insertOrUpdateProductId(ProductId productId);
}
