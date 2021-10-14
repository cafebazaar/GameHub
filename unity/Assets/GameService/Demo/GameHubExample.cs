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
        Log(result.toString());
    }

    public async void StartTournamentMatch()
    {
        var result = await gameHub.StartTournamentMatch("match_id", "metadata");
        Log(result.toString());
    }

    public async void EndTournamentMatch()
    {
        var result = await gameHub.EndTournamentMatch("my_session_id", 0.4f);
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
