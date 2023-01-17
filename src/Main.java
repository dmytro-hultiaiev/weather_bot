import org.json.JSONObject;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) throws TelegramApiException {
        Bot bot = new Bot(new DefaultBotOptions());
        TelegramBotsApi bot_api = new TelegramBotsApi(DefaultBotSession.class);
        bot_api.registerBot(bot);
    }
}
