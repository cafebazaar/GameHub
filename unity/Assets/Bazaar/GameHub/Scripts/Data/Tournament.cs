using UnityEngine;

namespace Bazaar.GameHub.Data
{
    public class Tournament
    {
        public string id,title;
        long startAt, endAt;
        public Tournament(AndroidJavaObject entity)
        {
            this.id = entity.Get<string>("id");
            this.title = entity.Get<string>("title");
            this.startAt = entity.Get<long>("startAt");
            this.endAt = entity.Get<long>("endAt");
        }

        override public string ToString() => $"id: {id}, title: {title}, startAt: {startAt}, endAt: {endAt}";
    }
}