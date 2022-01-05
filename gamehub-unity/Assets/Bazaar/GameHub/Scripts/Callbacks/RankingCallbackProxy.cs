using System.Collections.Generic;
using Bazaar.Callbacks;
using Bazaar.Data;
using Bazaar.GameHub.Data;
using UnityEngine;

namespace Bazaar.GameHub.Callbacks
{
    public class RankingCallbackProxy : CallbackProxy<List<RankItem>>
    {
        public RankingCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IRankingCallback") { }

        void onFinish(int status, string message, string stackTrace, AndroidJavaObject tournaments)
        {
            result = new Result<List<RankItem>>((Status)status, message, stackTrace);
            if (result.status == Status.Success)
            {
                var list = new List<RankItem>();
                var size = tournaments.Call<int>("size");
                for (int index = 0; index < size; index++)
                {
                    list.Add(new RankItem(tournaments.Call<AndroidJavaObject>("get", index)));
                }
                result.data = list;
            }
        }
    }
}