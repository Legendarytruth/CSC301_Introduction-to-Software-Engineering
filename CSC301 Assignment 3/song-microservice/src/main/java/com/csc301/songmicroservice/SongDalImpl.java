package com.csc301.songmicroservice;

import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

@Repository
public class SongDalImpl implements SongDal {

	private final MongoTemplate db;

	@Autowired
	public SongDalImpl(MongoTemplate mongoTemplate) {
		this.db = mongoTemplate;
	}

	@Override
	public DbQueryStatus addSong(Song songToAdd) {
		// TODO Auto-generated method stub
		ObjectId objectId = null;
		DbQueryStatus dbQueryStatus = new DbQueryStatus("Error in getting Song", DbQueryExecResult.QUERY_ERROR_GENERIC);
		Document document = new Document();
		document.append("songName", songToAdd.getSongName());
		document.append("songArtistFullName", songToAdd.getSongArtistFullName());
		document.append("songAlbum", songToAdd.getSongAlbum());
		document.append("songAmountFavourites", songToAdd.getSongAmountFavourites());
		try {
			//db.getCollection("songs").insertOne(document);
			db.insert(document,"songs");
			objectId = (ObjectId) document.getObjectId("_id");
			songToAdd.setId(objectId);
			dbQueryStatus = new DbQueryStatus(songToAdd.toString(), DbQueryExecResult.QUERY_OK);
		}catch(Exception e){
			dbQueryStatus = new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_GENERIC);
		}
		return dbQueryStatus;
	}

	@Override
	public DbQueryStatus findSongById(String songId) {
		// TODO Auto-generated method stub
		DbQueryStatus dbQueryStatus = new DbQueryStatus("Error in getting songid", DbQueryExecResult.QUERY_ERROR_GENERIC);
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(songId));
		List<Song> song = db.find(query, Song.class);
		if (song.isEmpty()) {
			dbQueryStatus = new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}else {
			dbQueryStatus = new DbQueryStatus(song.get(0).toString(), DbQueryExecResult.QUERY_OK);
		}
		return dbQueryStatus;
	}

	@Override
	public DbQueryStatus getSongTitleById(String songId) {
		// TODO Auto-generated method stub
		DbQueryStatus dbQueryStatus = new DbQueryStatus("Error in getting songid", DbQueryExecResult.QUERY_ERROR_GENERIC);
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(songId));
		List<Song> song = db.find(query, Song.class);
		if (song.isEmpty()) {
			dbQueryStatus = new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
		}else {
			dbQueryStatus = new DbQueryStatus(song.get(0).getSongName(), DbQueryExecResult.QUERY_OK);
		}
		return dbQueryStatus;
	}

	@Override
	public DbQueryStatus deleteSongById(String songId) { 
		// TODO Auto-generated method stub
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(songId));
		List<Song> song = db.find(query, Song.class);
		if (song.isEmpty()) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("OK", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			return dbQueryStatus;
		}else {
		db.findAndRemove(query, Song.class);
		DbQueryStatus dbQueryStatus = new DbQueryStatus("OK", DbQueryExecResult.QUERY_OK);
		return dbQueryStatus;
		}
	}

	@Override
	public DbQueryStatus updateSongFavouritesCount(String songId, boolean shouldDecrement) {
		// TODO Auto-generated method stub
		Query query = new Query();
		query.addCriteria(Criteria.where("_id").is(songId));
		List<Song> songlist = db.find(query, Song.class);
		if (songlist.isEmpty()) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("Song does not exist", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			return dbQueryStatus;
		}else {
		Update update = new Update();
		Song song = db.findOne(query, Song.class);
		
		if (song.getSongAmountFavourites() < 0) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("The song favourites is already at 0.", DbQueryExecResult.QUERY_ERROR_GENERIC);
			return dbQueryStatus;
		}else {
		if (shouldDecrement) {
			update.set("songAmountFavourites", song.getSongAmountFavourites()-1);
			db.updateFirst(query, update, Song.class);
		}else {
			update.set("songAmountFavourites", song.getSongAmountFavourites()+1);
			db.updateFirst(query, update, Song.class);
		}
		DbQueryStatus dbquerystatus = new DbQueryStatus ("OK", DbQueryExecResult.QUERY_OK);
		return dbquerystatus;
		}}
	}
}