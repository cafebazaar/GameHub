using UnityEngine;
using UnityEngine.UI;
using Bazaar.GameHub;
using Bazaar.GameHub.Data;

public class GameHubExample : MonoBehaviour
{
    public Text ConsoleText;

    private GameHub gameHub;
    private string reservedSessionId;

    void Start()
    {
        Log("Plugin Version: " + PluginVersion.VersionString);
        gameHub = new GameHub();
    }

    public async void Connect()
    {
        var result = await gameHub.Connect();
        Log(result.toString());
    }

    public async void StartTournamentMatch()
    {
        var result = await gameHub.StartTournamentMatch("match_id", "metadata");
        if (result.status == Result.Status.Success)
        {
            reservedSessionId = result.sessionId;
        }
        Log(result.toString());
    }

    public async void EndTournamentMatch()
    {
        if (reservedSessionId == null)
        {
            Log("Call `StartTournamentMatch` before!");
            return;
        }
        var result = await gameHub.EndTournamentMatch(reservedSessionId, 0.4f);
        Log(result.toString());
    }

    public void ShowLastTournamentLeaderboard()
    {
        gameHub.ShowLastTournamentLeaderboard();
    }

    public void Log(string message)
    {
        ConsoleText.text += message + "\n";
    }

    // void OnApplicationQuit()
    // {
    //     GameHub.Disconnect();
    // }
}
