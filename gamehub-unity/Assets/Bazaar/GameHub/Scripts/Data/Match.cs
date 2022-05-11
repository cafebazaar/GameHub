using UnityEngine;

namespace Bazaar.GameHub.Data
{
    public class Match
    {
        public string id;
        public string sessionId;
        public string metadata;
        
        public Match(AndroidJavaObject entity)
        {
            this.id = entity.Get<string>("id");
            this.sessionId = entity.Get<string>("sessionId");
            this.metadata = entity.Get<string>("metadata");
        }

        override public string ToString() => $"id: {id}, sessionId: {sessionId}, metadata: {metadata}";
    }
}