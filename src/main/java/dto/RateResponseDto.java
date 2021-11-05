package dto;

public class RateResponseDto {

    private String dateAsOf;
    private Conversions conversions;

    public Conversions getConversions() {
        return conversions;
    }

    public String getDateAsOf() {
        return dateAsOf;
    }

    @Override
    public String toString() {
        return "RateResponseDto{" +
                "dateAsOf='" + dateAsOf + '\'' +
                ", conversions=" + conversions +
                '}';
    }

}
