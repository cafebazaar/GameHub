using System.Threading.Tasks;
using Bazaar.GameHub.Data;
using UnityEngine;

namespace Bazaar.GameHub.Callbacks
{
    public class BaseCallbackProxy : AndroidJavaProxy
    {
        protected Result result;
        public BaseCallbackProxy(string address) : base(address) { }

        public async Task<Result> WaitForResult()
        {
            while (result == null)
                await Task.Delay(100);
            return result;
        }
    }
}