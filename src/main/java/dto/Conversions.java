package dto;

import java.util.Map;

public class Conversions {

    private Map<String, Double> UAH;

    public Map<String, Double> getUAH() {
        return UAH;
    }

    @Override
    public String toString() {
        return "Conversions{" +
                "UAH=" + UAH +
                '}';
    }

}
