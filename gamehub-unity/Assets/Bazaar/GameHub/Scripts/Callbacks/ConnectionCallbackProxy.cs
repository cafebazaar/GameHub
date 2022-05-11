using Bazaar.Data;
using Bazaar.Callbacks;
using System.Threading.Tasks;

namespace Bazaar.GameHub.Callbacks
{
    public class ConnectionCallbackProxy : CallbackProxy<bool>
    {
        public ConnectionCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IConnectionCallback")
        {
            taskCompletionSource = new TaskCompletionSource<Result<bool>>();
        }

        void onFinish(int status, string message, string stackTrace)
        {
            var _status = (Status)status;
            taskCompletionSource.SetResult(new Result<bool>(_status, message, stackTrace) { data = _status == Status.Success });
        }
    }
}