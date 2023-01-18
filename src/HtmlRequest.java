import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class HtmlRequest {
    public StringBuilder getRequestResult(String url){
        HttpURLConnection connection = null;
        StringBuilder sb = new StringBuilder();

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();

            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setConnectTimeout(250);
            connection.setReadTimeout(250);

            connection.connect();

            if (HttpURLConnection.HTTP_OK == connection.getResponseCode()){
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                while ((line = in.readLine()) != null){
                    sb.append(line);
                    sb.append("\n");
                }
            }
            else {
                sb.append("Error! Response code: " + connection.getResponseCode());
            }
        }
        catch (Throwable cause){
            cause.printStackTrace();
            sb.append("Error! Unable to execute html request");
        }
        finally {
            if (connection != null){
                connection.disconnect();
            }
        }
        return sb;
    }
}
