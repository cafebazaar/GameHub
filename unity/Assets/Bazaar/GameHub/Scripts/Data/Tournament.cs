using System;
using UnityEngine;

namespace Bazaar.GameHub.Data
{
    public class Tournament
    {
        public string id, title;
        DateTime startAt, endAt;
        public Tournament(AndroidJavaObject entity)
        {
            this.id = entity.Get<string>("id");
            this.title = entity.Get<string>("title");
            this.startAt = DateTimeOffset.FromUnixTimeSeconds(entity.Get<long>("startAt")).DateTime;
            this.endAt = DateTimeOffset.FromUnixTimeSeconds(entity.Get<long>("endAt")).DateTime;
            Debug.Log("startAt " + entity.Get<long>("startAt"));
        }

        override public string ToString() => $"id: {id}, title: {title}, startAt: {startAt}, endAt: {endAt}";
    }
}