using System.Collections.Generic;
using Bazaar.GameHub.Data;
using UnityEngine;

namespace Bazaar.GameHub.Callbacks
{
    public class RankingCallbackProxy : BaseCallbackProxy
    {
        public RankingCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IRankingCallback") { }

        void onFinish(int status, string message, string stackTrace, AndroidJavaObject tournaments)
        {
            if (status != (int)Result.Status.Success)
            {
                result = new RankingResult(status, null) { message = message, stackTrace = stackTrace };
                return;
            }
            var list = new List<RankItem>();
            var size = tournaments.Call<int>("size");
            for (int index = 0; index < size; index++)
            {
                list.Add(new RankItem(tournaments.Call<AndroidJavaObject>("get", index)));
            }
            result = new RankingResult(status, list) { message = "Fetch Ranking completed.", stackTrace = stackTrace };
        }
    }
}