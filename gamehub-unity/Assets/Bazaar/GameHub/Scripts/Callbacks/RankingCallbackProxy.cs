using System.Collections.Generic;
using System.Threading.Tasks;
using Bazaar.Callbacks;
using Bazaar.Data;
using Bazaar.GameHub.Data;
using UnityEngine;

namespace Bazaar.GameHub.Callbacks
{
    public class RankingCallbackProxy : CallbackProxy<List<RankItem>>
    {
        public RankingCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IRankingCallback")
        {
            taskCompletionSource = new TaskCompletionSource<Result<List<RankItem>>>();
        }

        void onFinish(int status, string message, string stackTrace, AndroidJavaObject tournaments)
        {
            var _status = (Status)status;
            var data = new List<RankItem>();
            if (_status == Status.Success)
            {
                var size = tournaments.Call<int>("size");
                for (int index = 0; index < size; index++)
                {
                    data.Add(new RankItem(tournaments.Call<AndroidJavaObject>("get", index)));
                }
            }
            taskCompletionSource.SetResult(new Result<List<RankItem>>(_status, message, stackTrace) { data = data });
        }
    }
}