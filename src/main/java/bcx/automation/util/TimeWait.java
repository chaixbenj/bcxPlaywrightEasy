package bcx.automation.util;

import java.time.LocalDateTime;

public class TimeWait {
    LocalDateTime startWait;
    public TimeWait() {
        startWait = LocalDateTime.now();
    }

    public void reinit() {
        startWait = LocalDateTime.now();
    }

    public boolean notOver(int timeoutInSecond) {
        return startWait.plusSeconds(timeoutInSecond).isAfter(LocalDateTime.now());
    }
}
