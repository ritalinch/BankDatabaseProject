package converters;

import dto.RateResponseDto;
import entities.Currency;
import entities.CurrencyRate;

import java.util.List;

public class RateDtoToEntity {

    public static List<CurrencyRate> convert(RateResponseDto dto) {
        return List.of(new CurrencyRate(Currency.EUR, dto.getConversions().getUAH().get("EUR")),
                new CurrencyRate(Currency.USD, dto.getConversions().getUAH().get("USD")));
    }

}
