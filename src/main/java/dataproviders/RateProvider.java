package dataproviders;

import com.google.gson.Gson;
import dto.RateResponseDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class RateProvider {

    private static final Gson gson = new Gson();

    public static RateResponseDto getRateResponseDto() {

        try {
            URL url = new URL("https://player.adtelligent.com/exchange_rates/302837/config.json?cb=https://minfin.com.ua/company/privatbank/currency/developers/api/");
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            String inputLine;
            StringBuilder sb = new StringBuilder();

            while ((inputLine = in.readLine()) != null)
                sb.append(inputLine);

            in.close();

            return gson.fromJson(sb.toString(), RateResponseDto.class);

        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return null;
    }

}
