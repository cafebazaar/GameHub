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
        Log(result.ToString());
    }

    public async void StartTournamentMatch()
    {
        var result = await gameHub.StartTournamentMatch("OgMSbLOC", "metadata");
        if (result.status == Status.Success)
        {
            reservedMatch = result.data;
        }
        Log(result.ToString());
    }

    public async void EndTournamentMatch()
    {
        if (reservedMatch == null)
        {
            Log("Call `StartTournamentMatch` before!");
            return;
        }
        var result = await gameHub.EndTournamentMatch(reservedMatch.sessionId, 0.5f);
        Log(result.ToString());
        reservedMatch = null;
    }

    public async void ShowLastTournamentRanking()
    {
        var result = await gameHub.ShowLastTournamentRanking();
        if (result.status != Status.Success)
        {
            Log(result.ToString());
        }
    }

    public async void GetLastTournamentRanking()
    {
        var result = await gameHub.GetLastTournamentRanking();
        Log(result.ToString());
    }


    public async void EventDoneNotify()
    {
        var result = await gameHub.EventDoneNotify("1");
        Log(result.ToString());
    }

    public async void GetEvents()
    {
        var result = await gameHub.GetEvents();
        Log(result.ToString());
    }

    public void Log(string message)
    {
        consoleText.text += message + "\n";
    }
}
