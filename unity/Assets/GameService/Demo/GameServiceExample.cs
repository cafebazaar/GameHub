using UnityEngine;
using UnityEngine.UI;
using Bazaar;

public class GameServiceExample : MonoBehaviour
{
    public Text ConsoleText;

    private GameService gameService;

    void Start()
    {
        Log("Plugin Version: " + PluginVersion.VersionString);
        gameService = new GameService();
    }

    public async void Connect()
    {
        var result = await gameService.Connect();
        Log($"{result.message}, {result.stackTrace}");
    }

    public async void StartTournamentMatch()
    {
        var result = await gameService.StartTournamentMatch("match_id", "metadata");
        Log($"{result.message}, {result.stackTrace}");
    }

    public async void EndTournamentMatch()
    {
        var result = await gameService.EndTournamentMatch("session_id", 0.4f);
        Log($"{result.message}, {result.stackTrace}");
    }

    public void ShowLastTournamentLeaderboard()
    {
        gameService.ShowLastTournamentLeaderboard();
    }

    public void Log(string message)
    {
        ConsoleText.text += message + "\n";
    }

    // void OnApplicationQuit()
    // {
    //     gameService.Disconnect();
    // }
}
