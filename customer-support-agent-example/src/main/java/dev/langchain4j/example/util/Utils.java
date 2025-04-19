package dev.langchain4j.example.util.dom.history_tree_processor;

import dev.langchain4j.example.entity.ParseResult;

import java.net.URI;
import java.net.URLDecoder;

public class Utils {
    public static ParseResult urlparse(String url) throws Exception {
        URI uri = new URI(url);

        // 基础部分解析
        String scheme = uri.getScheme();
        String netloc = uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
        String rawPath = uri.getRawPath();
        String query = uri.getRawQuery();
        String fragment = uri.getFragment();

        // 分割path和params
        String[] pathParts = rawPath.split(";", 2);
        String path = pathParts.length > 0 ? pathParts[0] : "";
        String params = pathParts.length > 1 ? pathParts[1] : "";

        // 解码特殊字符
        path = URLDecoder.decode(path, "UTF-8");
        params = URLDecoder.decode(params, "UTF-8");
        query = URLDecoder.decode(query, "UTF-8");

        return new ParseResult(scheme, netloc, path, params, query, fragment);
    }

}
