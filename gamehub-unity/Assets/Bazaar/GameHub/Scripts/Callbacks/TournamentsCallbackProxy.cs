using System.Collections.Generic;
using Bazaar.Callbacks;
using Bazaar.Data;
using Bazaar.GameHub.Data;
using UnityEngine;

namespace Bazaar.GameHub.Callbacks
{
    public class TournamentsCallbackProxy : CallbackProxy<List<Tournament>>
    {
        public TournamentsCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.ITournamentsCallback") { }

        void onFinish(int status, string message, string stackTrace, AndroidJavaObject tournaments)
        {
            result = new Result<List<Tournament>>((Status)status, message, stackTrace);
            if (result.status == Status.Success)
            {
                var list = new List<Tournament>();
                var size = tournaments.Call<int>("size");
                for (int index = 0; index < size; index++)
                {
                    list.Add(new Tournament(tournaments.Call<AndroidJavaObject>("get", index)));
                }
                result.data = list;
            }
        }
    }
}