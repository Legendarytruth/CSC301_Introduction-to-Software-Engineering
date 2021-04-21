package ca.utoronto.utm.mcs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.stream.Collectors;

import org.json.*;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

import static org.neo4j.driver.v1.Values.parameters;
import java.util.*;

public class computeBaconNumber implements HttpHandler
{
    public static Driver driver;
    public computeBaconNumber(Driver driv) {
    	driver = driv;
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("GET"))
                handleGet(r);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    
    public static String convert(InputStream inputStream) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String body = convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        String actorId = "";
        String response = "";
        if (deserialized.has("actorId")) {
            actorId = deserialized.getString("actorId");
        }
        String nactorId = Utils.actoridchanger(actorId);

        Session s = driver.session();
        Transaction t = s.beginTransaction();
        if(actorId.equals("")) {
            response = "BAD REQUEST";
            r.sendResponseHeaders(400, response.length());
            //System.out.println("400");
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            //System.out.println(f.next().get(0).asString().equals(actorId));
            os.close();
        }else if(actorId.equals("nm0000102")) {
            response = "{\"baconNumber\": \"0\"}";
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
        else{
                String res = "MATCH p=shortestPath((bacon:actor {id:\'nm0000102\'})-[*]-(B:actor {id:\'"+actorId +"\'}))RETURN length(p)";
                StatementResult sr = t.run(res);
                //System.out.println(sr.hasNext());
                t.success();
                s.close();

                if (sr.hasNext()) {
                    int num = sr.next().get(0).asInt() / 2;
                    response = "{\"baconNumber\": \"" + num + "\"}";
                }else{
                    response = "{\"baconNumber\": \"undefined\"}";
                }
                r.sendResponseHeaders(200, response.length());
                OutputStream os = r.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

    }}

