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

public class getActor implements HttpHandler
{
    public static Driver driver;
    public getActor(Driver driv) {
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
    public List<String> movielst = new ArrayList<String>();

    public void combine(StatementResult sr){
        while (sr.hasNext()){
            movielst.add("\""+sr.next().get(0).asString()+"\"");
            //System.out.println(movielst.toString());
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
        //Match (Actor:actor{id:"3645"}) return "{ actorId: "+Actor.id +", name: "+Actor.Name+", movies: "+[Actor.movies]+"}"
        //Match (Actor:actor{id:"3645"}) return "{ \"actorId\": \""+Actor.id +"\", \"name\": \""+Actor.Name+"\", \"movies\":"
        //"Match (Actor:actor{id:\'"+actorId+"\'}) return \"{ actorId: \"+Actor.id +\", name: \"+Actor.Name+\", movies: \"+[Actor.movies]+\"}\"";
        //"Match (Actor:actor{id:\'"+actorId+"\'}) return \"{ "actorId": \""+Actor.id +"\", "name": \""+Actor.Name+\"", "movies": \"";
            Session s = driver.session();
            Transaction t = s.beginTransaction();
        /*StatementResult f = t.run("Match (Actor:actor{id:\'" + actorId + "\'}) return true");
        t.success();
        if (f.hasNext()) {
            if (/*f.next().get(0).asBoolean() || actorId.equals("")) {
                response = "BAD REQUEST";
                r.sendResponseHeaders(400, response.length());
                //System.out.println("400");
                OutputStream os = r.getResponseBody();
                os.write(response.getBytes());
                //System.out.println(f.next().get(0).asString().equals(actorId));
                os.close();
            }
        }else{
            String res = "Match (Actor:actor{id:\'"+actorId+"\'}) return \"{\"+\"actorId: \"+Actor.id+\", name: \"+Actor.Name+\", movies: \"";
            String movies = "MATCH (actor {id:\'"+actorId+"\'})--(movie) RETURN movie.id";
            StatementResult movlist = t.run(movies);
            combine(movlist);
            StatementResult sr = t.run(res);
            //System.out.println(sr.hasNext());
            t.success();
            s.close();
            //String response = name + actorId + "\n";
            response = sr.next().get(0).asString()+movielst+"}";
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();*/

        StatementResult f = t.run("Match (Actor:actor{id:\'" + actorId + "\'}) return true");
        t.success();
        if(actorId.equals("")) {
            response = "BAD REQUEST";
            r.sendResponseHeaders(400, response.length());
            //System.out.println("400");
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            //System.out.println(f.next().get(0).asString().equals(actorId));
            os.close();
        }else if(f.hasNext() && f.next().get(0).asBoolean()){
            String res = "Match (Actor:actor{id:\'"+actorId+"\'}) return Actor.id + \",\" + Actor.Name";
            String movies = "MATCH (actor {id:\'"+actorId+"\'})--(movie) RETURN movie.id";
            StatementResult movlist = t.run(movies);
            combine(movlist);
            StatementResult sr = t.run(res);
            //System.out.println(sr.hasNext());
            t.success();
            s.close();
            //String response = name + actorId + "\n";
            String[] parts = sr.next().get(0).asString().split(",");
            response = "{\"actorId\": \""+ parts[0]+"\", \"name\": "+parts[1] +"\", \"movies\": "+movielst+"}";
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }else{
            response = "BAD REQUEST";
            r.sendResponseHeaders(404, response.length());
            //System.out.println("400");
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            //System.out.println(f.next().get(0).asString().equals(actorId));
            os.close();

        }
    }}

