package org.emeraldcraft.githubapitest.UpdateChecker;

import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

import static org.emeraldcraft.githubapitest.Main.VERSION;

public class GithubUpdateChecker {
    public static void checkForUpdates(String oAuthAccessToken) throws IOException {
        GitHub gitHub = GitHub.connectAnonymously();
        //GitHub gitHub = GitHub.connectUsingOAuth(oAuthAccessToken);
        if(gitHub.getRateLimit().getRemaining() == 0){
            System.out.println("I could not contact GitHub because we have been rate-limited.");
            return;
        }
        System.out.println("Connected to GitHub successfully.");
        GHRepository repository = gitHub.getRepository("EmeraldWither/MCInfoSpigot");
        GHRelease release = repository.getLatestRelease();
        String tag = release.getTagName();

        String tagModified = tag.replaceAll("\\.", "");
        String currentVersionModified = VERSION.replace("v", "").replaceAll("\\.", "");

        int tagInt = Integer.parseInt(tagModified);
        int currentVersion = Integer.parseInt(currentVersionModified);
        if (tagInt <= currentVersion) {
            System.out.println("We are on the latest version (" + currentVersion + ")!");
            return;
        }

        System.out.println("<!> We have to upgrade! We are on v" + currentVersion + " while the latest is v" + tagInt + " <!>");
        List<GHAsset> files = release.listAssets().toList();
        for (GHAsset asset : files) {
            String[] possibleExtensions = asset.getName().split("\\.");
            String extension = possibleExtensions[possibleExtensions.length - 1];
            if (extension.equalsIgnoreCase("jar")) {
                String downloadLocation = System.getProperty("user.dir") + "/" + asset.getName();
                String runScriptLocation = System.getProperty("user.dir") + "/run.bat";

                downloadFile(new URL(asset.getBrowserDownloadUrl()), downloadLocation);
                overWriteStartScript(runScriptLocation, asset.getName());
                break;
            }
        }
    }

    private static void downloadFile(URL url, String fileName) {
        try (InputStream in = url.openStream()) {
            File file = new File(fileName);
            if (file.exists()) return;

            System.out.println("Downloading file to: " + Paths.get(fileName));
            Files.copy(in, Paths.get(fileName));
        } catch (IOException e) {
            System.out.println("I was unable to download the file " + fileName + " from " + url);
            e.printStackTrace();
        }
    }

    private static void overWriteStartScript(String path, String fileName) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Start script does not exist. Will not overwrite");
                return;
            }

            Scanner scanner = new Scanner(file);
            if (scanner.hasNextLine()) {
                FileWriter writer = new FileWriter(file);
                String line = scanner.nextLine();
                if (line.isBlank()) {
                    writer.close();
                    scanner.close();
                    return;
                }
                String s = line.split("-jar")[1];
                String previousArgs = line.split("-jar")[0] + "-jar ";
                String afterArgs = "\"";
                if (s.split("\"").length >= 3) {
                    afterArgs = "\"" + s.split("\"")[2];
                }
                String writeData = previousArgs + "\"" + fileName + afterArgs;
                writer.write(writeData);
                writer.close();
                scanner.close();
                System.out.println("Overwrote the start script at " + path);
            }
        } catch (IOException e) {
            System.out.println("I was unable to overwrite the start script located at " + path);
            e.printStackTrace();
        }
    }
}
