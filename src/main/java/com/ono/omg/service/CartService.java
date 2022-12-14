package com.ono.omg.service;

import com.ono.omg.domain.Account;
import com.ono.omg.domain.Cart;
import com.ono.omg.domain.Product;
import com.ono.omg.dto.response.CartResponseDto;
import com.ono.omg.exception.CustomCommonException;
import com.ono.omg.exception.ErrorCode;
import com.ono.omg.repository.account.AccountRepository;
import com.ono.omg.repository.cart.CartRepository;
import com.ono.omg.repository.product.ProductRepository;
import com.ono.omg.security.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;
    /**
     * sy
     * 장바구니 조회 -> querydsl
     * */

    /**
     * 상품 장바구니 담기
     */
    @Transactional
    public Long inputProduct(Long productId, UserDetailsImpl userDetails) {
        Account account = accountRepository.findByUsername(userDetails.getAccount().getUsername()).orElseThrow(
                () -> new CustomCommonException(ErrorCode.USER_NOT_FOUND)
        );

        Optional<Cart> found = cartRepository.findByProductIdAndAccountId(productId, account.getId());

//        log.info("found -----------> " + found.toString());

        if (found.isEmpty()) {
            Cart cart = new Cart(account, productId);
            cartRepository.save(cart);
        } else {
            throw new CustomCommonException(ErrorCode.DUPLICATE_PRODUCT);
        }
        return account.getId();
    }

    /**
     * 장바구니 조회
     * */
    public List<CartResponseDto> getCartList(UserDetailsImpl userDetails) {
        Long accountId = userDetails.getAccount().getId();

        List<Long> cartProductIdList = cartRepository.findProductIdByAccountId(accountId);
        List<CartResponseDto>cartResponseList = new ArrayList<>();

        //가져온 상품Id로 해당 상품에 대한 정보 가져오기
        for(Long productId : cartProductIdList){
            Product p = productRepository.findById(productId).orElseThrow(
                    () -> new CustomCommonException(ErrorCode.NOT_FOUND_PRODUCT)
            );

            CartResponseDto dto = new CartResponseDto(p.getId(), p.getTitle(), p.getPrice(), p.getCategory(),
                                                                    p.getDelivery(), p.getStock(), p.getSellerId(), p.getImgUrl());
            cartResponseList.add(dto);
        }
        return cartResponseList;
    }
}

