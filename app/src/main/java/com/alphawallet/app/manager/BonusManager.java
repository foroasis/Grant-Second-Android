package teleblock.manager;

import org.telegram.messenger.ApplicationLoader;

import java.util.Timer;
import java.util.TimerTask;
import teleblock.util.TelegramUtil;


public class BonusManager {

    private static BonusManager instance;
    private Timer timer;
    private TimerTask task;
    private boolean running;

    public static BonusManager getInstance() {
        if (instance == null) {
            synchronized (BonusManager.class) {
                if (instance == null) {
                    instance = new BonusManager();
                }
            }
        }
        return instance;
    }

    public BonusManager() {
        timer = new Timer();
        initTimerTask();
    }

    private void initTimerTask() {
        task = new TimerTask() {
            @Override
            public void run() {
                running = true;
                executeTask();
            }
        };
    }

    private void executeTask() {
        // 应用在后台不执行
        if (ApplicationLoader.mainInterfacePaused) {
            cancelTicker();
            return;
        }

        TelegramUtil.requestBonusUnderway();
    }

    public void startTicker() {
        if (running) return;
        initTimerTask();
        timer.schedule(task, 0, 1000 * 60 * 5);
    }

    public void cancelTicker() {
        running = false;
        task.cancel();
    }
}
