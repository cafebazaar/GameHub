using Bazaar.Data;

namespace Bazaar.Callbacks
{
    public class TournamentMatchCallbackProxy : BaseCallbackProxy
    {
        public TournamentMatchCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback") { }


        void onFinish(int status, string arg1, string arg2, string arg3)
        {
            if (status == 0)
            {
                result = new Result(status) { sessionId = arg1, matchId = arg2, metadata = arg3 };
            }
            else
            {
                result = new Result(status) { message = arg1, stackTrace = arg2 };
            }
        }
    }
}