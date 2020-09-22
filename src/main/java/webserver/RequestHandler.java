package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import java.nio.file.Files;
import java.util.Map;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequestUtils;

public class RequestHandler extends Thread {
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try {
            BufferedReader bf = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = bf.readLine();;

            String[] tokens = line.split(" ");
            int urlIndex = 1;
            String url = tokens[urlIndex];
            log.debug("##### url: {}", url);

            if (url.contains("?")) {
                String[] urlAndQueryString = url.split("\\?");
                Map<String,String> queryStringMap = HttpRequestUtils.parseQueryString(urlAndQueryString[1]);

                log.debug("##### map: {}", queryStringMap.get("userId"));
                log.debug("##### map: {}", queryStringMap.get("password"));
                log.debug("##### map: {}", queryStringMap.get("name"));
                log.debug("##### map: {}", queryStringMap.get("email"));

                String userId = queryStringMap.get("userId");
                String password = queryStringMap.get("password");
                String name = queryStringMap.get("name");
                String email = queryStringMap.get("email");
                User user = new User(userId, password, name, email);
                log.debug("##### user: {}", user.toString());
            }

//            byte[] body = Files.readAllBytes(new File("./webapp" + url).toPath());
//            log.debug("##### byte body: {}", body);

            while (!"".equals(line)) {
                if (line == null) {
                    System.out.println("line이 null이예요!!");
                    return;
                }
                log.debug("##### {}", line);
                line = bf.readLine();
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = "Hello World".getBytes();



            response200Header(dos, body.length);
            responseBody(dos, body);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
