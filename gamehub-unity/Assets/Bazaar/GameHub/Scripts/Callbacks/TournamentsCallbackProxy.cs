using System.Collections.Generic;
using System.Threading.Tasks;
using Bazaar.Callbacks;
using Bazaar.Data;
using Bazaar.GameHub.Data;
using UnityEngine;

namespace Bazaar.GameHub.Callbacks
{
    public class TournamentsCallbackProxy : CallbackProxy<List<Tournament>>
    {
        public TournamentsCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.ITournamentsCallback")
        {
            taskCompletionSource = new TaskCompletionSource<Result<List<Tournament>>>();
        }

        void onFinish(int status, string message, string stackTrace, AndroidJavaObject tournaments)
        {
            var _status = (Status)status;
            var data = new List<Tournament>();
            if (_status == Status.Success)
            {
                var list = new List<Tournament>();
                var size = tournaments.Call<int>("size");
                for (int index = 0; index < size; index++)
                {
                    list.Add(new Tournament(tournaments.Call<AndroidJavaObject>("get", index)));
                }
            }
            taskCompletionSource.SetResult(new Result<List<Tournament>>(_status, message, stackTrace) { data = data });
        }
    }
}