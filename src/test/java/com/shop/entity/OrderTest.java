package com.shop.entity;

import com.shop.constant.ItemSellStatus;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderItemRepository;
import com.shop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class OrderTest {

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @PersistenceContext
    EntityManager em;

    public Item createItem(){
        Item item = Item.builder()
                .itemNm("테스트 상품")
                .price(10000)
                .itemDetail("상세설명")
                .itemSellStatus(ItemSellStatus.SELL)
                .stockNumber(100)
                .build();
        return item;
    }

    @Test
    @DisplayName("영속성 전이 테스트")
    public void cascadeTest(){
        Order order = new Order();

        for(int i = 0 ; i  < 3 ; i++){
            Item item = this.createItem();
            itemRepository.save(item);
            OrderItem orderItem = OrderItem.builder()
                    .item(item)
                    .count(10)
                    .orderPrice(1000)
                    .order(order)
                    .build();
            order.getOrderItems().add(orderItem);
        }

        orderRepository.saveAndFlush(order);
        em.clear();
        // 주문 데이터가 데이터베이스에 등록되는 것을 확인 할 수 있다.
        // 그 후 영속성이 전이되면서 order에 담아두었던 orderItem에 대한 쿼리문이 자동적으로 생성되는 것을 확인할 수 있다.

        Order savedOrder = orderRepository.findById(order.getId())
                .orElseThrow(EntityNotFoundException::new);
        assertEquals(3, savedOrder.getOrderItems().size());

    }

    public Order createOrder(){
        Order order = new Order();

        for(int i = 0; i < 3; i++){
            Item item = createItem();
            itemRepository.save(item);
            OrderItem orderItem = OrderItem.builder()
                    .item(item)
                    .count(10)
                    .orderPrice(1000)
                    .order(order)
                    .build();
            order.getOrderItems().add(orderItem);
        }

        Member member = new Member();
        memberRepository.save(member);

        order.setMember(member);
        orderRepository.save(order);

        return order;
    }

    @Test
    @DisplayName("고아객체 제거 테스트")
    public void orphanRemovalTest(){
        Order order = this.createOrder();
        order.getOrderItems().remove(0);
        em.flush(); // order_item 삭제 쿼리가 생성된다. 이는 order 엔티티(부모 엔티티)가 삭제될때 종속되는 orderItem들이 삭제되는 영속성 전이 때문이다.
    }

    @Test
    @DisplayName("지연 로딩 테스트")
    public void lazyLoadingTest(){

        Order order = this.createOrder();
        Long orderItemId = order.getOrderItems().get(0).getId();
        em.flush();
        em.clear();
        // TODO: 2022-01-13  order Item에 지연로딩 적용 전
        //해당 테스트 실행 시 매핑의 기본 전략인 즉시 로딩을 통해 엔티티를 가져옵니다. 그렇게 되어 order item에 대한 검색을 진행할 때
        // orderItem 뿐만 아니라 item, order, member가 모두 검색된다.
        // 즉, 엔티티하나를 검색하자 연관된 모든 엔티티에 대한 검색이 진행되는 것을 볼 수 있다.
        // 이 때문에 불필요한 쿼리까지 실행되는 것을 볼 수 있다.
        // 따라서 지연 로딩 방식에 대한 필요가 생기는 것이다.
        // TODO: 2022-01-13 order Item에 지연로딩 적용 후
        // 지연 로딩 적용후에는 orderItem만 적용되는 것을 볼 수 있다.
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(EntityNotFoundException::new);
        System.out.println("Order class :" + orderItem.getOrder().getClass());
        System.out.println("============================");
        orderItem.getOrder().getOrderDate();
        System.out.println("============================");
    }



}