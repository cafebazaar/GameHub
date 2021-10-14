using UnityEngine;
using UnityEngine.UI;
using Bazaar;

public class GameHubExample : MonoBehaviour
{
    public Text ConsoleText;

    private GameHub gameHub;

    void Start()
    {
        Log("Plugin Version: " + PluginVersion.VersionString);
        gameHub = new GameHub();
    }

    public async void Connect()
    {
        var result = await gameHub.Connect();
        Log($"{result.message}, {result.stackTrace}");
    }

    public async void StartTournamentMatch()
    {
        var result = await gameHub.StartTournamentMatch("match_id", "metadata");
        Log($"{result.message}, {result.stackTrace}");
    }

    public async void EndTournamentMatch()
    {
        var result = await gameHub.EndTournamentMatch("session_id", 0.4f);
        Log($"{result.message}, {result.stackTrace}");
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
    //     gameHub.Disconnect();
    // }
}
