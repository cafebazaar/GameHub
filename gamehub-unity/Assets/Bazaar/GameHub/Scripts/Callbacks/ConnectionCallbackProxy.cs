using Bazaar.Data;
using Bazaar.Callbacks;

namespace Bazaar.GameHub.Callbacks
{
    public class ConnectionCallbackProxy : CallbackProxy<bool>
    {
        public ConnectionCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IConnectionCallback") { }

        void onFinish(int status, string message, string stackTrace)
        {
            result = new Result<bool>((Status)status, message, stackTrace);
            result.data = result.status == Status.Success;
        }
    }
}