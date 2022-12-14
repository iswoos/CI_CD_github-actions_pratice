package com.ono.omg.service;

import com.ono.omg.domain.Account;
import com.ono.omg.domain.Order;
import com.ono.omg.domain.Product;
import com.ono.omg.dto.request.SearchRequestDto;
import com.ono.omg.dto.response.OrderResponseDto;
import com.ono.omg.dto.response.OrderResponseDto.MainPageOrdersResponseDto;
import com.ono.omg.dto.response.OrderResponseDto.createdOrdersResponseDto;
import com.ono.omg.dto.response.SearchResponseDto;
import com.ono.omg.exception.CustomCommonException;
import com.ono.omg.exception.ErrorCode;
import com.ono.omg.repository.account.AccountRepository;
import com.ono.omg.repository.order.OrderRepository;
import com.ono.omg.repository.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ono.omg.dto.response.OrderResponseDto.cancelOrderResponseDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 주문하기
     */
    @Transactional
    public createdOrdersResponseDto productOrder(Long productId, Account account) {
        Product findProduct = productRepository.findById(productId).orElseThrow(
                () -> new CustomCommonException(ErrorCode.NOT_FOUND_PRODUCT)
        );

        Account findAccount = accountRepository.findByUsername(account.getUsername()).orElseThrow(
                () -> new CustomCommonException(ErrorCode.USER_NOT_FOUND)
        );
        // 상품에 대한 주문은 여러개도 발생할 수 있다..?
        Order savedOrder = new Order(findAccount, findProduct, getTotalOrderPrice(findProduct.getPrice()));
        orderRepository.save(savedOrder);

        return new createdOrdersResponseDto(
                savedOrder.getId(), savedOrder.getTotalPrice(), account.getUsername(), findProduct
        );
    }

    /**
     * SJ: 지워도 괜찮은지요??
     */
    @Transactional
    public void testDecrease(Long productId, Account account) {
        Product findProduct = productRepository.findById(productId).orElseThrow(
                () -> new CustomCommonException(ErrorCode.NOT_FOUND_PRODUCT)
        );

        Account findAccount = accountRepository.findByUsername(account.getUsername()).orElseThrow(
                () -> new CustomCommonException(ErrorCode.USER_NOT_FOUND)
        );

        /**
         * 아래 문장을 Order 생성자에서 실행되도록 변경
         */
//        findProduct.decrease();

        // 상품에 대한 주문은 여러개도 발생할 수 있다..?
        Order savedOrder = new Order(findAccount, findProduct, getTotalOrderPrice(findProduct.getPrice()));
//        productRepository.saveAndFlush(findProduct);
//        orderRepository.saveAndFlush(savedOrder);
    }

    /**
     * 주문에 상품이 여러개일 경우를 대비해 별도의 메서드로 분리
     */
    private Integer getTotalOrderPrice(int price) {
        return price;
    }

    public List<MainPageOrdersResponseDto> findAllOrders(Pageable pageable, Account account) {
        List<MainPageOrdersResponseDto> findOrders = orderRepository.findOrdersParticularAccount(pageable, account.getId());

        return findOrders;
    }

    @Transactional
    public cancelOrderResponseDto cancel(Long orderId, Account account) {
        Order findOrder = orderRepository.findById(orderId).orElseThrow(
                () -> new CustomCommonException(ErrorCode.ORDER_NOT_FOUND)
        );

        Account findAccount = accountRepository.findById(account.getId()).orElseThrow(
                () -> new CustomCommonException(ErrorCode.USER_NOT_FOUND)
        );

        if(isSameAccount(findOrder, findAccount)) {
            throw new CustomCommonException(ErrorCode.INVALID_USER);
        }

        /**
         * SJ:
         * 우리 서비스만의 [주문 취소] 정책이 필요함
         * 예를 들어, 주문 취소를 했을 경우 재고 관리에 창고재고 라는 필드를 만들어서
         * 해당 값은 증가 시키고 사용자에게 보여지는 상품 재고는 유지를 한다던지와 같은
         *
         * 더불어 재고 관리 페이지에 [주문 취소]와 [판매 유무] 버튼이 필요함
         */

        /**
         * 주문 취소로 변경
         */
        findOrder.orderCancel();

        Long productId = findOrder.getProduct().getId();
        String productName = findOrder.getProduct().getTitle();
        String orderStatus = findOrder.getOrderType().getStatus();

        return new cancelOrderResponseDto(productId, productName, orderStatus);


    }

    /**
     * 상품 검색
     * */
    @Transactional
    public Page<SearchResponseDto> searchOrders(SearchRequestDto requestDto, Pageable pageable) {
        return productRepository.searchProduct(requestDto, pageable);
    }


//    /**
//     * 주문내역 조회
//     * */
//    public List<MyPageResponseDto> getOrderList(UserDetailsImpl userDetails) {
//        Long accountId = userDetails.getAccount().getId();
//        accountRepository.findById(accountId).orElseThrow(
//                () -> new CustomCommonException(ErrorCode.USER_NOT_FOUND)
//        );
//
//        List<Order> orders = orderRepository.findorderListByOrderId(accountId);
//        List<MyPageResponseDto> responseDtoList = orders.stream()
//                .map((o)-> new MyPageResponseDto(o.getProduct().getImgUrl(), o.getProduct().getTitle()))
//                .collect(Collectors.toList());
//
//        return responseDtoList;
//    }


    private boolean isSameAccount(Order findOrder, Account findAccount) {
        return !findOrder.getAccount().getId().equals(findAccount.getId());
    }

}