import com.vdurmont.emoji.EmojiParser;
import org.json.JSONObject;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

enum DialogState{
    LOCATION_UNDETECTED,
    CHOOSE_LOCATION,
    LOCATION_DETECTED
}

public class Bot extends TelegramLongPollingBot {

    private String current_location[] = new String[3];
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
        if(update.hasCallbackQuery()){
            try {
                handleCallbackQuery(update.getCallbackQuery());
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
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

    private void handleCallbackQuery(CallbackQuery query) throws TelegramApiException {
        Message message = query.getMessage();
        String param = query.getData();

        switch (param) {
            case "CURRENT_WEATHER_BUTTON":
                currentWeather(message);
                break;
            case "BACK_TO_MENU":
                mainMenu(message);
                break;
            case "DAY_FORECAST_BUTTON":
                dayForecast(message);
                break;
            case "HOUR_FORECAST_BUTTON":
                hourForecast(message);
                break;
            case "DAY_3_FORECAST_BUTTON":
                threeDayForecast(message);
                break;
            default:
                System.out.println("Can't detect inline button");
                break;
        }
    }


    private void chooseLocation(Message message) throws TelegramApiException {
        HtmlRequest obj = new HtmlRequest();
        String location_name = (message.getText()).replace(" ", "%20");
        String url = "http://api.weatherapi.com/v1/search.json?key=0a642e967559402d83a192312231601&q=" + location_name;
        StringBuilder location = obj.getRequestResult(url);

        location.deleteCharAt(location.indexOf("["));
        location.deleteCharAt(location.lastIndexOf("]"));

        if(location.length() > 1){
            JSONObject locationJson = new JSONObject(location.toString());
            current_location[0] = locationJson.getString("name");
            current_location[1] = locationJson.getString("region");
            current_location[2] = locationJson.getString("country");
            current_dialog = DialogState.LOCATION_DETECTED;

            execute(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(
                            EmojiParser.parseToUnicode(":white_check_mark: ") + "Location was successfully found"
                    )
                    .build());

            mainMenu(message);
        }
        else{
            execute(SendMessage.builder()
                .chatId(message.getChatId())
                .text(EmojiParser.parseToUnicode(":x: ") + "This location wasn't found. Please, try again")
                .build());
        }
    }

    private void mainMenu(Message message) throws TelegramApiException {

        List<List<InlineKeyboardButton>> rows_inline = new ArrayList<>();
        List<InlineKeyboardButton> row_inline = new ArrayList<>();
        List<InlineKeyboardButton> row_second_inline = new ArrayList<>();

        var curr_weather_but = new InlineKeyboardButton();
        curr_weather_but.setText("Current weather");
        curr_weather_but.setCallbackData("CURRENT_WEATHER_BUTTON");

        var day_forecast_but = new InlineKeyboardButton();
        day_forecast_but.setText("Daily forecast");
        day_forecast_but.setCallbackData("DAY_FORECAST_BUTTON");

        var hour_forecast_but = new InlineKeyboardButton();
        hour_forecast_but.setText("Hourly forecast");
        hour_forecast_but.setCallbackData("HOUR_FORECAST_BUTTON");

        var day_3_weather_forecast_but = new InlineKeyboardButton();
        day_3_weather_forecast_but.setText("3 day weather forecast");
        day_3_weather_forecast_but.setCallbackData("DAY_3_FORECAST_BUTTON");

        row_inline.add(curr_weather_but);
        row_inline.add(day_forecast_but);
        row_second_inline.add(hour_forecast_but);
        row_second_inline.add(day_3_weather_forecast_but);

        rows_inline.add(row_inline);
        rows_inline.add(row_second_inline);

        execute(SendMessage.builder()
                .chatId(message.getChatId())
                .text(
                        "*Main menu* " + "\n\n" +
                        EmojiParser.parseToUnicode(":round_pushpin: ") + "Your location: " +
                        current_location[0] + ", " + current_location[1] + ", " + current_location[2] + "\n\n" +
                        EmojiParser.parseToUnicode(":point_right: ") + "Please select below what information you would like to receive"
                )
                .parseMode("Markdown")
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(rows_inline).build())
                .build());

    }

    private void currentWeather(Message message){
        HtmlRequest obj = new HtmlRequest();
        String url = "http://api.weatherapi.com/v1/current.json?key=0a642e967559402d83a192312231601&q=" + current_location[0];
        StringBuilder weather = obj.getRequestResult(url);

        JSONObject weatherJson = new JSONObject(weather.toString());

        List<List<InlineKeyboardButton>> back_to_menu = new ArrayList<>();
        List<InlineKeyboardButton> back_to_menu_row = new ArrayList<>();

        var back_to_menu_button = new InlineKeyboardButton();
        back_to_menu_button.setText(EmojiParser.parseToUnicode(":pushpin: ")  + "Back to main menu");
        back_to_menu_button.setCallbackData("BACK_TO_MENU");

        back_to_menu_row.add(back_to_menu_button);
        back_to_menu.add(back_to_menu_row);

        try {
            execute(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(
                            EmojiParser.parseToUnicode(getWeatherIcon(((weatherJson.getJSONObject("current")).getJSONObject("condition")).getInt("code"))) + "*" + ((weatherJson.getJSONObject("current")).getJSONObject("condition")).getString("text") + "*" + "\n\n" +
                            EmojiParser.parseToUnicode(":thermometer: ") + "Temperature: " + (weatherJson.getJSONObject("current")).get("temp_c") + " °C" + "\n" +
                            EmojiParser.parseToUnicode(":thermometer: ") + "Feels like: " + (weatherJson.getJSONObject("current")).get("feelslike_c") + " °C" + "\n\n" +
                            EmojiParser.parseToUnicode(":dash: ") + "Wind speed: " + (weatherJson.getJSONObject("current")).get("wind_kph") + " km/h" + "\n\n" +
                            EmojiParser.parseToUnicode(":droplet: ") + "Humidity: " + (weatherJson.getJSONObject("current")).get("humidity") + " %" + "\n" +
                            EmojiParser.parseToUnicode(":sweat_drops: ") + "Pressure: " + (weatherJson.getJSONObject("current")).get("pressure_mb") + " mm Hg" + "\n"
                    )
                    .parseMode("Markdown")
                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(back_to_menu).build())
                    .build());
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void dayForecast(Message message) {
        HtmlRequest obj = new HtmlRequest();
        String url = "http://api.weatherapi.com/v1/forecast.json?key=0a642e967559402d83a192312231601&q=" + current_location[0] + "&days=1";
        StringBuilder weather = obj.getRequestResult(url);

        JSONObject weatherJson = new JSONObject(weather.toString());

        List<List<InlineKeyboardButton>> back_to_menu = new ArrayList<>();
        List<InlineKeyboardButton> back_to_menu_row = new ArrayList<>();

        var back_to_menu_button = new InlineKeyboardButton();
        back_to_menu_button.setText(EmojiParser.parseToUnicode(":pushpin: ")  + "Back to main menu");
        back_to_menu_button.setCallbackData("BACK_TO_MENU");

        back_to_menu_row.add(back_to_menu_button);
        back_to_menu.add(back_to_menu_row);

        try {
            execute(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(
                            "*Daily forecast (" + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getString("date")) + ")*" + "\n\n" +
                            EmojiParser.parseToUnicode(getWeatherIcon((((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("day")).getJSONObject("condition")).getInt("code")))
                                    + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("day")).getJSONObject("condition")).getString("text") + "\n\n" +
                            EmojiParser.parseToUnicode(":thermometer: ") + "Average temperature: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("day")).getDouble("avgtemp_c") + " °C" + "\n" +
                            EmojiParser.parseToUnicode(":thermometer: ") + "Min. temperature: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("day")).getDouble("mintemp_c") + " °C" + "\n" +
                            EmojiParser.parseToUnicode(":thermometer: ") + "Max. temperature: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("day")).getDouble("maxtemp_c") + " °C" + "\n\n" +
                            EmojiParser.parseToUnicode(":dash: ") + "Max. wind speed: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("day")).getDouble("maxwind_kph") + " km/h" + "\n" +
                            EmojiParser.parseToUnicode(":droplet: ") + "Average humidity: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("day")).getDouble("avghumidity") + " %" + "\n\n" +
                            EmojiParser.parseToUnicode(":cloud_rain: ") + "Chance of rain: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("day")).getDouble("daily_chance_of_rain") + " %" + "\n" +
                            EmojiParser.parseToUnicode(":snowflake: ") + "Chance of snow: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("day")).getDouble("daily_chance_of_snow") + " %" + "\n"
                    )
                    .parseMode("Markdown")
                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(back_to_menu).build())
                    .build());
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void hourForecast(Message message) {
        HtmlRequest obj = new HtmlRequest();
        String url = "http://api.weatherapi.com/v1/forecast.json?key=0a642e967559402d83a192312231601&q=" + current_location[0] + "&days=1";
        StringBuilder weather = obj.getRequestResult(url);

        JSONObject weatherJson = new JSONObject(weather.toString());

        List<List<InlineKeyboardButton>> back_to_menu = new ArrayList<>();
        List<InlineKeyboardButton> back_to_menu_row = new ArrayList<>();

        var back_to_menu_button = new InlineKeyboardButton();
        back_to_menu_button.setText(EmojiParser.parseToUnicode(":pushpin: ")  + "Back to main menu");
        back_to_menu_button.setCallbackData("BACK_TO_MENU");

        back_to_menu_row.add(back_to_menu_button);
        back_to_menu.add(back_to_menu_row);

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < 24; i++){
            sb.append(
                    EmojiParser.parseToUnicode(":clock1: ") + "*" + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(i)).getString("time") + "*" + "\n\n" +
                    EmojiParser.parseToUnicode(getWeatherIcon(((((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(i)).getJSONObject("condition")).getInt("code"))) +
                    ((((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(i)).getJSONObject("condition")).getString("text") + "\n\n" +
                    EmojiParser.parseToUnicode(":thermometer: ") + "Temperature: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(i)).getDouble("temp_c") + " °C" + "\n" +
                    EmojiParser.parseToUnicode(":thermometer: ") + "Feels like: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(i)).getDouble("feelslike_c") + " °C" + "\n\n" +
                    EmojiParser.parseToUnicode(":dash: ") + "Wind speed: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(i)).getDouble("wind_kph") + " km/h" + "\n\n" +
                    EmojiParser.parseToUnicode(":cloud_rain: ") + "Chance of rain: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(i)).getDouble("chance_of_rain") + " %" + "\n" +
                    EmojiParser.parseToUnicode(":snowflake: ") + "Chance of snow: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(i)).getDouble("chance_of_snow") + " %" + "\n\n"
            );
        }

        try {
            execute(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(sb.toString())
                    .parseMode("Markdown")
                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(back_to_menu).build())
                    .build());
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void threeDayForecast(Message message) {
        HtmlRequest obj = new HtmlRequest();
        String url = "http://api.weatherapi.com/v1/forecast.json?key=0a642e967559402d83a192312231601&q=" + current_location[0] + "&days=3";
        StringBuilder weather = obj.getRequestResult(url);

        JSONObject weatherJson = new JSONObject(weather.toString());

        List<List<InlineKeyboardButton>> back_to_menu = new ArrayList<>();
        List<InlineKeyboardButton> back_to_menu_row = new ArrayList<>();

        var back_to_menu_button = new InlineKeyboardButton();
        back_to_menu_button.setText(EmojiParser.parseToUnicode(":pushpin: ")  + "Back to main menu");
        back_to_menu_button.setCallbackData("BACK_TO_MENU");

        back_to_menu_row.add(back_to_menu_button);
        back_to_menu.add(back_to_menu_row);

        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < 3; i++){
            sb.append(
                    "*Weather forecast (" + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getString("date")) + ")*" + "\n\n" +
                    EmojiParser.parseToUnicode(getWeatherIcon((((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getJSONObject("day")).getJSONObject("condition")).getInt("code")))
                        + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getJSONObject("day")).getJSONObject("condition")).getString("text") + "\n\n" +
                    EmojiParser.parseToUnicode(":thermometer: ") + "Average temperature: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getJSONObject("day")).getDouble("avgtemp_c") + " °C" + "\n" +
                    EmojiParser.parseToUnicode(":thermometer: ") + "Min. temperature: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getJSONObject("day")).getDouble("mintemp_c") + " °C" + "\n" +
                    EmojiParser.parseToUnicode(":thermometer: ") + "Max. temperature: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getJSONObject("day")).getDouble("maxtemp_c") + " °C" + "\n\n" +
                    EmojiParser.parseToUnicode(":dash: ") + "Max. wind speed: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getJSONObject("day")).getDouble("maxwind_kph") + " km/h" + "\n" +
                    EmojiParser.parseToUnicode(":droplet: ") + "Average humidity: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getJSONObject("day")).getDouble("avghumidity") + " %" + "\n\n" +
                    EmojiParser.parseToUnicode(":cloud_rain: ") + "Chance of rain: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getJSONObject("day")).getDouble("daily_chance_of_rain") + " %" + "\n" +
                    EmojiParser.parseToUnicode(":snowflake: ") + "Chance of snow: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(i)).getJSONObject("day")).getDouble("daily_chance_of_snow") + " %" + "\n\n"
            );
        }

        try {
            execute(SendMessage.builder()
                    .chatId(message.getChatId())
                    .text(sb.toString())
                    .parseMode("Markdown")
                    .replyMarkup(InlineKeyboardMarkup.builder().keyboard(back_to_menu).build())
                    .build());
        }
        catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private String getWeatherIcon(int code){
        String str;

        switch(code){
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
            case (1150):
            case (1153):
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
                str = ":thunder_cloud_and_rain: "; break;
            default: str = ":rainbow: "; break;
        }

        return str;
    }
}
