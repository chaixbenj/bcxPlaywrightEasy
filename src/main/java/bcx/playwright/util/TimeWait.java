package bcx.playwright.util;

import java.time.LocalDateTime;

public class TimeWait {
    LocalDateTime startWait;
    public TimeWait() {
        startWait = LocalDateTime.now();
    }

    public void reinit() {
        startWait = LocalDateTime.now();
    }
    public boolean over(int timeoutInSecond) {
        return startWait.plusSeconds(timeoutInSecond).isBefore(LocalDateTime.now());
    }
    public boolean notOver(int timeoutInSecond) {
        return startWait.plusSeconds(timeoutInSecond).isAfter(LocalDateTime.now());
    }
}
