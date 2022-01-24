package org.emeraldcraft.githubapitest;

import java.io.IOException;

import static org.emeraldcraft.githubapitest.UpdateChecker.GithubUpdateChecker.checkForUpdates;

public class Main {
    public static final String VERSION = "v1.1.1";
    public static void main(String[] args) throws IOException {
        checkForUpdates(args[0]);
        System.out.println("Finished checking for updates.");
    }

}
