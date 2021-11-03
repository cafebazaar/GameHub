using System.Collections.Generic;
using Bazaar.GameHub.Data;
using UnityEngine;

namespace Bazaar.GameHub.Callbacks
{
    public class TournamentsCallbackProxy : BaseCallbackProxy
    {
        public TournamentsCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.ITournamentsCallback") { }

        void onFinish(int status, string message, string stackTrace, AndroidJavaObject tournaments)
        {
            if (status != (int)Result.Status.Success)
            {
                result = new TournamentsResult(status, null) { message = message, stackTrace = stackTrace };
                return;
            }
            var list = new List<Tournament>();
            var size = tournaments.Call<int>("size");
            for (int index = 0; index < size; index++)
            {
                list.Add(new Tournament(tournaments.Call<AndroidJavaObject>("get", index)));
            }
            result = new TournamentsResult(status, list) { message = "Fetch Tournaments completed." };
        }
    }
}