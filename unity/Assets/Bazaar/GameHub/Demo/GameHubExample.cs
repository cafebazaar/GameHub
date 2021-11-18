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
        gameHub = new GameHub();
        Log("Plugin Version: " + gameHub.version);
    }

    public async void Connect()
    {
        var result = await gameHub.Connect();
        Log(result.toString());
    }

    public async void GetTournaments()
    {
        var result = await gameHub.GetTournaments();
        Log(result.toString());
    }

    public async void StartTournamentMatch()
    {
        var result = await gameHub.StartTournamentMatch("OgMSbLOC", "metadata");
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

    public void ShowLastTournamentRanking()
    {
        _ = gameHub.ShowLastTournamentRanking();
    }

    public async void GetLastTournamentRanking()
    {
        var result = await gameHub.GetLastTournamentRanking();
        Log(result.toString());
    }

    public void Log(string message)
    {
        ConsoleText.text += message + "\n";
    }
}
