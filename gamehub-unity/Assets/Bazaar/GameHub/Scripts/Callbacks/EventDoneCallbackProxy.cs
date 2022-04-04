using System.Threading.Tasks;
using Bazaar.Callbacks;
using Bazaar.Data;

namespace Bazaar.GameHub.Callbacks
{
    public class EventDoneCallbackProxy : CallbackProxy<string>
    {
        public EventDoneCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IEventDoneCallback")
        {
            taskCompletionSource = new TaskCompletionSource<Result<string>>();
        }

        void onFinish(int status, string message, string stackTrace, string effectiveDoneTime)
        {
            taskCompletionSource.SetResult(new Result<string>((Status)status, message, stackTrace) { data = effectiveDoneTime });}
        }
    }
}