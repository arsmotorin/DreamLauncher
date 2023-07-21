package xyz.frogdream.launcher.downloader;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.to2mbn.jmccc.auth.OfflineAuthenticator;
import org.to2mbn.jmccc.launch.LaunchException;
import org.to2mbn.jmccc.launch.Launcher;
import org.to2mbn.jmccc.launch.LauncherBuilder;
import org.to2mbn.jmccc.launch.ProcessListener;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloadOption;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
import org.to2mbn.jmccc.mcdownloader.MinecraftDownloaderBuilder;
import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChain;
import org.to2mbn.jmccc.mcdownloader.provider.fabric.FabricDownloadProvider;
import org.to2mbn.jmccc.option.LaunchOption;
import org.to2mbn.jmccc.option.MinecraftDirectory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

// Don't change any piece of this code, but you can add more args in ARGS.

public class Download {
    static MinecraftDirectory dir = new MinecraftDirectory(System.getenv("LOCALAPPDATA") + "/.FrogDream");
    static String PREFIX = "https://new.frogdream.xyz/launcher/";
    static String[] ARGS = new String[]{"-XX:+UseG1GC", "-Dsun.java2d.opengl=true", "-Dminecraft.maxFPS=240", "-Dminecraft.disableVsync=true", "-XX:MaxGCPauseMillis=20", "-Duser.language=ru", "-Duser.country=RU", "-Dminecraft.disableMinecraftVersionCheck=true"};

    public static void download() {

        System.out.println("Установка в " + dir.getRoot().toString());

        processDownload();
        downloadMods();
        System.out.println("Предустановленные моды успешно установлены.");
    }

    static void processDownload() {

        FabricDownloadProvider fabricDownloadProvider = new FabricDownloadProvider();
        MinecraftDownloader downloader = MinecraftDownloaderBuilder.create().providerChain(DownloadProviderChain.create().addProvider(fabricDownloadProvider)).build();

        try {
            downloader.downloadIncrementally(dir, "fabric-loader-0.14.21-1.20.1", (CombinedDownloadCallback)null, new MinecraftDownloadOption[0]).get();
        } catch (ExecutionException | InterruptedException var3) {
            throw new RuntimeException(var3);
        }
    }

    public static void downloadMods() {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(PREFIX + "files")).build();

        HttpResponse response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException | IOException var11) {
            System.err.println("Не удалось загрузить список");
            throw new RuntimeException(var11);
        }

        String[] files = (String[])(new Gson()).fromJson((String)response.body(), String[].class);
        String[] var4 = files;
        int var5 = files.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            String file = var4[var6];
            System.out.println("Загрузка файла: " + file);

            try {
                URL url = new URL(PREFIX + file);
                File dest = new File(dir.getRoot(), file);
                FileUtils.copyURLToFile(url, dest);
            } catch (IOException var10) {
                System.err.println("Не удалось загрузить: " + file);
                var10.printStackTrace();
            }
        }

    }

    public static void launch(String nick) {

        if (!dir.getRoot().exists()) {
            download();
        }

        Launcher launcher = LauncherBuilder.buildDefault();

        try {
            LaunchOption opts = new LaunchOption("fabric-loader-0.14.21-1.20.1", OfflineAuthenticator.name(nick), dir);
            opts.setMaxMemory(8192);
            opts.setMinMemory(1024);
            Collections.addAll(opts.extraJvmArguments(), ARGS);
            launcher.launch(opts, new ProcessListener() {
                public void onLog(String log) {
                    System.out.println(log);
                }

                public void onErrorLog(String log) {
                    System.err.println(log);
                }

                public void onExit(int code) {
                    System.out.println("Exit with code " + code);
                }
            });
        } catch (IOException | LaunchException var3) {
            throw new RuntimeException(var3);
        }
    }
}
