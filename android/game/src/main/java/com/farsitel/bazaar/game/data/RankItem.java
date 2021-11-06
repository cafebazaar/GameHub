package com.farsitel.bazaar.game.data;

public class RankItem {
    String nickname, score, award;
    boolean hasFollowingEllipsis, isCurrentUser, isWinner;

    public RankItem(String nickname, String score, String award, boolean hasFollowingEllipsis, boolean isCurrentUser, boolean isWinner){
        this.nickname = nickname;
        this.score = score;
        this.award = award;
        this.hasFollowingEllipsis = hasFollowingEllipsis;
        this.isCurrentUser = isCurrentUser;
        this.isWinner = isWinner;
    }
}
