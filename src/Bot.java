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
                hourlyForecastMenu(message);
                break;
            case "HOUR_00":
                hourForecast(message, 0);
                break;
            case "HOUR_01":
                hourForecast(message, 1);
                break;
            case "HOUR_02":
                hourForecast(message, 2);
                break;
            case "HOUR_03":
                hourForecast(message, 3);
                break;
            case "HOUR_04":
                hourForecast(message, 4);
                break;
            case "HOUR_05":
                hourForecast(message, 5);
                break;
            case "HOUR_06":
                hourForecast(message, 6);
                break;
            case "HOUR_07":
                hourForecast(message, 7);
                break;
            case "HOUR_08":
                hourForecast(message, 8);
                break;
            case "HOUR_09":
                hourForecast(message, 9);
                break;
            case "HOUR_10":
                hourForecast(message, 10);
                break;
            case "HOUR_11":
                hourForecast(message, 11);
                break;
            case "HOUR_12":
                hourForecast(message, 12);
                break;
            case "HOUR_13":
                hourForecast(message, 13);
                break;
            case "HOUR_14":
                hourForecast(message, 14);
                break;
            case "HOUR_15":
                hourForecast(message, 15);
                break;
            case "HOUR_16":
                hourForecast(message, 16);
                break;
            case "HOUR_17":
                hourForecast(message, 17);
                break;
            case "HOUR_18":
                hourForecast(message, 18);
                break;
            case "HOUR_19":
                hourForecast(message, 19);
                break;
            case "HOUR_20":
                hourForecast(message, 20);
                break;
            case "HOUR_21":
                hourForecast(message, 21);
                break;
            case "HOUR_22":
                hourForecast(message, 22);
                break;
            case "HOUR_23":
                hourForecast(message, 23);
                break;
            case "SHOW_ALL_HOURS":
                hourForecast(message);
                break;
            case "DAY_3_FORECAST_BUTTON":
                threeDayForecast(message);
                break;
            case "ASTRO_FORECAST_BUTTON":
                astroForecast(message);
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
        List<InlineKeyboardButton> row_third_inline = new ArrayList<>();

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

        var astro_forecast_but = new InlineKeyboardButton();
        astro_forecast_but.setText("Astronomical forecast");
        astro_forecast_but.setCallbackData("ASTRO_FORECAST_BUTTON");

        row_inline.add(curr_weather_but);
        row_inline.add(day_forecast_but);
        row_second_inline.add(hour_forecast_but);
        row_second_inline.add(day_3_weather_forecast_but);
        row_third_inline.add(astro_forecast_but);

        rows_inline.add(row_inline);
        rows_inline.add(row_second_inline);
        rows_inline.add(row_third_inline);

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

    private InlineKeyboardButton createHourlyButtons(String hour){
        var obj = new InlineKeyboardButton();
        obj.setText(hour + ":00");
        obj.setCallbackData("HOUR_" + hour);

        return obj;
    }

    private void hourlyForecastMenu(Message message) throws TelegramApiException {
        List<List<InlineKeyboardButton>> rows_inline = new ArrayList<>();

        List<InlineKeyboardButton> row_first_inline = new ArrayList<>();
        List<InlineKeyboardButton> row_second_inline = new ArrayList<>();
        List<InlineKeyboardButton> row_third_inline = new ArrayList<>();
        List<InlineKeyboardButton> row_fourth_inline = new ArrayList<>();
        List<InlineKeyboardButton> row_fifth_inline = new ArrayList<>();
        List<InlineKeyboardButton> row_sixth_inline = new ArrayList<>();
        List<InlineKeyboardButton> row_seventh_inline = new ArrayList<>();

        var but_00 = createHourlyButtons("00");
        var but_01 = createHourlyButtons("01");
        var but_02 = createHourlyButtons("02");
        var but_03 = createHourlyButtons("03");
        var but_04 = createHourlyButtons("04");
        var but_05 = createHourlyButtons("05");
        var but_06 = createHourlyButtons("06");
        var but_07 = createHourlyButtons("07");
        var but_08 = createHourlyButtons("08");
        var but_09 = createHourlyButtons("09");
        var but_10 = createHourlyButtons("10");
        var but_11 = createHourlyButtons("11");
        var but_12 = createHourlyButtons("12");
        var but_13 = createHourlyButtons("13");
        var but_14 = createHourlyButtons("14");
        var but_15 = createHourlyButtons("15");
        var but_16 = createHourlyButtons("16");
        var but_17 = createHourlyButtons("17");
        var but_18 = createHourlyButtons("18");
        var but_19 = createHourlyButtons("19");
        var but_20 = createHourlyButtons("20");
        var but_21 = createHourlyButtons("21");
        var but_22 = createHourlyButtons("22");
        var but_23 = createHourlyButtons("23");

        var show_all = new InlineKeyboardButton();
        show_all.setText("Show all hours");
        show_all.setCallbackData("SHOW_ALL_HOURS");

        var back_to_menu_button = new InlineKeyboardButton();
        back_to_menu_button.setText("Back to main menu");
        back_to_menu_button.setCallbackData("BACK_TO_MENU");

        row_first_inline.add(but_00);
        row_first_inline.add(but_01);
        row_first_inline.add(but_02);
        row_first_inline.add(but_03);

        row_second_inline.add(but_04);
        row_second_inline.add(but_05);
        row_second_inline.add(but_06);
        row_second_inline.add(but_07);

        row_third_inline.add(but_08);
        row_third_inline.add(but_09);
        row_third_inline.add(but_10);
        row_third_inline.add(but_11);

        row_fourth_inline.add(but_12);
        row_fourth_inline.add(but_13);
        row_fourth_inline.add(but_14);
        row_fourth_inline.add(but_15);

        row_fifth_inline.add(but_16);
        row_fifth_inline.add(but_17);
        row_fifth_inline.add(but_18);
        row_fifth_inline.add(but_19);

        row_sixth_inline.add(but_20);
        row_sixth_inline.add(but_21);
        row_sixth_inline.add(but_22);
        row_sixth_inline.add(but_23);

        row_seventh_inline.add(show_all);
        row_seventh_inline.add(back_to_menu_button);

        rows_inline.add(row_first_inline);
        rows_inline.add(row_second_inline);
        rows_inline.add(row_third_inline);
        rows_inline.add(row_fourth_inline);
        rows_inline.add(row_fifth_inline);
        rows_inline.add(row_sixth_inline);
        rows_inline.add(row_seventh_inline);

        execute(SendMessage.builder()
                .chatId(message.getChatId())
                .text(
                    EmojiParser.parseToUnicode(":point_right: ") + "Please select the time you are interested in or display the hourly weather for the whole day"
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
        back_to_menu_button.setText(EmojiParser.parseToUnicode(":pushpin: ") + "Back to main menu");
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

    private void hourForecast(Message message, int num) {
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
                            EmojiParser.parseToUnicode(":clock1: ") + "*" + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getString("time") + "*" + "\n\n" +
                            EmojiParser.parseToUnicode(getWeatherIcon(((((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getJSONObject("condition")).getInt("code"))) +
                                ((((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getJSONObject("condition")).getString("text") + "\n\n" +
                            EmojiParser.parseToUnicode(":thermometer: ") + "Temperature: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("temp_c") + " °C" + "\n" +
                            EmojiParser.parseToUnicode(":thermometer: ") + "Feels like: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("feelslike_c") + " °C" + "\n\n" +
                            EmojiParser.parseToUnicode(":dash: ") + "Wind speed: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("wind_kph") + " km/h" + "\n\n" +
                            EmojiParser.parseToUnicode(":cloud_rain: ") + "Chance of rain: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("chance_of_rain") + " %" + "\n" +
                            EmojiParser.parseToUnicode(":snowflake: ") + "Chance of snow: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("chance_of_snow") + " %" + "\n\n"
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

        for(int num = 0; num < 24; num++){
            sb.append(
                    EmojiParser.parseToUnicode(":clock1: ") + "*" + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getString("time") + "*" + "\n\n" +
                    EmojiParser.parseToUnicode(getWeatherIcon(((((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getJSONObject("condition")).getInt("code"))) +
                        ((((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getJSONObject("condition")).getString("text") + "\n\n" +
                    EmojiParser.parseToUnicode(":thermometer: ") + "Temperature: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("temp_c") + " °C" + "\n" +
                    EmojiParser.parseToUnicode(":thermometer: ") + "Feels like: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("feelslike_c") + " °C" + "\n\n" +
                    EmojiParser.parseToUnicode(":dash: ") + "Wind speed: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("wind_kph") + " km/h" + "\n\n" +
                    EmojiParser.parseToUnicode(":cloud_rain: ") + "Chance of rain: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("chance_of_rain") + " %" + "\n" +
                    EmojiParser.parseToUnicode(":snowflake: ") + "Chance of snow: " + (((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONArray("hour")).getJSONObject(num)).getDouble("chance_of_snow") + " %" + "\n\n"
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

    private void astroForecast(Message message) throws TelegramApiException {
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

        execute(SendMessage.builder()
                .chatId(message.getChatId())
                .text(
                    "*Astronomical forecast (" + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getString("date")) + ")*" + "\n\n" +
                    EmojiParser.parseToUnicode(":sun_with_face: ") + "Sunrise: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("astro")).getString("sunrise") + "\n" +
                    EmojiParser.parseToUnicode(":sun_with_face: ") + "Sunset: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("astro")).getString("sunset") + "\n\n" +
                    EmojiParser.parseToUnicode(":full_moon_with_face: ") + "Moonrise: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("astro")).getString("moonrise") + "\n" +
                    EmojiParser.parseToUnicode(":full_moon_with_face: ") + "Moonset: " + ((((weatherJson.getJSONObject("forecast")).getJSONArray("forecastday")).getJSONObject(0)).getJSONObject("astro")).getString("moonset")

                )
                .parseMode("Markdown")
                .replyMarkup(InlineKeyboardMarkup.builder().keyboard(back_to_menu).build())
                .build());
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
