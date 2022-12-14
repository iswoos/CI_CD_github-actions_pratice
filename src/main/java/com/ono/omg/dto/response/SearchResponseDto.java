package com.ono.omg.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SearchResponseDto {
    private String title;
    private Integer price;
    private Integer stock;
    private String category;
    private String delivery;
    private Long sellerId;
    private String isDeleted;
    private String imgUrl;

    @Builder
    @QueryProjection
    public SearchResponseDto(String title, Integer price, Integer stock, String category, String delivery, Long sellerId, String isDeleted, String imgUrl) {
        this.title = title;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.delivery = delivery;
        this.sellerId = sellerId;
        this.isDeleted = isDeleted;
        this.imgUrl = imgUrl;
    }
}
