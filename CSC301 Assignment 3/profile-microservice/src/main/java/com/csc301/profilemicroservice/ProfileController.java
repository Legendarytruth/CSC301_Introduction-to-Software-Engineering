package com.csc301.profilemicroservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.csc301.profilemicroservice.Utils;
import com.csc301.songmicroservice.DbQueryExecResult;
import com.csc301.songmicroservice.DbQueryStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/")
public class ProfileController {
	public static final String KEY_USER_NAME = "userName";
	public static final String KEY_USER_FULLNAME = "fullName";
	public static final String KEY_USER_PASSWORD = "password";

	@Autowired
	private final ProfileDriverImpl profileDriver;

	@Autowired
	private final PlaylistDriverImpl playlistDriver;

	OkHttpClient client = new OkHttpClient();

	public ProfileController(ProfileDriverImpl profileDriver, PlaylistDriverImpl playlistDriver) {
		this.profileDriver = profileDriver;
		this.playlistDriver = playlistDriver;
	}

	@RequestMapping(value = "/profile", method = RequestMethod.POST)
	public @ResponseBody Map<String, Object> addProfile(@RequestParam Map<String, String> params,
			HttpServletRequest request) {
		String userName;
		String fullName;
		String password;
		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("POST %s", Utils.getUrl(request)));
		
		userName = params.get("userName");
		fullName = params.get("fullName");
		password = params.get("password");
		if(userName == null || fullName == null || password == null) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("Params are wrong", DbQueryExecResult.QUERY_ERROR_GENERIC);
			response.put("message", dbQueryStatus.getMessage());
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}else {
			DbQueryStatus dbQueryStatus= profileDriver.createUserProfile(userName, fullName, password);
			response.put("message", dbQueryStatus.getMessage());
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
	}

	@RequestMapping(value = "/followFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> followFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus = profileDriver.followFriend(userName, friendUserName);
		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		return response;
	}

	@RequestMapping(value = "/getAllFriendFavouriteSongTitles/{userName}", method = RequestMethod.GET)
	public @ResponseBody Map<String, Object> getAllFriendFavouriteSongTitles(@PathVariable("userName") String userName,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus = profileDriver.getAllSongFriendsLike(userName);
		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		return response;
	}


	@RequestMapping(value = "/unfollowFriend/{userName}/{friendUserName}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unfollowFriend(@PathVariable("userName") String userName,
			@PathVariable("friendUserName") String friendUserName, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		DbQueryStatus dbQueryStatus = profileDriver.unfollowFriend(userName, friendUserName);
		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		return response;
	}

	@RequestMapping(value = "/likeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> likeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		Request request2 = new Request.Builder().url("http://localhost:3001" + "/getSongById/" + songId).method("GET",null).build();
		Call call = client.newCall(request2);
		Response responsefromSong = null;
		String Songservicebody = "{}";
		try {
			responsefromSong = call.execute();
			Songservicebody = responsefromSong.body().string();
			//Songservicebody.contains("OK");
			//System.out.println(Songservicebody.contains("OK"));
			
		}catch(Exception e) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("Server error", DbQueryExecResult.QUERY_ERROR_GENERIC);
			response.put("message", dbQueryStatus.getMessage());
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
		if(Songservicebody.contains("OK")) {
			RequestBody body = RequestBody.create(null, new byte[0]);
			Request request3 = new Request.Builder().url("http://localhost:3001" + "/updateSongFavouritesCount/" + songId + "?shouldDecrement=false").method("PUT",body).build();
			Call call2 = client.newCall(request3);
			Response responsefromSong2 = null;
			String Songservicebody2 = "{}";
			try {
				responsefromSong2 =call2.execute();
				Songservicebody2 = responsefromSong2.body().string();
				//System.out.print(Songservicebody2);
			}catch(Exception e) {
				DbQueryStatus dbQueryStatus = new DbQueryStatus("Error in changing the favourite counter", DbQueryExecResult.QUERY_ERROR_GENERIC);
				response.put("message", dbQueryStatus.getMessage());
				response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
				return response;
			}
		DbQueryStatus dbQueryStatus = playlistDriver.likeSong(userName, songId);
		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		return response;
		}else {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("SongId is missing", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response.put("message", dbQueryStatus.getMessage());
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
			
		}

	}

	@RequestMapping(value = "/unlikeSong/{userName}/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> unlikeSong(@PathVariable("userName") String userName,
			@PathVariable("songId") String songId, HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		Request request2 = new Request.Builder().url("http://localhost:3001" + "/getSongById/" + songId).method("GET",null).build();
		Call call = client.newCall(request2);
		Response responsefromSong = null;
		String Songservicebody = "{}";
		try {
			responsefromSong = call.execute();
			Songservicebody = responsefromSong.body().string();
			
		}catch(Exception e) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("Server error", DbQueryExecResult.QUERY_ERROR_GENERIC);
			response.put("message", dbQueryStatus.getMessage());
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
		if(Songservicebody.contains("OK")) {
			RequestBody body = RequestBody.create(null, new byte[0]);
			Request request3 = new Request.Builder().url("http://localhost:3001" + "/updateSongFavouritesCount/" + songId + "?shouldDecrement=true").method("PUT",body).build();
			Call call2 = client.newCall(request3);
			try {
				call2.execute();
			}catch(Exception e) {
				DbQueryStatus dbQueryStatus = new DbQueryStatus("Error in changing the favourite counter", DbQueryExecResult.QUERY_ERROR_GENERIC);
				response.put("message", dbQueryStatus.getMessage());
				response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
				return response;
			}
			DbQueryStatus dbQueryStatus = playlistDriver.unlikeSong(userName, songId);
		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		return response;
		}else {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("SongId is missing", DbQueryExecResult.QUERY_ERROR_NOT_FOUND);
			response.put("message", dbQueryStatus.getMessage());
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
	}

	@RequestMapping(value = "/deleteAllSongsFromDb/{songId}", method = RequestMethod.PUT)
	public @ResponseBody Map<String, Object> deleteAllSongsFromDb(@PathVariable("songId") String songId,
			HttpServletRequest request) {

		Map<String, Object> response = new HashMap<String, Object>();
		response.put("path", String.format("PUT %s", Utils.getUrl(request)));
		RequestBody body = RequestBody.create(null, new byte[0]);
		Request request2 = new Request.Builder().url("http://localhost:3001" + "/deleteSongById/" + songId).method("DELETE",null).build();
		Call call = client.newCall(request2);
		Response responsefromSong = null;
		String Songservicebody = "{}";
		try {
			responsefromSong = call.execute();
			Songservicebody = responsefromSong.body().string();
			
		}catch(Exception e) {
			DbQueryStatus dbQueryStatus = new DbQueryStatus("Server error", DbQueryExecResult.QUERY_ERROR_GENERIC);
			response.put("message", dbQueryStatus.getMessage());
			response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
			return response;
		}
		DbQueryStatus dbQueryStatus = playlistDriver.deleteSongFromDb(songId);
		response.put("message", dbQueryStatus.getMessage());
		response = Utils.setResponseStatus(response, dbQueryStatus.getdbQueryExecResult(), dbQueryStatus.getData());
		return response;
		
	}
}