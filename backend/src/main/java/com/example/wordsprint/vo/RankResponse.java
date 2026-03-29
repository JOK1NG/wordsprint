package com.example.wordsprint.vo;

import lombok.Data;

import java.util.List;

@Data
public class RankResponse {

    private Integer myRank;

    private Integer myScore;

    private List<RankItemVO> list;
}
