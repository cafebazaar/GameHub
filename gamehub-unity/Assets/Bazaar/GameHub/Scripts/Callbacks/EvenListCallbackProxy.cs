using System.Collections.Generic;
using Bazaar.Callbacks;
using Bazaar.Data;
using Bazaar.GameHub.Data;

namespace Bazaar.GameHub.Callbacks
{
    public class EvenListCallbackProxy : CallbackProxy<List<Event>>
    {
        public EvenListCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IGetEventsCallback") { }

        void onFinish(int status, string message, string stackTrace, UnityEngine.AndroidJavaObject events)
        {
            result = new Result<List<Event>>((Status)status, message, stackTrace);
            if (result.status == Status.Success)
            {
                var list = new List<Event>();
                var size = events.Call<int>("size");
                for (int index = 0; index < size; index++)
                {
                    list.Add(new Event(events.Call<UnityEngine.AndroidJavaObject>("get", index)));
                }
                result.data = list;
            }
        }
    }
}