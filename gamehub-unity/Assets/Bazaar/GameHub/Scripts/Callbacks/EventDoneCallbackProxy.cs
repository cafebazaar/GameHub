using Bazaar.Callbacks;
using Bazaar.Data;

namespace Bazaar.GameHub.Callbacks
{
    public class EventDoneCallbackProxy : CallbackProxy<string>
    {
        public EventDoneCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IEventDoneCallback") { }

        void onFinish(int status, string message, string stackTrace, string effectiveDoneTime)
        {
            result = new Result<string>((Status)status, message, stackTrace);
            if (result.status == Status.Success)
            {
                result.data = effectiveDoneTime;
            }
        }
    }
}