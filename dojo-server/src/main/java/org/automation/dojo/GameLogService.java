package org.automation.dojo;

import org.automation.dojo.web.scenario.BasicScenario;
import org.automation.dojo.web.scenario.Release;

import java.util.ArrayList;
import java.util.List;

/**
 * @author serhiy.zelenin
 */
public class GameLogService implements LogService {
    public void playerLog(PlayerRecord record) {
    }

    public List<GameLog> getGameLogs(String clientAddress, BasicScenario scenario) {
        return new ArrayList<GameLog>();
    }

    public List<String> getUniqueClientAddresses() {
        return new ArrayList<String>();
    }

    public void createGameLog(Release previousRelease) {
    }
}