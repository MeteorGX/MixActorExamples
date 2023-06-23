package com.meteorcat.mission;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 测试Excel转JSON
 */
@SpringBootTest
class MissionApplicationTests {

    /**
     * 日志句柄
     */
    Logger logger = LoggerFactory.getLogger(MissionApplicationTests.class);

    /**
     * JSON解析器
     */
    ObjectMapper mapper = new ObjectMapper();

    /**
     * 测试Excel转化
     * @throws IOException IO异常
     */
    @Test
    void contextLoads() throws IOException {
        // 检索目录所有JSON文件
        String dirname = System.getProperty("user.dir");
        dirname = String.format("%s/tables",dirname);
        Path pathHandler = Path.of(dirname);
        logger.info("Read Dirname = {}",pathHandler.getFileName());

        // 检索出所有JSON文件
        Map<String,JsonNode> nodes = new HashMap<>();
        try(Stream<Path> tmp = Files.list(pathHandler)){
            String finalDirname = dirname;
            tmp.filter(f-> f.toString().endsWith("json")).forEach(f->{
                String name = f.getFileName().toString().replace(".json","");
                String filename = String.format("%s/%s", finalDirname,f.getFileName());
                logger.info("Read Filename({}) = {}",name,filename);
                try {
                    // 转化为JsonNode
                    String data = Files.readString(Path.of(filename));
                    JsonNode node = mapper.readTree(data);
                    nodes.put(name,node);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }


        // 检索出所有 Excel 配置


    }

}
