package com.shop.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.constant.ItemSellStatus;
import com.shop.dto.ItemSearchDto;
import com.shop.dto.MainItemDto;
import com.shop.dto.QMainItemDto;
import com.shop.entity.Item;
import com.shop.entity.QItem;

import com.shop.entity.QItemImg;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.thymeleaf.util.StringUtils;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

public class ItemRepositoryCustomImpl implements ItemRepositoryCustom{

    private JPAQueryFactory queryFactory;

    public ItemRepositoryCustomImpl(EntityManager em){
        this.queryFactory = new JPAQueryFactory(em);
    }

    private BooleanExpression searchSellStatusEq(ItemSellStatus searchSellStatus){
        //QItem.item.itemSellStatus가 null이면 null을 반환하고 : 아니면 그냥 결과값을 반환한다.
        return searchSellStatus == null? null : QItem.item.itemSellStatus.eq(searchSellStatus);
    }

    private BooleanExpression itemNmLike(String searchQuery){
        return StringUtils.isEmpty(searchQuery) ? null : QItem.item.itemNm.like("%" + searchQuery + "%");
    }

    private BooleanExpression regDtsAfter(String searchDataType){
        LocalDateTime dateTime = LocalDateTime.now();

        //조건에 따라 현재 시간에서 특정 값을 뺀 후
        if(StringUtils.equals("all", searchDataType) || searchDataType == null){
            return null;
        }else if(StringUtils.equals("1d", searchDataType)){
            dateTime = dateTime.minusDays(1);
        }else if(StringUtils.equals("1w", searchDataType)){
            dateTime = dateTime.minusWeeks(1);
        }else if(StringUtils.equals("1m", searchDataType)){
            dateTime = dateTime.minusMonths(1);
        }else if(StringUtils.equals("6m", searchDataType)){
            dateTime = dateTime.minusMonths(6);
        }
        //해당 값 이후의 아이템만 검색한다.
        return QItem.item.regTime.before(dateTime);
    }

    private BooleanExpression searchByLike(String searchBy, String searchQuery){
        if(StringUtils.equals("itemNm", searchBy)){
            return QItem.item.itemNm.like("%" + searchQuery + "%");
        }else if(StringUtils.equals("createdBy", searchBy)){
            return QItem.item.createdBy.like("%" + searchQuery + "%");
        }
        return null;
    }

    @Override
    public Page<Item> getAdminItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        QueryResults<Item> results = queryFactory.selectFrom(QItem.item)
                //, 로 나눠주면 해당 값들이 조건으로 들어가게 된다.
                .where(regDtsAfter(itemSearchDto.getSearchQuery()),
                        searchSellStatusEq(itemSearchDto.getSearchSellStatus()),
                        searchByLike(itemSearchDto.getSearchBy(),
                                itemSearchDto.getSearchQuery()))
                .orderBy(QItem.item.id.desc())
                // 시작 인덱스를 가져온다.
                .offset(pageable.getOffset())
                // 한번에 가져올 최대 개수를 지정한다.
                .limit(pageable.getPageSize())
                .fetchResults();
        List<Item> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<MainItemDto> getMainItemPage(ItemSearchDto itemSearchDto, Pageable pageable){
        QItem item = QItem.item;
        QItemImg itemImg = QItemImg.itemImg;

        QueryResults<MainItemDto> results = queryFactory
                .select(
                        new QMainItemDto(
                                item.id,
                                item.itemNm,
                                item.itemDetail,
                                itemImg.imgUrl,
                                item.price
                        ))
                                .from(itemImg)
                                .join(itemImg.item, item)
                                .where(itemImg.repimgYn.eq("Y"))
                                .where(itemNmLike(itemSearchDto.getSearchQuery()))
                                .orderBy(item.id.desc())
                                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        List<MainItemDto> content = results.getResults();
        long total = results.getTotal();
        return new PageImpl<>(content, pageable, total);
    }
}
