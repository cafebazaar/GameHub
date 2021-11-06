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
            using (var pluginClass = new AndroidJavaClass("com.farsitel.bazaar.game.GameHubBridge"))
            {
                if (pluginClass != null)
                {
                    gameHubClass = pluginClass.CallStatic<AndroidJavaObject>("getInstance");
                }
            }
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

        public async Task<Result> ShowTournamentLeaderboard(String tournamentId, Action<Result> onComplete = null)
        {
            var callback = new ConnectionCallbackProxy();
            gameHubClass.Call("showTournamentLeaderboard", UnityActivity.GetCurrentActivity(), tournamentId, callback);
            var result = await callback.WaitForResult();
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result> ShowLastTournamentLeaderboard(Action<Result> onComplete = null)
        { 
            return await ShowTournamentLeaderboard("-1", onComplete);
        }

    }
}