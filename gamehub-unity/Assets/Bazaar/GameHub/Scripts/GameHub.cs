using System;
using System.Threading.Tasks;
using System.Collections.Generic;
using Bazaar.Data;
using Bazaar.GameHub.Data;
using Bazaar.GameHub.Callbacks;

namespace Bazaar.GameHub
{
    public class GameHub : Bridge
    {

        public GameHub() : base("com.farsitel.bazaar.game.GameHub") { }

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

        public async Task<Result<Match>> StartTournamentMatch(string matchId, string metaData = "", Action<Result<Match>> onComplete = null)
        {
            var result = Result<Match>.GetDefault();
            if (isAndroid)
            {
                var callback = new TournamentMatchCallbackProxy();
                bridge.Call("startTournamentMatch", UnityActivity.GetCurrentActivity(), callback, matchId, metaData);
                result = await callback.WaitForResult();
            }
            else
            {
                await Task.Delay(1);
            }
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result<Match>> EndTournamentMatch(string sessionId, float score, Action<Result<Match>> onComplete = null)
        {
            var result = Result<Match>.GetDefault();
            if (isAndroid)
            {
                var callback = new TournamentMatchCallbackProxy();
                bridge.Call("endTournamentMatch", callback, sessionId, score);
                result = await callback.WaitForResult();
            }
            else
            {
                await Task.Delay(1);
            }
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result<bool>> ShowTournamentRanking(String tournamentId, Action<Result<bool>> onComplete = null)
        {
            var result = Result<bool>.GetDefault();
            if (isAndroid)
            {
                var callback = new ConnectionCallbackProxy();
                bridge.Call("showTournamentRanking", UnityActivity.GetCurrentActivity(), tournamentId, callback);
                result = await callback.WaitForResult();
            }
            else
            {
                await Task.Delay(1);
            }
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result<bool>> ShowLastTournamentRanking(Action<Result<bool>> onComplete = null)
        {
            return await ShowTournamentRanking("-1", onComplete);
        }

        public async Task<Result<List<RankItem>>> GetLastTournamentRanking(Action<Result<List<RankItem>>> onComplete = null)
        {
            var result = Result<List<RankItem>>.GetDefault();
            if (isAndroid)
            {
                var callback = new RankingCallbackProxy();
                bridge.Call("getTournamentRanking", UnityActivity.GetCurrentActivity(), "-1", callback);
                result = await callback.WaitForResult();
            }
            else
            {
                await Task.Delay(1);
            }
            onComplete?.Invoke(result);
            return result;
        }

        public async Task<Result<string>> EventDoneNotify(string eventId, Action<Result<string>> onComplete = null)
        {
            var result = Result<string>.GetDefault();
            if (isAndroid)
            {
                var callback = new EventDoneCallbackProxy();
                bridge.Call("eventDoneNotify", UnityActivity.GetCurrentActivity(), eventId, callback);
                result = await callback.WaitForResult();
            }
            else
            {
                await Task.Delay(1);
            }
            onComplete?.Invoke(result);
            return result;
        }

        
        public async Task<Result<List<Event>>> GetEvents(Action<Result<List<Event>>> onComplete = null)
        {
            var result = Result<List<Event>>.GetDefault();
            if (isAndroid)
            {
                var callback = new EvenListCallbackProxy();
                bridge.Call("getEvents", UnityActivity.GetCurrentActivity(), callback);
                result = await callback.WaitForResult();
            }
            else
            {
                await Task.Delay(1);
            }
            onComplete?.Invoke(result);
            return result;
        }
    }
}