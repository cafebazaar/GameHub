using Bazaar.Callbacks;
using Bazaar.Data;
using Bazaar.GameHub.Data;

namespace Bazaar.GameHub.Callbacks
{
    public class TournamentMatchCallbackProxy : CallbackProxy<Match>
    {
        public TournamentMatchCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback") { }

        void onFinish(int status, string message, string stackTrace, UnityEngine.AndroidJavaObject match)
        {
            result = new Result<Match>((Status)status, message, stackTrace);
            if (result.status == Status.Success)
            {
                result.data = new Match(match);
            }
        }
    }
}