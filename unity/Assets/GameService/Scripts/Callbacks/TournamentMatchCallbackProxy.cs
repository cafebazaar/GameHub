using Bazaar.Data;

namespace Bazaar.Callbacks
{
    public class TournamentMatchCallbackProxy : BaseCallbackProxy
    {
        public TournamentMatchCallbackProxy() : base("com.farsitel.bazaar.games.callbacks.ITournamentMatchCallback") { }


        void onFinish(int status, string message, string stackTrace)
        {
            result = new Result((Status)status, message, stackTrace);
        }

        //    void onSuccess(AndroidJavaObject purchaseEntity)
        // {
        //     var list = new List<SKUDetails>();
        //     var size = purchaseEntity.Call<int>("size");
        //     for (int index = 0; index < size; index++)
        //     {
        //         list.Add(new SKUDetails(purchaseEntity.Call<AndroidJavaObject>("get", index)));
        //     }
        //     result = new SKUDetailsResult(Status.Success, list, "Fetch SKU details completed.");
        // }
    }
}