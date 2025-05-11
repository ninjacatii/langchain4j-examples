package dev.langchain4j.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestJsonParse {
    @Test
    public void test() {
        String s = "<｜tool▁calls▁begin｜><｜tool▁call▁begin｜>function<｜tool▁sep｜>AgentOutput\n" +
                "```json\n" +
                "{\n" +
                "  \"currentState\": {\n" +
                "    \"evaluationPreviousGoal\": \"Success - Successfully extracted 8 VPS providers with rating 8 from the review page\",\n" +
                "    \"memory\": \"Found 8 VPS providers with rating=8: 1. ZgoCloud, 2. WePC, 3. 六六云, 4. Evoxt, 5. RackNerd, 6. ByteVirt, 7. Tokyonline, 8. Back Waves. Need to visit each provider's page (0/8 done), randomly check 5 VPS products from each, filter for annual price <1000 and tier E1-E3, then create MD table. Currently at step 2/20.\",\n" +
                "    \"nextGoal\": \"Visit the first VPS provider page (ZgoCloud) at /review/zgo to check their VPS products\"\n" +
                "  },\n" +
                "  \"action\": [\n" +
                "    {\n" +
                "      \"go_to_url\": {\n" +
                "        \"url\": \"https://digvps.com/review/zgo\"\n" +
                "      }\n" +
                "    }\n" +
                "  ]\n" +
                "}\n" +
                "```<｜tool▁call▁end｜><｜tool▁calls▁end｜>";
        String start = "```json";
        s = s.substring(s.indexOf(start) + start.length());
        String end = "```";
        s = s.substring(0, s.lastIndexOf(end));
        log.info("***" + s + "***");

    }
}
