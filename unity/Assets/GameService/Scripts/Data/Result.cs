namespace Bazaar.Data
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
    public class Result
    {
        public Status status;
        public string message;
        public string stackTrace;
        public string sessionId;
        public string matchId;
        public string metadata;

        public Result(int status)
        {
            this.status = (Status)status; 
        }

        public string toString()
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
            return text;
        }
    }
}