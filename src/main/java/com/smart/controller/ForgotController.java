package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	Random random = new Random();
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPassword;
	
	
	
	
	//open email id form
	@GetMapping("/forgot")
	public String openEmailForm(Model m) {
		//m.addAttribute("title", "forgot password | Smart Phonebook Manager");
		return "forgot_email_form";
	}
	
	//
	/**
	 * @param email
	 * @param session
	 * @return
	 */
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {
		
		
		//otp generating
		
		int otp = random.nextInt(999999);
		System.out.println("OTP: "+otp);
		
		//OTP sending 
		String subject = "OTP from SPM";
		String message = ""
				+"<div style='border: 1px solid red; padding: 20px'>"
				+ "<h1>"
				+ "OTP is : "
				+ "<b>"+ otp
				+ "</b>"
				+ "</h1>"
				+ "</div>";
		String to = email;
		
		boolean flag = this.emailService.sendEmail(subject, message, to);
		if(flag) {
			session.setAttribute("otpByEmail", otp);
			session.setAttribute("email", email);
			return "verify_otp";
		}else {		
			session.setAttribute("message", new Message("this email is not registered !! ", "danger"));
			return "forgot_email_form";
		}
	}
	
	//verify otp handler
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp , HttpSession session) {
		int myOtp = (int)session.getAttribute("otpByEmail");
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
		
		if(myOtp == otp) {
			
			if(user == null) {
				session.setAttribute("message", new Message("User does not exist with this email!!", "danger"));
				return "forgot_email_form";
			}
			
			
			return "password_change_form";
		}else {
			session.setAttribute("message", new Message( "You have entered Wrong OTP!! Please check..", "danger"));
			return "verify_otp";
		}		
	}
	
	//change password handler
	
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newPassword") String newpassword, HttpSession session) {
		
		String email = (String)session.getAttribute("email");
		User user = this.userRepository.getUserByUserName(email);
	
		user.setPassword(this.bCryptPassword.encode(newpassword));
		this.userRepository.save(user);
		return "redirect:/signin?change=Password changed successfully..";
	}
	
	
	
	
	
	
	
	
}

