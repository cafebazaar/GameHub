
namespace Bazaar.Data
{
    public enum Status { Success = 0, Disconnect = 1, NeedsUpdate = 2, Failure = 3, Unknown = 4 }
    public class Result
    {
        public Status status;
        public string message;
        public string stackTrace;

        public Result(Status status, string message, string stackTrace = null)
        {
            this.status = status;
            this.message = message;
            this.stackTrace = stackTrace;
        }
    }

}