package com.aurora.profile;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

public class Network {

    private final File pfpsFolder;

    public Network(File dataFolder) {
        this.pfpsFolder = new File(dataFolder, "Pfps");
        if (!pfpsFolder.exists()) {
            pfpsFolder.mkdirs();
        }
    }

    public CompletableFuture<File> downloadHeadAsync(String username) {
        String url = "https://minotar.net/helm/" + username + "/256.png";

        File playerFolder = new File(pfpsFolder, username);
        if (!playerFolder.exists()) playerFolder.mkdirs();

        File headFile = new File(playerFolder, username + "_head.png");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    try {
                        // Сохраняем байты в файл
                        Files.write(headFile.toPath(), response.body());

                        if (!Files.exists(headFile.toPath())) {
                            throw new RuntimeException("Не удалось скачать голову для " + username);
                        }

                        return headFile;

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public CompletableFuture<File> downloadUpperBodyAsync(String username) {
        String url = "https://mc-heads.net/body/" + username + "/left/256.png";

        File playerFolder = new File(pfpsFolder, username);
        if (!playerFolder.exists()) playerFolder.mkdirs();

        File outputFile = new File(playerFolder, username + "_upper.png");

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    try {
                        if (response.body() == null || response.body().length == 0) {
                            throw new RuntimeException("Пустой ответ при скачивании изображения");
                        }

                        BufferedImage fullImage = ImageIO.read(new java.io.ByteArrayInputStream(response.body()));

                        if (fullImage == null) {
                            throw new RuntimeException("Не удалось загрузить изображение. Возможно, неправильный формат.");
                        }

                        int width = fullImage.getWidth();
                        int height = fullImage.getHeight();

                        BufferedImage upperHalf = fullImage.getSubimage(0, 0, width, height / 2);

                        BufferedImage copy = new BufferedImage(upperHalf.getWidth(), upperHalf.getHeight(), BufferedImage.TYPE_INT_ARGB);
                        Graphics2D g = copy.createGraphics();
                        g.drawImage(upperHalf, 0, 0, null);
                        g.dispose();

                        ImageIO.write(copy, "png", outputFile);

                        return outputFile;

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException("Ошибка при обработке изображения: " + e.getMessage());
                    }
                });
    }


}

