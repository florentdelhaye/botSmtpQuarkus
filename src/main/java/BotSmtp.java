import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.vertx.core.Vertx;
import io.vertx.core.net.NetSocket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@QuarkusMain
public class BotSmtp {
    public static void main(String... args) {
        Quarkus.run(MyApp.class, args);
    }

    public static class MyApp implements QuarkusApplication {

        private static int numberOfConnections = 0;

        private static HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .build();

        @Override
        public int run(String... args) throws Exception {

            Vertx vertx = Vertx.vertx();

            vertx.createNetServer()
                    .connectHandler(MyApp::handleNewClient)
                    .listen(1967);

            //vertx.setPeriodic(1000, id -> System.out.println(howMany()));

            Quarkus.waitForExit();
            return 0;
        }

        private static void handleNewClient(NetSocket socket) {
            numberOfConnections++;
            socket.write("220 smtp.----.---- SMTP Ready\n");

            final StringBuilder dataStringBuilder = new StringBuilder();
            final List<String> to = new ArrayList<>();
            final String[] from = new String[1];

            socket.handler(command -> {

                if (command.toString().contains("EHLO")) {
                    socket.write("250-smtp.----.----\n");
                    socket.write("250-PIPELINING\n");
                    socket.write("250 8BITMIME\n");
                } else if (command.toString().contains("MAIL FROM:")) {
                    from[0] = command.toString();
                    socket.write("250 Sender ok\n");
                } else if (command.toString().contains("RCPT TO:")) {
                    to.add(command.toString());
                    if (command.toString().contains("+bounce")) {
                        socket.write("550 5.1.1 Mailbox\n");
                    } else {
                        socket.write("250 Recipient ok\n");
                    }
                } else if (command.toString().contains("DATA")) {
                    socket.write("354 End data with <CR><LF>.<CR><LF>\n");
                } else if (command.toString().contains("RSET")) {
                    to.clear();
                    dataStringBuilder.delete(0, dataStringBuilder.length());
                    socket.write("250 Ok\n");
                } else if (command.toString().contains("QUIT")) {
                    socket.write("221 Closing connection\n").compose(unused -> socket.close());
                } else {
                    dataStringBuilder.append(command.toString());
                    if (dataStringBuilder.substring(dataStringBuilder.length() - 5).equals("\r\n.\r\n")) {
                        socket.write("250 Ok\n");
                        receivedMessage(from[0], to, dataStringBuilder.toString());
                    }
                }
            });

            socket.closeHandler(unused -> {
                numberOfConnections--;
                try {
                    generateComportemental(to, dataStringBuilder.toString());
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        private static String howMany() {
            return "We now have " + numberOfConnections + " connections";
        }

        private static void receivedMessage(String from, List<String> to, String data) {
            System.out.println("Received message");
            System.out.println("From : " + from);
            System.out.println("To : " + to.toString());
            System.out.println("Data : " + data);
        }

        private static void generateComportemental(List<String> rcps, String body) throws URISyntaxException {

            body = body.replaceAll("<", " ");
            body = body.replaceAll("/>", " ");
            body = body.replaceAll(">", " ");
            List<String> words = Arrays.asList(body.split(" "));

            Stream<String> srcUrl = words.stream()
                    .filter(s -> s.length() >= 6)
                    .filter(s -> s.substring(0, 4).equals("src="))
                    .map(s -> s.substring(5, s.length() - 1));

            Stream<String> hrefUrl = words.stream()
                    .filter(s -> s.length() >= 7)
                    .filter(s -> s.substring(0, 5).equals("href="))
                    .map(s -> s.substring(6, s.length() - 1));

            List<String> urls = Stream.concat(srcUrl, hrefUrl).collect(Collectors.toList());

            for (String rcp : rcps) {
                if (rcp.contains("+open")) {
                    Optional<String> first = urls.stream().filter(s -> s.contains("/o/")).findFirst();
                    if (first.isPresent()) {
                        httpClient.sendAsync(HttpRequest.newBuilder()
                                        .GET()
                                        .uri(new URI(first.get()))
                                        .build(),
                                HttpResponse.BodyHandlers.discarding());
                    }
                }
                if (rcp.contains("+click")) {
                    Optional<String> first = urls.stream().filter(s -> s.contains("/c/")).findFirst();
                    if (first.isPresent()) {
                        httpClient.sendAsync(HttpRequest.newBuilder()
                                        .GET()
                                        .uri(new URI(first.get()))
                                        .build()
                                , HttpResponse.BodyHandlers.discarding());
                    }
                }
            }

        }

    }

}


