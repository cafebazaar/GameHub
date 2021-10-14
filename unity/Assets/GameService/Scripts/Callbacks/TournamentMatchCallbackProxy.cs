using Bazaar.Data;

namespace Bazaar.Callbacks
{
    public class TournamentMatchCallbackProxy : BaseCallbackProxy
    {
        public TournamentMatchCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback") { }


        void onFinish(int status, string message, string stackTrace)
        {
            result = new Result((Status)status, message, stackTrace);
        }
    }
}