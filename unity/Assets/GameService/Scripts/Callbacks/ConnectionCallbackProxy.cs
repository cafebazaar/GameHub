using Bazaar.Data;

namespace Bazaar.Callbacks
{
    public class ConnectionCallbackProxy : BaseCallbackProxy
    {
        public ConnectionCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.IConnectionCallback") { }

        void onFinish(int status, string message, string stackTrace)
        {
            result = new Result((Status)status, message, stackTrace);
        }
    }
}