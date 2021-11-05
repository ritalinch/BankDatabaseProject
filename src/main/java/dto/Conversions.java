package dto;

import java.util.Map;

public class Conversions {

    private Map<String, Float> UAH;

    public Map<String, Float> getUAH() {
        return UAH;
    }

    @Override
    public String toString() {
        return "Conversions{" +
                "UAH=" + UAH +
                '}';
    }

}
