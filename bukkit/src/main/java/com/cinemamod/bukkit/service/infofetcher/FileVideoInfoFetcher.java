package com.cinemamod.bukkit.service.infofetcher;

import com.cinemamod.bukkit.service.VideoServiceType;
import com.cinemamod.bukkit.video.VideoInfo;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import org.bytedeco.ffmpeg.ffprobe;
import org.bytedeco.javacpp.Loader;

public class FileVideoInfoFetcher extends VideoInfoFetcher {
    private static final String ffprobePath = Loader.load(ffprobe.class);
    private final String url;
    private final String requesterUsername;

    public FileVideoInfoFetcher(String permission, String url, String requesterUsername) {
        super(permission);
        this.url = url;
        this.requesterUsername = requesterUsername;
    }

    @Override
    public CompletableFuture<VideoInfo> fetch() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Process process = new ProcessBuilder(ffprobePath, "-i", this.url, "-show_entries", "format=duration", "-v", "quiet", "-of", "default=noprint_wrappers=1:nokey=1").redirectErrorStream(true).start();
                String result = FileVideoInfoFetcher.readInput(process.getInputStream()).trim();
                if (!result.isEmpty()) {
                    try {
                        VideoServiceType serviceType = VideoServiceType.FILE;
                        String id = this.url;
                        String title = this.url;
                        String thumbnailUrl = "https://cinemamod-static.ewr1.vultrobjects.com/images/file_thumbnail.jpg";
                        float durationSeconds = Float.parseFloat(result);
                        return new VideoInfo(serviceType, id, title, this.requesterUsername, thumbnailUrl, (long)durationSeconds);
                    } catch (NumberFormatException numberFormatException) {}
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }

    private static String readInput(InputStream inputStream) {
        try (Scanner scanner = new Scanner(inputStream);){
            StringBuilder stringBuilder = new StringBuilder();
            while (scanner.hasNext()) {
                stringBuilder.append(scanner.nextLine());
            }
            String string = stringBuilder.toString();
            return string;
        }
    }
}
