package io.goji.exp.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sqlite.SQLiteConfig;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public class BangumiFetcher {

    static {
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "7890");
    }

    private static final String API_BASE = "https://bangumi.moe/api/torrent/";
    private static final String URL_BASE = "https://bangumi.moe/torrent/";
    private static final HttpClient client = HttpClient.newBuilder()
            .proxy(ProxySelector.of(new InetSocketAddress("localhost", 7890)))
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();


    private final static String DB_FILE = "bangumi_moe";
    private final static String CREATE_TABLE_STATEMENT = "CREATE TABLE [bangumi] (\n" +
            "  [_id] TEXT,\n" +
            "  [category_tag_id] TEXT,\n" +
            "  [title] TEXT,\n" +
            "  [introduction] TEXT,\n" +
            "  [tag_ids] TEXT,\n" +
            "  [comments] INT,\n" +
            "  [downloads] INT,\n" +
            "  [finished] INT,\n" +
            "  [leechers] INT,\n" +
            "  [seeders] INT,\n" +
            "  [uploader_id] TEXT,\n" +
            "  [team_id] TEXT NULL,\n" +
            "  [publish_time] TEXT,\n" +
            "  [magnet] TEXT,\n" +
            "  [infoHash] TEXT,\n" +
            "  [file_id] TEXT,\n" +
            "  [teamsync] INT NULL,\n" +
            "  [content] TEXT,\n" +
            "  [size] TEXT,\n" +
            "  [btskey] TEXT,\n" +
            "  [sync] TEXT NULL\n" +
            ");\n" +
            "\n";

    public static void main(String[] args) throws Exception {

        int pageCount = fetchPageCount();


        List<Map<String, Object>> totalTorrents = new ArrayList<>();

        Instant start = Instant.now();
        System.out.println("Total page count: " + pageCount + "\nStarting to fetch torrents...");

        // 理论上h2应该支持100个以上? 这里大概是服务端限制了, 所以分批处理
        // 如果pageCount大于30, 就分批处理, 这个值可以根据实际情况调整, 设置太大client会出现各种各样奇怪的问题
        int pageStart = 0, pageCountPerBatch = 30, pageTotal = pageCount;
        while (pageTotal - pageStart > 0) {
            List<Map<String, Object>> torrents = fetchAllTorrents(pageStart, pageCountPerBatch, pageTotal);
            totalTorrents.addAll(torrents);
            pageStart += pageCountPerBatch;
            System.out.println("Now pageStart: " + pageStart + "; torrents fetched: " + torrents.size());
            saveToJsonFile(torrents, "data-" + pageStart + ".json");
            Prepared_Statements_Single_Transaction_Batched(torrents);

        }
        Duration consumedTime =
                Duration.ofMillis(Instant.now().toEpochMilli() - start.toEpochMilli());
        System.out.println("Total torrents fetched: " + totalTorrents.size() + "\nTime consumed: " + consumedTime + "ms\n");

        // 数据有溢出风险
        //saveToJsonFile(totalTorrents, "data.json");

    }

    private static int fetchPageCount() throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(STR."\{API_BASE}latest"))
                .header("accept", "application/json, text/plain, */*")
                .header("accept-language", "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7")
                .header("cache-control", "no-cache")
                .header("cookie", "locale=en")
                .header("pragma", "no-cache")
                .header("priority", "u=1, i")
                .header("referer", "https://bangumi.moe/")
                .header("sec-ch-ua", "\"Google Chrome\";v=\"129\", \"Not=A?Brand\";v=\"8\", \"Chromium\";v=\"129\"")
                .header("sec-ch-ua-mobile", "?0")
                .header("sec-ch-ua-platform", "\"Windows\"")
                .header("sec-fetch-dest", "empty")
                .header("sec-fetch-mode", "cors")
                .header("sec-fetch-site", "same-origin")
                .header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/129.0.0.0 Safari/537.36")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> responseData = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {
        });

        return (int) responseData.get("page_count");
    }

    private static List<Map<String, Object>> fetchAllTorrents(int pageStart, int pageCountPerBatch, int pageTotal) throws InterruptedException, ExecutionException {

        List<Map<String, Object>> torrents = new ArrayList<>();

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            List<StructuredTaskScope.Subtask<List<Map<String, Object>>>> futures = new ArrayList<>();

            int pageEnd = pageStart + pageCountPerBatch;
            if (pageEnd > pageTotal) {
                pageEnd = pageTotal;
            }
            for (int i = pageStart; i <= pageEnd; i++) {
                final int page = i;
                futures.add(scope.fork(() -> fetchTorrentPage(page)));
            }


            scope.join();
            scope.throwIfFailed();

            for (var future : futures) {

                // Gather results from the completed tasks
                torrents.addAll(future.get());
            }
        }


        return torrents;
    }

    private static List<Map<String, Object>> fetchTorrentPage(int page) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_BASE + "page/" + page))
                .GET()
                .build();

        Files.writeString(Path.of("write.log"), "page=" + page + "\n", StandardOpenOption.APPEND, StandardOpenOption.CREATE);
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Map<String, Object> pageData = objectMapper.readValue(response.body(), new TypeReference<Map<String, Object>>() {
        });
        List<Map<String, Object>> torrentList = (List<Map<String, Object>>) pageData.get("torrents");

        List<Map<String, Object>> bangumiInfoList = new ArrayList<>();
        for (Map<String, Object> item : torrentList) {


            Map<String, Object> bangumiInfoItem = Map.ofEntries(
                    Map.entry("_id", Optional.ofNullable((String) item.get("_id")).orElse("")),
                    Map.entry("category_tag_id", Optional.ofNullable((String) item.get("category_tag_id")).orElse("")),
                    Map.entry("title", Optional.ofNullable((String) item.get("title")).orElse("")),
                    Map.entry("introduction", Optional.ofNullable((String) item.get("introduction")).orElse("")),
                    Map.entry("tag_ids",  convertToJson(item.get("tag_ids"))),
                    Map.entry("comments", Optional.ofNullable((Integer) item.get("comments")).orElse(0)),
                    Map.entry("downloads", Optional.ofNullable((Integer) item.get("downloads")).orElse(0)),
                    // todo, 类型转换异常如何处理?
                    Map.entry("finished", Optional.ofNullable((Integer) item.get("finished")).orElse(0)),
                    Map.entry("leechers", Optional.ofNullable((Integer) item.get("leechers")).orElse(0)),
                    Map.entry("seeders", Optional.ofNullable((Integer) item.get("seeders")).orElse(0)),
                    Map.entry("uploader_id", Optional.ofNullable((String) item.get("uploader_id")).orElse("")),
                    Map.entry("team_id", Optional.ofNullable((String) item.get("team_id")).orElse("")),
                    Map.entry("publish_time", Optional.ofNullable((String) item.get("publish_time")).orElse("")),
                    Map.entry("magnet", Optional.ofNullable((String) item.get("magnet")).orElse("")),
                    Map.entry("infoHash", Optional.ofNullable((String) item.get("infoHash")).orElse("")),
                    Map.entry("file_id", Optional.ofNullable((String) item.get("file_id")).orElse("")),
                    Map.entry("teamsync", Optional.ofNullable((Boolean) item.get("teamsync")).orElse(false)),
                    Map.entry("content", convertToJson(item.get("content"))) ,
                    Map.entry("size", Optional.ofNullable((String) item.get("size")).orElse("")),
                    Map.entry("btskey", Optional.ofNullable((String) item.get("btskey")).orElse("")),
                    Map.entry("sync", convertToJson(item.get("sync")))

            );
//          System.out.printf("%s UP\t%s:\t%s%n", bangumiInfoItem.get("seeders"), bangumiInfoItem.get("title"), bangumiInfoItem.get("url"));
            bangumiInfoList.add(bangumiInfoItem);
        }
        return bangumiInfoList;
    }

    // 将 Object 转换为 JSON 字符串
    private static String convertToJson(Object value) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            if (value instanceof List) {
                List<?> list = (List<?>) value;
                return list.isEmpty() ? "[]" : objectMapper.writeValueAsString(list);
            } else {
                return Optional.ofNullable(value).map(v -> {
                            try {
                                return objectMapper.writeValueAsString(v);
                            } catch (Exception e) {
                                e.printStackTrace();
                                return "null";
                            }
                        }
                ).orElse("null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "null"; // 出现错误时返回 null
        }
    }

    private static void saveToJsonFile(List<Map<String, Object>> torrents, String fileName) throws Exception {
        String json = objectMapper.writeValueAsString(torrents);
        Files.writeString(Path.of(fileName), json, StandardOpenOption.CREATE);
    }

    public static void Prepared_Statements_Single_Transaction_Batched(List<Map<String, Object>> torrents) throws Exception {
        SQLiteConfig config = new SQLiteConfig();

        try (Connection connection = config.createConnection("jdbc:sqlite:" + DB_FILE + "-" + LocalDateTime.now().toLocalDate() + ".sqlite")) {

            connection.setAutoCommit(false);

            // 检查表是否存在，如果不存在则创建
            try (Statement statement = connection.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='bangumi'");
                if (!resultSet.next()) {
                    statement.executeUpdate(CREATE_TABLE_STATEMENT);
                }
            }
            //     String batchInsertSql = "INSERT INTO [bangumi] ([_id],[category_tag_id],[title],[introduction],[tag_ids],[comments],[downloads],[finished],[leechers],[seeders],[uploader_id],[team_id],[publish_time],[magnet],[infoHash],[file_id],[teamsync],[content],[size],[btskey],[sync]) VALUES";
            String insertSql = "INSERT INTO bangumi VALUES (?, ?, ?, ?, ?,   ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?, ?, ?, ?, ?,    ?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {
                int batch_size = torrents.size();
                for (int i = 0; i < torrents.size(); i++) {
                    preparedStatement.setString(1, Optional.ofNullable((String) torrents.get(i).get("_id")).orElse(""));
                    preparedStatement.setString(2, Optional.ofNullable((String) torrents.get(i).get("category_tag_id")).orElse(""));
                    preparedStatement.setString(3, Optional.ofNullable((String) torrents.get(i).get("title")).orElse(""));
                    preparedStatement.setString(4, Optional.ofNullable((String) torrents.get(i).get("introduction")).orElse(""));
                    preparedStatement.setString(5, Optional.ofNullable((String) torrents.get(i).get("tag_ids")).orElse(""));
                    preparedStatement.setInt(6, Optional.ofNullable((Integer) torrents.get(i).get("comments")).orElse(0));
                    preparedStatement.setInt(7, Optional.ofNullable((Integer) torrents.get(i).get("downloads")).orElse(0));
                    preparedStatement.setInt(8, Optional.ofNullable((Integer) torrents.get(i).get("finished")).orElse(0));
                    preparedStatement.setInt(9, Optional.ofNullable((Integer) torrents.get(i).get("leechers")).orElse(0));
                    preparedStatement.setInt(10, Optional.ofNullable((Integer) torrents.get(i).get("seeders")).orElse(0));
                    preparedStatement.setString(11, Optional.ofNullable((String) torrents.get(i).get("uploader_id")).orElse(""));
                    preparedStatement.setString(12, Optional.ofNullable((String) torrents.get(i).get("team_id")).orElse(""));
                    preparedStatement.setString(13, Optional.ofNullable((String) torrents.get(i).get("publish_time")).orElse(""));
                    preparedStatement.setString(14, Optional.ofNullable((String) torrents.get(i).get("magnet")).orElse(""));
                    preparedStatement.setString(15, Optional.ofNullable((String) torrents.get(i).get("infoHash")).orElse(""));
                    preparedStatement.setString(16, Optional.ofNullable((String) torrents.get(i).get("file_id")).orElse(""));
                    preparedStatement.setInt(17, (Integer)(!((Boolean) torrents.get(i).get("teamsync")) ? 0 : 1));
                    preparedStatement.setString(18, Optional.ofNullable((String) torrents.get(i).get("content")).orElse(""));
                    preparedStatement.setString(19, Optional.ofNullable((String) torrents.get(i).get("size")).orElse(""));
                    preparedStatement.setString(20, Optional.ofNullable((String) torrents.get(i).get("btskey")).orElse(""));
                    preparedStatement.setString(21, Optional.ofNullable((String) torrents.get(i).get("sync")).orElse(""));


                    preparedStatement.addBatch();

//                    if (i % batch_size == 0 && i > 0) {
//                        preparedStatement.executeBatch();
//                    }
                }
                preparedStatement.executeBatch();

            }

            connection.commit();

        }


    }
}
