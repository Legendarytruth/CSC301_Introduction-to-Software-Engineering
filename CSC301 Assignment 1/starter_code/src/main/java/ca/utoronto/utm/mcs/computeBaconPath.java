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

public class computeBaconPath implements HttpHandler
{
    public static Driver driver;
    public computeBaconPath(Driver driv) {
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

    public List<String> combine2(StatementResult sr){
        List<String> parts = new ArrayList<String>();
        List<String> lst = new ArrayList<String>();
        while (sr.hasNext()){
            parts.add(sr.next().get(0).asString());
        }
        for(int i = 0; i < parts.size()-1; i++){
            if (i%2 == 0){
                lst.add("{ \"actorId\":\"" +  parts.get(i) + "\", \"movieId\": \""+ parts.get(i+1)+ "\" }");
            }else{
                lst.add("{ \"actorId\":\"" +  parts.get(i+1) + "\", \"movieId\": \""+ parts.get(i)+ "\" }");
            }
        }
        //System.out.println(parts);
        //System.out.println(lst);
        return lst;
    }
    public void handleGet(HttpExchange r) throws IOException, JSONException {
        String body = convert(r.getRequestBody());
        JSONObject deserialized = new JSONObject(body);
        String actorId = "";
        String response = "";
        if (deserialized.has("actorId")) {
            actorId = deserialized.getString("actorId");
        }
        //String nactorId = Utils.actoridchanger(actorId);
        
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
        }else if(actorId.equals("nm0000102")){
            response = "{\"baconNumber\": \"0\", \"baconPath\": [{\"actorId\": \"nm0000102\", \"movieId\": \"tt0087277\"}]}";
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }else{
            String res = "MATCH p=shortestPath((bacon:actor {id:\'nm0000102\'})-[*]-(B:actor {id:\'"+actorId +"\'}))Unwind nodes(p) as n RETURN n.id";
            StatementResult sr = t.run(res);
            List<String> lst = combine2(sr);
            //System.out.println(sr.hasNext());
            t.success();
            s.close();

            if(lst.isEmpty()){
                //List lst2 = sr.next().get(0).asList();
                //Integer.parseInt(lst2.get(0).toString())/2;
                //System.out.println(lst2.get(1));
                response = "{\"baconNumber\": \"undefined\", \"baconPath\": \" undefined \"}";
            }else{
                int num = lst.size()/2;
                response = "{\"baconNumber\": \"" + num + "\", \"baconPath\": \""+ lst +"\"}";
            }
            r.sendResponseHeaders(200, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }}

