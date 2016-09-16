package org.sultan.Messenger.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sultan.Messenger.dataBase.DatabaseClass;
import org.sultan.Messenger.model.Profile;

public class ProfileService {

	private Map<String, Profile> profiles=DatabaseClass.getProfiles();
	
	public ProfileService(){
		profiles.put("Sultan", new Profile("Sultan","Mohamed"));
	}
	public List<Profile> getAllProfiles(){
		return new ArrayList<Profile>(profiles.values());
		
	}
	 
	public Profile updateProfile(Profile newProfile){
		profiles.put(newProfile.getProfileName(), newProfile);
		return profiles.get(newProfile.getProfileName());
	}
	
	public String deleteProfile(Profile newProfile){
//		profiles
		return "X";
	}
	
	public Profile getprofile(String profileName){
		return profiles.get(profileName);
		
	}
}
