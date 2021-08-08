package com.smart.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.smart.dao.UserRepository;
import com.smart.entities.User;

public class CustomDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepository userrepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		//fetching user from database
		User user = userrepository.getUserByUserName(username);
		if(user == null) {
			throw new UsernameNotFoundException("couldn't found user");
		}
		
		CustomUserDetails customUserDetail = new CustomUserDetails(user);
		
		return customUserDetail;
	}

}
