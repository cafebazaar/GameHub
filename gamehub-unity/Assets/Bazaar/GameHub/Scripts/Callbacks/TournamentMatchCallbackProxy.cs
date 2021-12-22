using Bazaar.GameHub.Data;

namespace Bazaar.GameHub.Callbacks
{
    public class TournamentMatchCallbackProxy : BaseCallbackProxy
    {
        public TournamentMatchCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback") { }


        void onFinish(int status, string arg1, string arg2, string arg3)
        {
            if (status != (int)Result.Status.Success)
            {
                result = new TournamentMatchResult(status) { message = arg1, stackTrace = arg2 };
                return;
            }
            result = new TournamentMatchResult(status) { sessionId = arg1, matchId = arg2, metadata = arg3 };
        }
    }
}