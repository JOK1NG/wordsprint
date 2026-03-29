package com.example.wordsprint.vo;

import lombok.Data;

import java.util.List;

@Data
public class FamiliarityDistributionVO {

    private Integer totalCards;

    private List<FamiliarityDistributionItemVO> items;
}
