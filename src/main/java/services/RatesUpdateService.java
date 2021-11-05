package services;

import converters.RateDtoToEntity;
import dataproviders.RateProvider;

public class RatesUpdateService {

    private static final Runnable task = () -> {
        try {
            while (true) {
                RateDtoToEntity.convert(RateProvider.getRateResponseDto())
                        .forEach(rate -> MainService.performTransaction(
                                MainService.em(),
                                () -> MainService.em().persist(rate)
                        ));
                Thread.sleep(1000000000000000000L);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    };

    public static void update() {
        new Thread(task).start();
    }
}
