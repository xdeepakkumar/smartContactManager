package com.smart.controller;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	
	//home page
	@GetMapping("/")
	public String home(Model m) {
		m.addAttribute("title", "Home - Smart Phonebook Manager");
		return "home";
	}
	//about page
	@GetMapping("/about")
	public String about(Model m) {
		m.addAttribute("title", "About - Smart Phonebook Manager");
		return "about";
	}
	//contact 
	@GetMapping("/contact")
	public String contact(Model m) {
		m.addAttribute("title", "Contact - Smart Phonebook Manager");
		return "about";
	}
	//register page
	@GetMapping("/signup")
	public String signup(Model m) {
		m.addAttribute("title", "Register - Smart Phonebook Manager");
		m.addAttribute("user", new User());
		return "signup";
	}
	//user registration handler
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, @RequestParam(value="agreement", defaultValue = "false") Boolean agreement, Model m,BindingResult res , HttpSession session) {
		try {
			if(!agreement) {
				System.out.println("You have not agreed with terms and condfitions.");
				throw new Exception("You have not agreed with terms and condfitions.");
			}
			
			if(res.hasErrors()) 
			{
				System.out.println("Error : "+ res.toString());
				m.addAttribute("user", user);
				return "about";
			}
				
				
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			this.userRepository.save(user);	
			
			m.addAttribute("user", new User());
			session.setAttribute("message", new Message(" Regustration success !! " , "alert-success"));
			return "signup";
			
		} catch (Exception e) {
			e.printStackTrace();
			m.addAttribute("user", user);
			session.setAttribute("message", new Message("it seems username exist !! or ", "alert-danger"));
			return "signup";
		}
		
	}
	
	//handler for custom login
	@GetMapping("/signin")
	public String customLogin(Model model) {
		model.addAttribute("title", "Login - Smart Phnebook Manager");
		return "login";
	}
	
	
	
	
	
	
	
	
	
	
	
}
