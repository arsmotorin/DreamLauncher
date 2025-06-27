//package xyz.dream.launcher.managers;
//
//import com.google.gson.Gson;
//import org.apache.commons.io.FileUtils;
//import org.to2mbn.jmccc.auth.OfflineAuthenticator;
//import org.to2mbn.jmccc.launch.LaunchException;
//import org.to2mbn.jmccc.launch.Launcher;
//import org.to2mbn.jmccc.launch.LauncherBuilder;
//import org.to2mbn.jmccc.launch.ProcessListener;
//import org.to2mbn.jmccc.mcdownloader.MinecraftDownloadOption;
//import org.to2mbn.jmccc.mcdownloader.MinecraftDownloader;
//import org.to2mbn.jmccc.mcdownloader.MinecraftDownloaderBuilder;
//import org.to2mbn.jmccc.mcdownloader.download.concurrent.CombinedDownloadCallback;
//import org.to2mbn.jmccc.mcdownloader.provider.DownloadProviderChain;
//import org.to2mbn.jmccc.mcdownloader.provider.fabric.FabricDownloadProvider;
//import org.to2mbn.jmccc.option.LaunchOption;
//import org.to2mbn.jmccc.option.MinecraftDirectory;
//
//import javax.swing.*;
//import java.io.File;
//import java.io.IOException;
//import java.net.URI;
//import java.net.URL;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.util.Collections;
//import java.util.Objects;
//import java.util.concurrent.ExecutionException;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.concurrent.atomic.AtomicReference;
//import java.util.Map;
//import java.util.HashMap;
//
//public class Downloader {
//    static String minecraftPath = System.getProperty("os.name").toLowerCase().contains("mac") ?
//            System.getProperty("user.home") + "/.FrogDream" :
//            System.getenv("LOCALAPPDATA") + "/.FrogDream";
//    static MinecraftDirectory dir = new MinecraftDirectory(minecraftPath);
//
//    static String PREFIX = "https://new.frogdream.xyz/launcher/";
//
//    private static final Map<String, Object> cache = new HashMap<>();
//
//    public static void download() {
//        if (cache.containsKey("download")) {
//            System.out.println("Using cached download result.");
//            return;
//        }
//
//        processDownload();
//        //downloadMods();
//        System.out.println("Mods downloaded.");
//
//        cache.put("download", true);
//    }
//
//    static void processDownload() {
//        if (cache.containsKey("processDownload")) {
//            System.out.println("Using cached processDownload result.");
//            return;
//        }
//
//        FabricDownloadProvider fabricDownloadProvider = new FabricDownloadProvider();
//        MinecraftDownloader downloader = MinecraftDownloaderBuilder.create().providerChain(DownloadProviderChain.create().addProvider(fabricDownloadProvider)).build();
//
//        try {
//            downloader.downloadIncrementally(dir, "fabric-loader-0.15.11-1.21", (CombinedDownloadCallback) null, new MinecraftDownloadOption[0]).get();
//        } catch (ExecutionException | InterruptedException var3) {
//            throw new RuntimeException(var3);
//        }
//
//        cache.put("processDownload", true);
//    }
//
//public static void downloadMods() {
//    if (cache.containsKey("downloadMods")) {
//        System.out.println("Using cached downloadMods result.");
//        return;
//    }
//
//    HttpClient client = HttpClient.newHttpClient();
//    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(PREFIX + "files")).build();
//
//    try {
//        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
//        String[] files = new Gson().fromJson(response.body(), String[].class);
//
//        for (String file : files) {
//            System.out.println("Load: " + file);
//
//            try {
//                URL url = new URL(PREFIX + file);
//                File dest = new File(dir.getRoot(), file);
//                FileUtils.copyURLToFile(url, dest);
//            } catch (IOException e) {
//                System.err.println("Load error: " + file);
//                e.printStackTrace();
//            }
//        }
//    } catch (Exception e) {
//        System.err.println("Download error.");
//        throw new RuntimeException(e);
//    }
//
//    cache.put("downloadMods", true);
//}
//
//    public static void launch(String nick, JLabel playTextLabel, final AtomicReference<Float> currentBrightness, boolean swiftPlay, boolean makeOptimization, boolean makeFreecam, int xmx) {
//        if (!dir.getRoot().exists()) {
//            download();
//        }
//
//        int threads = Runtime.getRuntime().availableProcessors();
//
//        ExecutorService executorService = Executors.newFixedThreadPool(threads, r -> {
//            Thread thread = new Thread(r);
//            thread.setPriority(Thread.NORM_PRIORITY);
//            return thread;
//        });
//
//        executorService.submit(() -> {
//            Launcher launcher = LauncherBuilder.buildDefault();
//
//            try {
//                LaunchOption opts = new LaunchOption("fabric-loader-0.15.11-1.21", OfflineAuthenticator.name(nick), dir);
//
//                opts.setMaxMemory(xmx * 1024);
//                opts.setMinMemory(256);
//
//                System.setProperty("apple.awt.application.name", "Minecraft");
//
//            if (makeOptimization) {
//                Collections.addAll(opts.extraJvmArguments(), "-XX:+UseG1GC", "-XX:+ParallelRefProcEnabled", "-XX:+ParallelRefProcEnabled", "-XX:+UnlockExperimentalVMOptions", "-XX:MaxDirectMemorySize=256M", "-XX:+AlwaysPreTouch");
//            } else {
//                Collections.addAll(opts.extraJvmArguments(), "-Dsun.java2d.opengl=true", "-Dminecraft.disableMinecraftVersionCheck=true", "-Dminecraft.disableVsync=true", "-Duser.language=ru", "-Duser.country=RU");
//            }
//
//                File modsFolder = new File(minecraftPath + "/mods");
//
//                int finalInitialFileCount = Objects.requireNonNull(modsFolder.listFiles()).length;
//                launcher.launch(opts, new ProcessListener() {
//                    int logCount = 0;
//
//                    public void onLog(String log) {
//                        System.out.println(log);
//                        int lineCount = log.split("\n").length;
//                        logCount += lineCount;
//
//                        if (!swiftPlay) {
//                            if (logCount >= 10 && logCount < 20) {
//                                playTextLabel.setText("Please wait...");
//                            } else if (logCount >= 22 + finalInitialFileCount * 6) {
//                                playTextLabel.setText("Play");
//                                currentBrightness.set(1.11F);
//                            }
//                        } else {
//                            playTextLabel.setText("Swift-play");
//                        }
//                    }
//
//                    public void onErrorLog(String log) {
//                        System.err.println(log);
//                    }
//
//                    public void onExit(int code) {
//                        System.out.println("Exit with code " + code);
//
//                        if (swiftPlay) {
//                            System.out.println("Swift-play");
//                            currentBrightness.set(1.11F);
//                        } else {
//                            playTextLabel.setText("Play");
//                            currentBrightness.set(1.11F);
//                        }
//                    }
//                });
//            } catch (IOException | LaunchException var3) {
//                throw new RuntimeException(var3);
//            }
//        });
//
//        executorService.shutdown();
//    }
//}
