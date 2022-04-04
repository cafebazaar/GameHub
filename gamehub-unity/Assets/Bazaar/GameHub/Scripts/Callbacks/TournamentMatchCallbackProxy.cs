using System.Threading.Tasks;
using Bazaar.Callbacks;
using Bazaar.Data;
using Bazaar.GameHub.Data;

namespace Bazaar.GameHub.Callbacks
{
    public class TournamentMatchCallbackProxy : CallbackProxy<Match>
    {
        public TournamentMatchCallbackProxy() : base("com.farsitel.bazaar.game.callbacks.ITournamentMatchCallback")
        {
            taskCompletionSource = new TaskCompletionSource<Result<Match>>();
        }

        void onFinish(int status, string message, string stackTrace, UnityEngine.AndroidJavaObject match)
        {
            var _status = (Status)status;
            Match data = null;
            if (_status == Status.Success)
            {
                data = new Match(match);
            }
            taskCompletionSource.SetResult(new Result<Match>(_status, message, stackTrace) { data = data });
        }
    }
}