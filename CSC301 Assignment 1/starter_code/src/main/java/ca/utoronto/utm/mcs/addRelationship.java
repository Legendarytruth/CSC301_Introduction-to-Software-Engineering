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

public class addRelationship implements HttpHandler
{
    public static Driver driver;
    public addRelationship(Driver driv){
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
        String actorId = "";
        String movieId = "";
        String response = "";
        if (deserialized.has("actorId")) {
            actorId = deserialized.getString("actorId");
        }if (deserialized.has("movieId")) {
            movieId = deserialized.getString("movieId");
        }

        /*
        //String res = "CREATE(:movie {actorId: \""+ actorId + "\", movieId: \""+ movieId +"\"});";
        String res = "MATCH (a:actor),(b:movie) where a.id = \""+ actorId + "\" AND b.id = \"" + movieId + "\" CREATE (a)-[r:TEST]->(b)return type(r);";
        Session s = driver.session();
        Transaction t = s.beginTransaction();
        //s.run("CREATE(:PERSON {id:" + actorId + ", Name:" + name + "});");
        StatementResult sr = t.run(res);
        //System.out.println("sent");
        t.success();
        s.close();

        System.out.println(actorId +"/"+ movieId);
        String response = actorId + movieId + "\n";
        r.sendResponseHeaders(200, response.length());
        OutputStream os = r.getResponseBody();
        os.write(response.getBytes());
        os.close();
        */


        /////////////////////////////////////////////////

        Session s = driver.session();
        Transaction t = s.beginTransaction();
        StatementResult f = t.run("RETURN EXISTS( (:actor {id:\'"+actorId  +"\'})-[:ACTED_IN]-(:movie {id:\'"+movieId +"\'}) )");
        t.success();
        System.out.println(f.next().get(0).asBoolean()); //DOESN'T WORK.
        if (f.hasNext()) {
            if (f.next().get(0).asBoolean() || movieId.equals("") || actorId.equals("")) {
                response = "BAD REQUEST";
                r.sendResponseHeaders(400, response.length());
                //System.out.println("400");
                OutputStream os = r.getResponseBody();
                os.write(response.getBytes());
                //System.out.println(f.next().get(0).asString().equals(movieId));
                os.close();
            }
        }else{
            String res = "MATCH (a:actor),(b:movie) where a.id = \""+ actorId + "\" AND b.id = \"" + movieId + "\" CREATE (a)-[r:ACTED_IN]->(b)return type(r);";
            StatementResult sr = t.run(res);
            //System.out.println(sr.hasNext());
            t.success();
            s.close();
            //System.out.println(actorId + "/" + movieId);
            //String response = name + movieId + "\n";
            response = "OK";
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    }
}

