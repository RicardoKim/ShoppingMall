package com.shop.repository;

import com.shop.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByItemNm(String itemNm);
    
//    상품명과 상품상세설명 둘중 어느 하나의 조건에 맞는 상품을 찾는 메소드
    List<Item> findByItemNmOrItemDetail(String itemNm, String itemDetail);

    List<Item> findByPriceLessThan(Integer price);

    List<Item>  findByPriceLessThanOrderByPriceDesc(Integer price);
}
