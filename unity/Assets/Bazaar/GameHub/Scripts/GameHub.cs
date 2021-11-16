using UnityEngine;
using System.Threading.Tasks;
using System;
using Bazaar.GameHub.Callbacks;
using Bazaar.GameHub.Data;

namespace Bazaar.GameHub
{
    public class GameHub
    {
        private AndroidJavaObject gameHubClass;

        public GameHub()
        {
            using (var pluginClass = new AndroidJavaClass("com.farsitel.bazaar.game.GameHub"))
            {
                if (pluginClass != null)
                {
                    gameHubClass = pluginClass.CallStatic<AndroidJavaObject>("getInstance");
                }
            }
        }
        public string version
        {
            get { return gameHubClass.Call<String>("getVersion"); }
        }

        public async Task<Result> Connect(bool showPrompts = true, Action<Result> onComplete = null)
        {
            var callback = new ConnectionCallbackProxy();
            gameHubClass.Call("connect", UnityActivity.GetCurrentActivity(), showPrompts, callback);
            var result = await callback.WaitForResult();
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<TournamentsResult> GetTournaments(Action<TournamentsResult> onComplete = null)
        {
            var callback = new TournamentsCallbackProxy();
            gameHubClass.Call("getTournaments", UnityActivity.GetCurrentActivity(), callback);
            var data = await callback.WaitForResult();
            var result = (TournamentsResult)data;
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