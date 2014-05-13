package benchmark;

import com.m3.curly.HTTP;
import com.m3.curly.Request;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.HttpServer;
import server.handler.GetMethodHandler;

public class SimpleGetBenchmarkTest {

    Logger logger = LoggerFactory.getLogger(SimpleGetBenchmarkTest.class);

    Runnable getRunnable(HttpServer server) {
        final HttpServer _server = server;
        return new Runnable() {
            @Override
            public void run() {
                try {
                    _server.start();
                } catch (Exception e) {
                    logger.debug("Failed to invoke server because {}", e.getMessage(), e);
                }
            }
        };
    }

    @Test
    public void createInstanceEveryTime() throws Exception {
        final HttpServer server = new HttpServer(new GetMethodHandler(), 8888);
        try {
            Runnable runnable = getRunnable(server);
            new Thread(runnable).start();
            Thread.sleep(300L);

            String URL = "http://localhost:8888/";
            int timesToCall = 300;

            {
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < timesToCall; i++) {
                    Request request = new Request(URL);
                    HTTP.get(request);
                    Thread.sleep(10L);
                }
                long endTime = System.currentTimeMillis();
                long millis = endTime - startTime - 10 * timesToCall;
                System.out.println("com.m3.curly 300 GET req : " + millis + " milliseconds");
                Thread.sleep(300L);
            }

            {
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < timesToCall; i++) {
                    HttpClient client = new HttpClient();
                    HttpMethod method = new GetMethod(URL);
                    client.executeMethod(method);
                    Thread.sleep(10L);
                }
                long endTime = System.currentTimeMillis();
                long millis = endTime - startTime - 10 * timesToCall;
                System.out.println("Commons HttpClient 300 GET req : " + millis + " milliseconds");
                Thread.sleep(300L);
            }

            {
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < timesToCall; i++) {
                    HttpClient client = new HttpClient(new MultiThreadedHttpConnectionManager());
                    HttpMethod method = new GetMethod(URL);
                    client.executeMethod(method);
                    Thread.sleep(10L);
                }
                long endTime = System.currentTimeMillis();
                long millis = endTime - startTime - 10 * timesToCall;
                System.out.println("Commons HttpClient(MultiThreaded) 300 GET req : " + millis + " milliseconds");
                Thread.sleep(300L);
            }

        } finally {
            server.stop();
            Thread.sleep(300L);
        }

    }

    @Test
    public void reuseInstance() throws Exception {
        final HttpServer server = new HttpServer(new GetMethodHandler(), 8888);
        try {
            Runnable runnable = getRunnable(server);
            new Thread(runnable).start();
            Thread.sleep(300L);

            String URL = "http://localhost:8888/";
            int timesToCall = 300;

            {
                Request request = new Request(URL);
                long startTime = System.currentTimeMillis();
                for (int i = 0; i < timesToCall; i++) {
                    HTTP.get(request);
                    Thread.sleep(10L);
                }
                long endTime = System.currentTimeMillis();
                long millis = endTime - startTime - 10 * timesToCall;
                System.out.println("com.m3.curly 300 GET req : " + millis + " milliseconds");
                Thread.sleep(300L);
            }

            {
                HttpClient client = new HttpClient();
                long startTime = System.currentTimeMillis();
                HttpMethod method = new GetMethod(URL);
                for (int i = 0; i < timesToCall; i++) {
                    client.executeMethod(method);
                    Thread.sleep(10L);
                }
                long endTime = System.currentTimeMillis();
                long millis = endTime - startTime - 10 * timesToCall;
                System.out.println("Commons HttpClient 300 GET req : " + millis + " milliseconds");
                Thread.sleep(300L);
            }

        } finally {
            server.stop();
            Thread.sleep(100L);
        }
    }
}
