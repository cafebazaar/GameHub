using System.Collections.Generic;

namespace Bazaar.GameHub.Data
{
    public class Result
    {

        public enum Status
        {
            Failure = -1,

            Success = 0,
            Disconnected = 1,
            LoginBazaar = 3,
            UpdateBazaar = 4,
            InstallBazaar = 5,
            Unknown = 6
        }

        public Status status;
        public string message;
        public string stackTrace;


        public Result(int status)
        {
            this.status = (Status)status;
        }

        public virtual string toString()
        {
            var text = $"Result [ status: {status}, ";
            if (message != null)
                text += $"message: {message}, ";
            if (stackTrace != null)
                text += $"stackTrace: {stackTrace} ";
            return text + "]";
        }
    }


    public class TournamentMatchResult : Result
    {
        public string sessionId;
        public string matchId;
        public string metadata;

        public TournamentMatchResult(int status) : base(status)
        {
        }

        override public string toString()
        {
            var text = $"Result [ status: {status}, ";
            if (message != null)
                text += $"message: {message}, ";
            if (stackTrace != null)
                text += $"stackTrace: {stackTrace} ";
            if (sessionId != null)
                text += $"sessionId: {sessionId}, ";
            if (matchId != null)
                text += $"matchId: {matchId}, ";
            if (metadata != null)
                text += $"metadata: {metadata}";
            return text + "]";
        }
    }

    public class TournamentsResult : Result
    {
        public List<Tournament> tournaments;


        public TournamentsResult(int status, List<Tournament> tournaments) : base(status)
        {
            this.status = (Status)status;
            this.tournaments = tournaments;
        }

        override public string toString()
        {
            var text = $"Result [ status: {status}, ";
            if (message != null)
                text += $"message: {message}, ";
            if (stackTrace != null)
                text += $"stackTrace: {stackTrace} ";
            if (tournaments != null)
                text += $"tournaments: {tournaments}";
            return text + "]";
        }
    }

}