using UnityEngine;

namespace Bazaar.GameHub.Data
{
    public class RankItem
    {
        public string nickname;
        public string score;
        public string award;
        public bool hasFollowingEllipsis;
        public bool isCurrentUser;
        public bool isWinner;

        public RankItem(AndroidJavaObject entity)
        {
            this.nickname = entity.Get<string>("nickname");
            this.score = entity.Get<string>("score");
            this.award = entity.Get<string>("award");
            this.hasFollowingEllipsis = entity.Get<bool>("hasFollowingEllipsis");
            this.isCurrentUser = entity.Get<bool>("isCurrentUser");
            this.isWinner = entity.Get<bool>("isWinner");
        }

        override public string ToString() => $"nickname: {nickname}, score: {score}";
    }
}