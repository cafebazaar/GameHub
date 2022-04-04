using System.Collections.Generic;
using System.Threading.Tasks;
using Bazaar.Callbacks;
using Bazaar.Data;
using Bazaar.GameHub.Data;

namespace Bazaar.GameHub.Callbacks
{
    public class EvenListCallbackProxy : CallbackProxy<List<Event>>
    {
        public EvenListCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IGetEventsCallback")
        {
            taskCompletionSource = new TaskCompletionSource<Result<List<Event>>>();
        }

        void onFinish(int status, string message, string stackTrace, UnityEngine.AndroidJavaObject events)
        {
            var _status = (Status)status;
            var data = new List<Event>();
            if (_status == Status.Success)
            {
                var size = events.Call<int>("size");
                for (int index = 0; index < size; index++)
                {
                    data.Add(new Event(events.Call<UnityEngine.AndroidJavaObject>("get", index)));
                }
            }
            taskCompletionSource.SetResult(new Result<List<Event>>(_status, message, stackTrace) { data = data });
        }
    }
}