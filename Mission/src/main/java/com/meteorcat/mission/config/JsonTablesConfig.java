package com.meteorcat.mission.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Json策划表配置
 * @author MeteorCat
 */
@Configuration
public class JsonTablesConfig {

    /**
     * 日志句柄
     */
    Logger logger = LoggerFactory.getLogger(JsonTablesConfig.class);

    /**
     * 节点配置
     */
    @Value("${mix.mission.table.path}")
    private String path;


    /**
     * JSON解析器
     */
    private final ObjectMapper mapper = new ObjectMapper();


    /**
     * 暴露全局JSON配置表
     * @return Map<String, JsonNode>
     * @throws IOException IO异常
     */
    @Bean
    public Map<String, JsonNode> getNodes() throws IOException {
        Map<String,JsonNode> nodes = new HashMap<>();
        Path pathHandler = Path.of(path);
        try(Stream<Path> filenames = Files.list(pathHandler)){
            // 检索所有的JSON文件
            final String prefixName = ".json";
            filenames.forEach(hit->{
                String f = hit.toString();
                if(f.endsWith(prefixName)){
                    String filename = String.format("%s/%s",path,hit.getFileName());
                    try {

                        // 读取JSON文件内容选取文件名为Key
                        String name = hit.getFileName().toString().replace(prefixName,"");
                        String content = readFileString(filename);

                        // 解析JSON对象并记录
                        JsonNode node = mapper.readTree(content);
                        nodes.put(name,node);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

        // 遍历加载的配置JSON
        if(logger.isInfoEnabled()){
            for (Map.Entry<String, JsonNode> node : nodes.entrySet()) {
                logger.info("Load Config = {}.json",node.getKey());
            }
        }
        return nodes;
    }


    /**
     * 读取文件所有文本内容
     * @param filename 文件名
     * @return String
     * @throws IOException IO异常
     */
    private String readFileString(String filename) throws IOException {
        return Files.readString(Path.of(filename));
    }

}
