import com.vdurmont.emoji.EmojiParser;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Optional;

enum DialogState{
    LOCATION_UNDETECTED,
    CHOOSE_LOCATION,
    LOCATION_DETECTED
}

public class Bot extends TelegramLongPollingBot {

    private String current_location;
    private DialogState current_dialog;

    public Bot(DefaultBotOptions options){
        super(options);
        current_dialog = DialogState.LOCATION_UNDETECTED;
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
//            if (message.hasText()){
//                HtmlRequest obj = new HtmlRequest();
//                String url = "http://api.weatherapi.com/v1/current.json?key=0a642e967559402d83a192312231601&q=" + message.getText();
//                StringBuilder weather = obj.getRequestResult(url);
//
//                JSONObject weatherJson = new JSONObject(weather.toString());
//                String current_weather = (((weatherJson.getJSONObject("current")).getJSONObject("condition")).getString("text"));
//
//                try {
//                    execute(SendMessage.builder()
//                            .chatId(message.getChatId())
//                            .text(
//                                    EmojiParser.parseToUnicode(getWeatherIcon(weatherJson)) + "*" + (((weatherJson.getJSONObject("current")).getJSONObject("condition")).getString("text")) + "*" + "\n\n" +
//                                    EmojiParser.parseToUnicode(":thermometer: ") + "Current temperature: " + (weatherJson.getJSONObject("current")).get("temp_c") + " C" + "\n" +
//                                    EmojiParser.parseToUnicode(":thermometer: ") + "Feels like: " + (weatherJson.getJSONObject("current")).get("feelslike_c") + " C" + "\n\n" +
//                                    EmojiParser.parseToUnicode(":dash: ") + "Wind speed: " + (weatherJson.getJSONObject("current")).get("wind_kph") + " km/h" + "\n\n" +
//                                    EmojiParser.parseToUnicode(":droplet: ") + "Humidity: " + (weatherJson.getJSONObject("current")).get("humidity") + " %" + "\n" +
//                                    EmojiParser.parseToUnicode(":sweat_drops: ") + "Pressure: " + (weatherJson.getJSONObject("current")).get("pressure_mb") + " mm Hg" + "\n"
//                            )
//                            .parseMode("Markdown")
//                            .build());
//                }
//                catch (TelegramApiException e) {
//                    throw new RuntimeException(e);
//                }
//            }
            if(message.hasText() && message.hasEntities()){
                try {
                    handleMessage(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            else if(message.hasText() && (current_dialog == DialogState.CHOOSE_LOCATION)){
                try {
                    chooseLocation(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
            else{
                System.out.println("Error");
            }
        }
    }

    private void handleMessage(Message message) throws TelegramApiException {
        Optional<MessageEntity> commandEntity =  message.getEntities().stream().filter(e -> "bot_command".equals(e.getType())).findFirst();

        if(commandEntity.isPresent()){
            String command = message.getText().substring(commandEntity.get().getOffset(), commandEntity.get().getLength());
            switch (command){
                case "/start":
                    execute(SendMessage.builder()
                        .chatId(message.getChatId())
                        .text(EmojiParser.parseToUnicode(":wave: ") + "Hello, enter the locality where you want to track the weather")
                        .build());
                    current_dialog = DialogState.CHOOSE_LOCATION;
                    break;
                default:
                    System.out.println("Error");
                    break;
            }
        }
    }

    private void chooseLocation(Message message) throws TelegramApiException {
        HtmlRequest obj = new HtmlRequest();
        String url = "http://api.weatherapi.com/v1/search.json?key=0a642e967559402d83a192312231601&q=" + message.getText();
        StringBuilder location = obj.getRequestResult(url);

        location.deleteCharAt(location.indexOf("["));
        location.deleteCharAt(location.lastIndexOf("]"));

        if(location.length() > 1){
            JSONObject locationJson = new JSONObject(location.toString());
            current_location = locationJson.getString("name");
            current_dialog = DialogState.LOCATION_DETECTED;

            execute(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(
                            EmojiParser.parseToUnicode(":white_check_mark: ") + "Location was successfully found" + "\n\n" +
                            EmojiParser.parseToUnicode(":round_pushpin: ") + "Your location: " +
                            locationJson.getString("name") + ", " + locationJson.getString("region") + ", " + locationJson.getString("country")
                    )
                    .build());
        }
        else{
            execute(SendMessage.builder()
                .chatId(message.getChatId())
                .text(EmojiParser.parseToUnicode(":x: ") + "This location wasn't found. Please, try again")
                .build());
        }
    }

    private String getWeatherIcon(JSONObject obj){
        String str;

        switch((((obj.getJSONObject("current")).getJSONObject("condition")).getInt("code"))){
            case (1000):
                str = ":sunny: "; break;
            case (1003):
                str = ":partly_sunny: "; break;
            case (1006):
                str = ":white_sun_behind_cloud: "; break;
            case (1009):
                str = ":cloud: "; break;
            case (1030):
            case (1135):
                str = ":fog: "; break;
            case (1063):
            case (1180):
            case (1183):
            case (1186):
            case (1189):
            case (1192):
            case (1195):
            case (1240):
            case (1243):
            case (1246):
                str = ":cloud_rain: "; break;
            case (1066):
            case (1210):
            case (1213):
            case (1216):
            case (1219):
            case (1222):
            case (1225):
            case (1258):
                str = ":snowflake: "; break;
            case (1069):
            case (1072):
            case (1168):
            case (1171):
            case (1198):
            case (1201):
            case (1204):
            case (1207):
            case (1237):
            case (1249):
            case (1252):
            case (1255):
            case (1264):
                str = ":cloud_with_snow: "; break;
            case (1087):
                str = ":cloud_with_lightning: "; break;
            case (1114):
            case (1117):
                str = ":cloud_with_lightning: "; break;
            case (1273):
            case (1276):
            case (1279):
            case (1282):
                str = "::thunder_cloud_and_rain:: "; break;
            default: str = "1"; break;
        }

        return str;
    }
}
