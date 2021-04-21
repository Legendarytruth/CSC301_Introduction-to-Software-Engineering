package com.csc301.profilemicroservice;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.stereotype.Repository;
import org.neo4j.driver.v1.Transaction;

@Repository
public class PlaylistDriverImpl implements PlaylistDriver {

	Driver driver = ProfileMicroserviceApplication.driver;

	public static void InitPlaylistDb() {
		String queryStr;

		try (Session session = ProfileMicroserviceApplication.driver.session()) {
			try (Transaction trans = session.beginTransaction()) {
				queryStr = "CREATE CONSTRAINT ON (nPlaylist:playlist) ASSERT exists(nPlaylist.plName)";
				trans.run(queryStr);
				trans.success();
			}
			session.close();
		}
	}
	

	@Override
	public DbQueryStatus likeSong(String userName, String songId) {
		String playlist = userName+"_favouriteplaylist";
		String res = "MATCH (a:profile) WHERE a.userName = \"" + userName + "\"RETURN true";
		Session session = driver.session();
		Transaction transaction = session.beginTransaction();
		StatementResult statementResult = transaction.run(res);
		transaction.success();
		
		if (statementResult.hasNext()) {
			String res2 = "MATCH (a:song) WHERE a.songId = \""+songId+"\" RETURN true";
			StatementResult statementResult2 = transaction.run(res2);
			transaction.success();
			if(statementResult2.hasNext()) {
				String res3 = "MATCH (a:playlist), (b:song) WHERE a.plName= \"" +playlist+"\" AND b.songId= \""+songId+"\" CREATE (a)-[r:includes]->(b)";
				StatementResult statementResult3 = transaction.run(res3);
				transaction.success();
				session.close();
				DbQueryStatus dbQueryStatus = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
				return dbQueryStatus;	
			}
			String res4 = "MATCH (a:playlist) WHERE a.plName=\"" + playlist + "\"CREATE (a)-[r:includes]->(:song {songId:\""+songId+"\"});";
			StatementResult statementResult4 = transaction.run(res4);
			transaction.success();
			session.close();
			DbQueryStatus dbQueryStatus = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
			return dbQueryStatus;
		}else {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("userName does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			return dbQueryStatus;
		}
	}

	@Override
	public DbQueryStatus unlikeSong(String userName, String songId) {
		String playlist = userName+"_favouriteplaylist";
		String res = "MATCH (a:profile), (b:song) WHERE a.userName = \"" + userName + "\"AND b.songId = \""+songId+"\"RETURN true";
		Session session = driver.session();
		Transaction transaction = session.beginTransaction();
		StatementResult statementResult = transaction.run(res);
		transaction.success();
		if (statementResult.hasNext()) {
			String res2 = "MATCH (a:song), (b:playlist) WHERE a.songId=\""+songId+"\" AND b.plName=\""+playlist+"\" AND (b)-[:includes]->(a) RETURN (b)-[:includes]->(a)";
			StatementResult statementResult2 = transaction.run(res2);
			transaction.success();
			if(statementResult2.hasNext()) {
				String res3 = "MATCH (a:playlist {plName:\""+playlist+"\"})-[r:includes]-(b:song {songId:\""+songId+"\"}) DELETE r RETURN true;";
				StatementResult statementResult3 = transaction.run(res3);
				transaction.success();
				session.close();
				DbQueryStatus dbQueryStatus = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
				return dbQueryStatus;
			}else {
				DbQueryStatus dbQueryStatus = new DbQueryStatus("Relationship not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
				return dbQueryStatus;
			}
		}else {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("userName or songId not found", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			return dbQueryStatus;
		}
		

	}

	@Override
	public DbQueryStatus deleteSongFromDb(String songId) {
		String res = "MATCH (a:song) WHERE a.songId=\""+songId+"\" DETACH DELETE a RETURN true";
		Session session = driver.session();
		Transaction transaction = session.beginTransaction();
		StatementResult statementResult = transaction.run(res);
		transaction.success();
		session.close();
		DbQueryStatus dbQueryStatus = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return dbQueryStatus;
	}
}
