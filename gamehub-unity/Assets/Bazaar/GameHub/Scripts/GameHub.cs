using System;
using System.Threading.Tasks;
using System.Collections.Generic;
using Bazaar.Data;
using Bazaar.GameHub.Data;
using Bazaar.GameHub.Callbacks;

namespace Bazaar.GameHub
{
    public class GameHub:Bridge
    {

        public GameHub():base("com.farsitel.bazaar.game.GameHub"){}

        public async Task<Result<bool>> Connect(bool showPrompts = true, Action<Result<bool>> onComplete = null)
        {
            var result = Result<bool>.GetDefault();
            if (isAndroid)
        {
            var callback = new ConnectionCallbackProxy();
                bridge.Call("connect", UnityActivity.GetCurrentActivity(), showPrompts, callback);
                result = await callback.WaitForResult();
            }
            else
            {
                await Task.Delay(1);
            }
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result<List<Tournament>>> GetTournaments(Action<Result<List<Tournament>>> onComplete = null)
        {
            var result = Result<List<Tournament>>.GetDefault();
            if (isAndroid)
        {
            var callback = new TournamentsCallbackProxy();
                bridge.Call("getTournaments", UnityActivity.GetCurrentActivity(), callback);
                result = await callback.WaitForResult();
            }
            else
            {
                await Task.Delay(1);
            }
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<TournamentMatchResult> StartTournamentMatch(string matchId, string metaData = "", Action<TournamentMatchResult> onComplete = null)
        {
            var callback = new TournamentMatchCallbackProxy();
            gameHubClass.Call("startTournamentMatch", UnityActivity.GetCurrentActivity(), callback, matchId, metaData);
            var data = await callback.WaitForResult();
            var result = (TournamentMatchResult)data;
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result> EndTournamentMatch(string sessionId, float score, Action<Result> onComplete = null)
        {
            var callback = new TournamentMatchCallbackProxy();
            gameHubClass.Call("endTournamentMatch", callback, sessionId, score);
            var result = await callback.WaitForResult();
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result> ShowTournamentRanking(String tournamentId, Action<Result> onComplete = null)
        {
            var callback = new ConnectionCallbackProxy();
            gameHubClass.Call("showTournamentRanking", UnityActivity.GetCurrentActivity(), tournamentId, callback);
            var result = await callback.WaitForResult();
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result> ShowLastTournamentRanking(Action<Result> onComplete = null)
        {
            return await ShowTournamentRanking("-1", onComplete);
        }

        public async Task<RankingResult> GetLastTournamentRanking(Action<RankingResult> onComplete = null)
        {
            var callback = new RankingCallbackProxy();
            gameHubClass.Call("getTournamentRanking", UnityActivity.GetCurrentActivity(), "-1", callback);
            var data = await callback.WaitForResult();
            var result = (RankingResult)data;
            onComplete?.Invoke(result);
            return result;
        }
    }
}