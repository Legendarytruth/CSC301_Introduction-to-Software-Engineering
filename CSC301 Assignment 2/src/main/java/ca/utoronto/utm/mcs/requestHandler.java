package ca.utoronto.utm.mcs;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCursor;
import com.sun.net.httpserver.HttpServer;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import javax.inject.Inject;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;


public class requestHandler implements HttpHandler{

    public static Dagger service;
    private MongoClient client;
    private HttpServer server;
    private MongoCollection collection;
    public requestHandler(Dagger service){
        this.service = service;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        databaseCreator();
        try{
            if(httpExchange.getRequestMethod().equals("PUT")){
                handlePut(httpExchange);
            }else if(httpExchange.getRequestMethod().equals("GET")){
                handleGet(httpExchange);
            }else if(httpExchange.getRequestMethod().equals("DELETE")){
                handleDelete(httpExchange);
            }else{
                String error = "METHOD NOT ALLOWED";
                httpExchange.sendResponseHeaders(405, error.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
        }catch (final Exception e){
            e.printStackTrace();
        }
    }

    public void databaseCreator(){
        this.client = service.getDb();
        this.server = service.getServer();
        this.collection = client.getDatabase("csc301a2").getCollection("posts");
    }


    public Document setDoc(String title, String author, String content, String tags){
        Document doc = new Document();
        doc.put("title", title);
        doc.put("author", author);
        doc.put("content", content);
        doc.put("tags", tags);
        return doc;
    }

    public void handlePut(HttpExchange r) throws IOException, JSONException {
        Document doc;
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialize = new JSONObject(body);

        String error = "";

        if (!(r.getRequestMethod().equals("PUT"))){
            error = "METHOD NOT ALLOWED";
            r.sendResponseHeaders(405, error.length());
            OutputStream os = r.getResponseBody();
            os.write(error.getBytes());
            os.close();
        }
      
        ObjectId id = null;
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
        else{
            error = "BAD REQUEST";
            r.sendResponseHeaders(400, error.length());
            OutputStream os = r.getResponseBody();
            os.write(error.getBytes());
            os.close();
        }
      
        doc = setDoc(title, author, content, tags);
        try {
            collection.insertOne(doc);
            id = (ObjectId) doc.get("_id");
        }catch (Exception e){
            error = "SERVER ERROR";
            r.sendResponseHeaders(500, error.length());
            OutputStream os = r.getResponseBody();
            os.write(error.getBytes());
            os.close();

            e.printStackTrace();
        }
        String response = "{ \"_id\": \"" + id.toString() + "\"}";
        r.sendResponseHeaders(200, response.length());
        OutputStream os = r.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    public void handleGet(HttpExchange r) throws IOException, JSONException{
        String response = "";
        Document doc;
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialize = new JSONObject(body); //need a JSON error thing here too
        String id = "";
        String title = "";
        String error = "";

        if (!(r.getRequestMethod().equals("GET"))){
            error = "METHOD NOT ALLOWED";
            r.sendResponseHeaders(405, error.length());
            OutputStream os = r.getResponseBody();
            os.write(error.getBytes());
            os.close();
        }

        if (deserialize.has("title")){
            title = deserialize.getString("title");
        }
        if (deserialize.has("_id")){
            id = deserialize.getString("_id");
        }

        if (!id.equals("")){
            Document search = new Document();
            ObjectId objectId = new ObjectId(id);
            search.put("_id",objectId);
            MongoCursor<Document> cursor = collection.find(search).cursor();
            if (cursor.hasNext()) {
                response = "[" + cursor.next().toJson() + "]";
                r.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = r.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }else {
                //I think it would be a 404 error here, as id was not found.
                error = "NOT FOUND";
                r.sendResponseHeaders(404, error.length());
                OutputStream os = r.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
        }else if (!title.equals("")){
            List<String> fullresponse = new ArrayList();
            Document search = new Document();
            search.put("title", new BasicDBObject("$regex", title));

            MongoCursor<Document> cursor = collection.find(search).iterator();
            try {
                if (cursor.hasNext()){
                while (cursor.hasNext()) {
                    fullresponse.add(cursor.next().toJson());
                    //System.out.println(cursor.next().toJson());
                }}
            }
            catch(Exception e){
                error = "SERVER ERROR";
                r.sendResponseHeaders(500, error.length());
                OutputStream os = r.getResponseBody();
                os.write(error.getBytes());
                os.close();
                e.printStackTrace();
            }
            if (fullresponse.isEmpty()){
                error = "NOT FOUND";
                r.sendResponseHeaders(404, error.length());
                OutputStream os = r.getResponseBody();
                os.write(error.getBytes());
                os.close();
                //error message here I think or maybe not needed
            }else{
                r.sendResponseHeaders(200, fullresponse.toString().getBytes().length);
                OutputStream os = r.getResponseBody();
                os.write(fullresponse.toString().getBytes());
                os.close();
            }
        }else{
            //maybe error??????? as both id, and title are ""
            error = "BAD REQUEST";
            r.sendResponseHeaders(400, error.length());
            OutputStream os = r.getResponseBody();
            os.write(error.getBytes());
            os.close();
        }
    }

    public void handleDelete(HttpExchange r) throws IOException, JSONException{
        String body = Utils.convert(r.getRequestBody());
        JSONObject deserialize = new JSONObject(body); //need a JSON error thing here too
        String id = "";
        String error = "";

        if (!(r.getRequestMethod().equals("DELETE"))){
            error = "METHOD NOT ALLOWED";
            r.sendResponseHeaders(405, error.length());
            OutputStream os = r.getResponseBody();
            os.write(error.getBytes());
            os.close();
        }

        if (deserialize.has("_id")){
            id = deserialize.getString("_id");
        }
        if (id == ""){
            error = "BAD REQUEST";
            r.sendResponseHeaders(400, error.length());
            OutputStream os = r.getResponseBody();
            os.write(error.getBytes());
            os.close();
          
        }else{
            String res = "";
            Document remove = new Document();
            ObjectId objectId = new ObjectId(id);
            remove.put("_id", objectId);
            MongoCursor<Document> cursor = collection.find(remove).iterator();
            if  (!cursor.hasNext()) {
                error = "NOT FOUND";
                r.sendResponseHeaders(404, error.length());
                OutputStream os = r.getResponseBody();
                os.write(error.getBytes());
                os.close();
            }
            try {
                collection.deleteOne(remove);
            }
            catch(Exception e){
                error = "SERVER ERROR";
                r.sendResponseHeaders(500, error.length());
                OutputStream os = r.getResponseBody();
                os.write(error.getBytes());
                os.close();
                e.printStackTrace();
            }

            r.sendResponseHeaders(200, res.length());
            OutputStream os = r.getResponseBody();
            os.write(res.getBytes());
            os.close();
        }

    }


}
