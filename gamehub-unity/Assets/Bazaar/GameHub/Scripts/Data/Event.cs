using UnityEngine;

namespace Bazaar.GameHub.Data
{
    public class Event
    {
        public string id;
        public string startTimestamp;
        public string endTimestamp;
        
        public Event(AndroidJavaObject entity)
        {
            this.id = entity.Get<string>("id");
            this.startTimestamp = entity.Get<string>("startTimestamp");
            this.endTimestamp = entity.Get<string>("endTimestamp");
        }

        override public string ToString() => $"id: {id}, startTimestamp: {startTimestamp}, endTimestamp: {endTimestamp}";
    }
}