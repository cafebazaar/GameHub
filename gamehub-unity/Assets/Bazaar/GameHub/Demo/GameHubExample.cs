using UnityEngine;
using UnityEngine.UI;
using Bazaar.Data;
using Bazaar.GameHub;
using Bazaar.GameHub.Data;

public class GameHubExample : MonoBehaviour
{
    [SerializeField] private Text consoleText;
    [SerializeField] private GameObject menu;

    private GameHub gameHub;
    private Match reservedMatch;

    void Start()
    {
        gameHub = new GameHub();
        Log("Plugin Version: " + gameHub.version);
    }

    public async void Connect()
    {
        var result = await gameHub.Connect();
        Log(result.ToString());
        if (result.status == Status.Success)
        {
            menu.SetActive(true);
        }
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

    public async void ShowLastTournamentRanking()
    {
        var result = await gameHub.ShowLastTournamentRanking();
        if (result.status != Result.Status.Success)
        {
            Log(result.toString());
        }
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
