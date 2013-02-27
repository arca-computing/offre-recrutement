package recrutement;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;

public class OffreRecrutement implements HttpHandler {

    public static final String REFERENCE = "";

    public static void main(final String[] args) throws Exception {
        HttpServer serveur = HttpServer.create(new InetSocketAddress(2013), 0);
        serveur.createContext("/", new OffreRecrutement());
        serveur.start();
        System.out.println("ça tourne...");
    }

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        String resultat = "BAD REQUEST";
        if ("/details".equals(httpExchange.getRequestURI().getPath())) {
            resultat = getDetails();
        }

        byte[] reponse = resultat.getBytes();
        httpExchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        httpExchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, reponse.length);
        httpExchange.getResponseBody().write(reponse);
        httpExchange.close();
    }

    private String getDetails() throws IOException {
        final String code = getCode();
        final URL url = new URL("http://www.arca-computing.fr/offre-recrutement/" + code);
        final URLConnection connection = url.openConnection();

        final StringBuilder reponse = new StringBuilder();

        final BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            reponse.append(inputLine);
        }
        in.close();

        return reponse.toString();
    }

    private String getCode() {
        String code;
        try {
            code = hash(REFERENCE, REFERENCE.length());
            code = hash(code, 7);
            code = hash(code, 3);
            code = hash(code, 11);
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
        return code;
    }

    private String hash(final String value, int length) throws Exception {
        final MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.update(value.getBytes("UTF-8"), 0, length);
        return convertToHex(digest.digest());
    }

    private String convertToHex(byte[] data) {
        final StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }
}
