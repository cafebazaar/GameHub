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

        public async Task<Result> Connect(Action<Result> onComplete = null)
        {
            var callback = new ConnectionCallbackProxy();
            gameHubClass.Call("connect", UnityActivity.GetCurrentActivity(), callback);
            var result = await callback.WaitForResult();
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result> StartTournamentMatch(string matchId, string metaData = "", Action<Result> onComplete = null)
        {
            var callback = new TournamentMatchCallbackProxy();
            gameHubClass.Call("startTournamentMatch", UnityActivity.GetCurrentActivity(), callback, matchId, metaData);
            var result = await callback.WaitForResult();
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

        public void ShowLastTournamentLeaderboard()
        {
            gameHubClass.Call("showLastTournamentLeaderboard", UnityActivity.GetCurrentActivity());
        }
    }
}