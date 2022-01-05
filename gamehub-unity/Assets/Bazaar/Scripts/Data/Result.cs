using System.Collections;

namespace Bazaar.Data
{
    public enum Status { Success, Start, Cancel, Disconnect, Failure, Unknown }
    public class Result<T>
    {
        public Status status;
        public string message;
        public string stackTrace;
        public T data;

        public Result(Status status, string message, string stackTrace = null)
        {
            this.status = status;
            this.message = message;
            this.stackTrace = stackTrace;
        }

        public static Result<T> GetDefault()
        {
            return new Result<T>(Status.Failure, "Bazaar plugins only support Android platform!");
        }

        public new string ToString()
        {
            var str = $"status: {status}";
            if (!string.IsNullOrEmpty(message))
            {
                str += $", message: {message}";
            }
            if (!string.IsNullOrEmpty(stackTrace))
            {
                str += $", stackTrace: {stackTrace}";
            }
            if (data == null)
            {
                return str;
            }
            
            str += $", data: [ ";
            if (data is IList)
            {
                var list = (IList)data;
                foreach (var item in list)
                {
                    str += $"{item.ToString()}\n";
                }
            }
            else
            {
                str += $"{data.ToString()}";
            }

            return str + " ]";
        }
    }
}
