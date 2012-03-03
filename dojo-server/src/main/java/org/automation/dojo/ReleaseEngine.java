package org.automation.dojo;

import org.apache.commons.io.IOUtils;
import org.automation.dojo.web.scenario.BasicScenario;
import org.automation.dojo.web.scenario.Release;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import sun.misc.JavaSecurityProtectionDomainAccess;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.fest.reflect.core.Reflection.constructor;

/**
 * @author serhiy.zelenin
 */
public class ReleaseEngine {
    private List<Release> releases = new ArrayList<Release>();
    protected int currentReleaseIndex = 0;

    private Resource scenarioResource;

    @Autowired
    private BugsQueue bugsQueue;

    @Autowired
    private ScoreService scoreService;

    @Autowired
    private LogService logService;

    public ReleaseEngine() {
    }

    public ReleaseEngine(BugsQueue bugsQueue, ScoreService scoreService, LogService logService) {
        this.bugsQueue = bugsQueue;
        this.scoreService = scoreService;
        this.logService = logService;
    }

    public ReleaseEngine(Release ... releasesArray) {
        Collections.addAll(releases, releasesArray);
    }

     public Release getCurrentRelease() {
        return releases.get(currentReleaseIndex);
    }

    public void nextMajorRelease() {
        if (currentReleaseIndex == releases.size() - 1) {
            return;
        }
        notifyServices();
        setMajorRelease(currentReleaseIndex + 1);
    }

    public void nextMinorRelease() {
        notifyServices();
        getCurrentRelease().takeNextBug();
    }

    private void notifyServices() {
        //WARN!! the order is not tested but it is important to calculate scores
        logService.createGameLog(getCurrentRelease());
        scoreService.nextRelease(getCurrentRelease());
    }

    public void init() {
        try {
            List<String> scenarioLines = IOUtils.readLines(scenarioResource.getInputStream());
            int i = 0;
            int currentReleaseNumber = 1;
            Release currentRelease = new Release();
            while (i<scenarioLines.size()) {
                String[] scenarioParts = scenarioLines.get(i).split(",");
                int scenarioId = Integer.parseInt(scenarioParts[0]);
                int releaseNumber = Integer.parseInt(scenarioParts[2]);
                String scenarioDescription = scenarioParts[1];
                Class<BasicScenario> scenarioClass = getScenarioClassByName(scenarioParts[3]);

                if (currentReleaseNumber != releaseNumber) {
                    releases.add(currentRelease);
                    currentReleaseNumber = releaseNumber;
                    currentRelease = new Release();
                }

                currentRelease.addScenario(scenario(scenarioClass, scenarioId, scenarioDescription));
                i++;
            }
            releases.add(currentRelease);
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifyServices();
    }

    private Class<BasicScenario> getScenarioClassByName(String className) {
        Class<?> aClass = null;
        try {
            aClass = this.getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(String.format("Class %s not found in the classpath", className));
        }
        if (aClass != null && BasicScenario.class.isAssignableFrom(aClass)) {
            return (Class<BasicScenario>)aClass;
        } else {
            throw new IllegalArgumentException("This is not scenario class: " + aClass.getName());
        }
    }

    private BasicScenario scenario(Class<BasicScenario> scenarioClass, int id, String description) {
        return constructor().withParameterTypes(int.class, String.class, BugsQueue.class)
                             .in(scenarioClass)
                             .newInstance(id, description, bugsQueue);
    }

    public void setScenarioResource(Resource scenarioResource) {
        this.scenarioResource = scenarioResource;
    }

    public BasicScenario getScenario(int scenarioId) {
        return getCurrentRelease().getScenario(scenarioId);
    }

    public String getMinorInfo() {
        return getCurrentRelease().toString();
    }

    public String getMajorInfo() {
        return String.valueOf(currentReleaseIndex);
    }

    //WARN!! public for testing only!!!
    public void setMajorRelease(int index) {
        currentReleaseIndex = index;
        getCurrentRelease().setNoBug();
    }

    public List<BasicScenario> getCurrentScenarios() {
        return getCurrentRelease().getScenarios();
    }
}
