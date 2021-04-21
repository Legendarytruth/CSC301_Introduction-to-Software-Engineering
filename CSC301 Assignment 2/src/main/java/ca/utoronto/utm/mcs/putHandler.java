package ca.utoronto.utm.mcs;


import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.json.*;

import javax.inject.Inject;

public class putHandler {

    private MongoClient client;
    private HttpServer server;
    private MongoDatabase database;
    private MongoCollection collection;
    private BasicDBObject dbObject = new BasicDBObject();

    @Inject
    public void databaseCreator(MongoClient client, HttpServer server){
        this.client = client;
        this.server = server;
        this.database = client.getDatabase("csc301a2");
        this.collection = database.getCollection("posts");
    }

    public void setDbObject(String title, String author, String content, String tags){
        dbObject.put("title", title);
        dbObject.put("author", author);
        dbObject.put("content", content);
        dbObject.put("tags", tags);
    }

    public void handlePut(HttpExchange r) throws IOException, JSONException {
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialize = new JSONObject(body);
        String response = "";
        if (!(r.getRequestMethod().equals("PUT"))){
            response = "METHOD NOT ALLOWED";
            r.sendResponseHeaders(405, response.length());
            OutputStream os = r.getResponseBody();
            os.write(response.getBytes());
            os.close();
            return;
        }

        String id = "";
        String title = "";
        String author = "";
        String content = "";
        String tags = "";

        if (deserialize.has("title") && deserialize.has("author") && deserialize.has("content") && deserialize.has("tags")) {
                title = deserialize.getString("title");
                author = deserialize.getString("author");
                content = deserialize.getString("content");
                tags = deserialize.getString("tags");

        }
        setDbObject(title, author, content, tags);
        try {
            collection.insertOne(dbObject);
            id = (String) dbObject.get("_id");
        }catch (Exception e){
            e.printStackTrace();
        }

        response = "{" + id + "}";
      
        r.sendResponseHeaders(200, response.length());
        OutputStream os = r.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}
