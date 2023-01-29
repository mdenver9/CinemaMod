package com.cinemamod.bukkit.storage.sql;

import com.cinemamod.bukkit.service.VideoServiceType;
import com.cinemamod.bukkit.video.VideoInfo;
import com.cinemamod.bukkit.video.VideoRequest;
import com.cinemamod.bukkit.storage.VideoStorage;

import javax.annotation.Nullable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class SQLVideoStorage extends VideoStorage {

    @Override
    public CompletableFuture<Void> saveVideoInfo(VideoInfo videoInfo) {
        return searchVideoInfo(videoInfo.getServiceType(), videoInfo.getId()).thenAcceptAsync(search -> {
            try (Connection connection = createConnection()) {
                if (search instanceof RelationalVideoInfo) {
                    updateVideoInfo(new RelationalVideoInfo(videoInfo, ((RelationalVideoInfo) search).getRelId()), connection);
                } else {
                    insertVideoInfo(videoInfo, connection);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public CompletableFuture<VideoInfo> searchVideoInfo(VideoServiceType videoService, String id) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = createConnection()) {
                return queryVideoInfo(videoService, id, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    @Override
    public CompletableFuture<Void> saveVideoRequest(VideoRequest request) {
        return searchVideoInfo(request.getVideoInfo().getServiceType(), request.getVideoInfo().getId()).thenAcceptAsync(videoInfoSearch -> {
            if (videoInfoSearch instanceof RelationalVideoInfo) {
                try (Connection connection = createConnection()) {
                    RelationalVideoRequest videoRequestSearch = queryVideoRequest(request.getRequester(), (RelationalVideoInfo) videoInfoSearch, connection);

                    if (videoRequestSearch == null) {
                        insertVideoRequest(request, (RelationalVideoInfo) videoInfoSearch, connection);
                    } else {
                        updateVideoRequest(new RelationalVideoRequest(request, videoRequestSearch.getRelationalVideoInfo()), connection);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public CompletableFuture<Set<VideoRequest>> loadVideoRequests(UUID requester) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = createConnection()) {
                return queryVideoRequests(requester, connection);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return null;
        });
    }

    @Nullable
    protected abstract RelationalVideoInfo queryVideoInfo(VideoServiceType videoService, String id, Connection connection) throws SQLException;

    protected abstract int insertVideoInfo(VideoInfo videoInfo, Connection connection) throws SQLException;

    protected abstract void updateVideoInfo(RelationalVideoInfo videoInfo, Connection connection) throws SQLException;

    @Nullable
    protected abstract RelationalVideoRequest queryVideoRequest(UUID requester, RelationalVideoInfo videoInfo, Connection connection) throws SQLException;

    protected abstract void insertVideoRequest(VideoRequest videoRequest, RelationalVideoInfo videoInfo, Connection connection) throws SQLException;

    protected abstract void updateVideoRequest(RelationalVideoRequest videoRequest, Connection connection) throws SQLException;

    protected abstract Set<VideoRequest> queryVideoRequests(UUID requester, Connection connection) throws SQLException;

    protected abstract void createTables(Connection connection) throws SQLException;

    protected abstract Connection createConnection() throws SQLException;

}
