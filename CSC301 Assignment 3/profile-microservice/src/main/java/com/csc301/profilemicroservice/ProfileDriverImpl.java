package com.csc301.profilemicroservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;

import org.springframework.stereotype.Repository;

import com.csc301.songmicroservice.DbQueryExecResult;
import com.csc301.songmicroservice.DbQueryStatus;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.Call;

import org.neo4j.driver.v1.Transaction;

@Repository
public class ProfileDriverImpl implements ProfileDriver {

	Driver driver = ProfileMicroserviceApplication.driver;
	OkHttpClient client = new OkHttpClient();

	public static void InitProfileDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.userName)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT exists(nProfile.password)";
				trans.run(queryStr);

				queryStr = "CREATE CONSTRAINT ON (nProfile:profile) ASSERT nProfile.userName IS UNIQUE";
				trans.run(queryStr);

				trans.success();
			}
			session.close();
		}
	}
	
	@Override
	public DbQueryStatus createUserProfile(String userName, String fullName, String password) {
		//String res = "CREATE (:profile {userName:\'"+userName+"\", fullName: \""+fullName+"\", password: \""+password+"\"});";
		String playlist = userName+"_favouriteplaylist";
        String res = "CREATE ("+userName+":profile {userName:\""+userName+"\", fullName: \""+fullName+"\", password: \""+password+"\"})-[r:created]->("+playlist+":playlist {plName:\""+playlist+"\"});";
		Session session = driver.session();
		Transaction transaction = session.beginTransaction();
		StatementResult statementResult = transaction.run(res);
		transaction.success();
		session.close();
		DbQueryStatus dbQueryStatus = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return dbQueryStatus;
	}

	@Override
	public DbQueryStatus followFriend(String userName, String frndUserName) {
		Session session = driver.session();
		Transaction transaction = session.beginTransaction();
		String res2 = "MATCH (a:profile {userName:\""+userName+"\"})-[r:follows]-(b:profile {userName:\""+frndUserName+"\"}) Return true";
		StatementResult statementResult2 = transaction.run(res2);
		transaction.success();
		if(statementResult2.hasNext()) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("User already follows the other person", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return dbQueryStatus;
		}
		String res = "MATCH (a:profile), (b:profile) WHERE a.userName=\""+userName+"\" AND b.userName=\""+frndUserName+"\" Create" +
                "(a)-[r:follows]->(b) RETURN r";
		StatementResult statementResult = transaction.run(res);
		transaction.success();
		session.close();
		if(statementResult.hasNext()) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			return dbQueryStatus;
			
		}else {
		DbQueryStatus dbQueryStatus = new DbQueryStatus("Username or friendusername not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		return dbQueryStatus;
		}
	}

	@Override
	public DbQueryStatus unfollowFriend(String userName, String frndUserName) {
		String res = "MATCH (a:profile {userName:\""+userName+"\"})-[r:follows]-(b:profile {userName:\""+frndUserName+"\"}) DELETE r RETURN true;";
		Session session = driver.session();
		Transaction transaction = session.beginTransaction();
		StatementResult statementResult = transaction.run(res);
		transaction.success();
		session.close();
		if(statementResult.hasNext()) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			return dbQueryStatus;
			
		}else {
		DbQueryStatus dbQueryStatus = new DbQueryStatus("Username or friendusername not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		return dbQueryStatus;
		}
		
	}

	@Override
	public DbQueryStatus getAllSongFriendsLike(String userName) {
		String resFinal = "{data\": {";
		StatementResult resFriend = driver.session().run("MATCH (a:profile {userName: \""+userName+"\"})-[:follows]-(m:profile) RETURN m.userName");
		
		for (Record name: resFriend.list()) {
			
			resFinal += "	\""+name+"\": [";
			StatementResult resSong = driver.session().run("MATCH (a:profile {userName: \""+name+"\"})-[:created]-(m:playlist)-[:includes]-(n:song)  RETURN n.songId");
			
			for(Record song: resSong.list()) {
				String songName = "{}";
				Request songRequest = new Request.Builder().url("http://localhost:3001" + "/getSongTitleById/" + song.toString()).build();
				Response songResponse = null;
				Call call = client.newCall(songRequest);
				try {
					songResponse = call.execute();
					songName = songResponse.body().string();
					System.out.println(songResponse.toString());
					System.out.println(song);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					System.out.println("error");
				}
				
				resFinal += "\""+songName+"\"";
			}
			resFinal += "],";
		}
		
		resFinal = resFinal.substring(0, resFinal.length() - 1);
		resFinal += "}";
		DbQueryStatus dbQueryStatus = new DbQueryStatus(resFinal, DbQueryExecResult.QUERY_OK);
		return dbQueryStatus;
	}
}
