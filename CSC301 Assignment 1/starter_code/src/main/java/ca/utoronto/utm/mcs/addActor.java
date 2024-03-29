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

public class addActor implements HttpHandler
{
    public static Driver driver;
    public addActor(Driver driv) {
    	driver = driv;
    }

    public void handle(HttpExchange r) {
        try {
            if (r.getRequestMethod().equals("PUT"))
                handlePut(r);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    
    public static String convert(InputStream inputStream) throws IOException {

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            return br.lines().collect(Collectors.joining(System.lineSeparator()));
        }
    }

    public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        String name = "";
        String actorId = "";
        String response = "";
        if (deserialized.has("name")) {
            name = deserialized.getString("name");
        }if (deserialized.has("actorId")) {
            actorId = deserialized.getString("actorId");
        }
            Session s = driver.session();
            Transaction t = s.beginTransaction();
        StatementResult f = t.run("Match (Actor:actor{id:\'" + actorId + "\'}) return Actor.id");
        t.success();
        if (f.hasNext()) {
            if (f.next().get(0).asString().equals(actorId) || actorId.equals("") || name.equals("")) {
                response = "BAD REQUEST";
                r.sendResponseHeaders(400, response.length());
                //System.out.println("400");
                OutputStream os = r.getResponseBody();
                os.write(response.getBytes());
                //System.out.println(f.next().get(0).asString().equals(actorId));
                os.close();
            }
        }else{
            String res = "CREATE(:actor {id: \"" + actorId + "\", Name: \"" + name + "\"});";
            StatementResult sr = t.run(res);
            //System.out.println(sr.hasNext());
            t.success();
            s.close();
            //System.out.println(name + "/" + actorId);
            //String response = name + actorId + "\n";
            response = "OK";
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
}
    }}

