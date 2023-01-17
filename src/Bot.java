import org.json.JSONObject;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Bot extends TelegramLongPollingBot {

    public Bot(DefaultBotOptions options){
        super(options);
    }

    @Override
    public String getBotUsername() {
        return "@weather_pred_bot";
    }

    @Override
    public String getBotToken() {
        return "5881321838:AAGkSY4kr-6vx7ZAPa2BZqfWyAIWXTxYLoQ";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()){
            Message message = update.getMessage();
            if (message.hasText()){
                HtmlRequest obj = new HtmlRequest();
                String url = "http://api.weatherapi.com/v1/current.json?key=0a642e967559402d83a192312231601&q=" + message.getText();
                StringBuilder weather = obj.getRequestResult(url);

                JSONObject weatherJson = new JSONObject(weather.toString());
                String current_weather = (((weatherJson.getJSONObject("current")).getJSONObject("condition")).getString("text"));

                try {
                    execute(SendMessage.builder()
                            .chatId(message.getChatId())
                            .text(
                                    (((weatherJson.getJSONObject("current")).getJSONObject("condition")).getString("text")) + "\n\n" +
                                    "\uD83C\uDF21 Current temperature: " + (weatherJson.getJSONObject("current")).get("temp_c") + " C" + "\n" +
                                    "\uD83C\uDF21 Feels like: " + (weatherJson.getJSONObject("current")).get("feelslike_c") + " C" + "\n\n" +
                                    "\uD83D\uDCA8 Wind speed: " + (weatherJson.getJSONObject("current")).get("wind_kph") + " km/h" + "\n" +
                                    "\uD83D\uDCA6 Humidity: " + (weatherJson.getJSONObject("current")).get("humidity") + " %" + "\n" +
                                    "Pressure: " + (weatherJson.getJSONObject("current")).get("pressure_mb") + " mm Hg" + "\n"
                            )
                            .build());
                }
                catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
