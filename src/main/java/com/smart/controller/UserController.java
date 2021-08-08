package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	
	//method for adding common data to response
	@ModelAttribute
	public void addCommonData(Model m, Principal principal) {
		String userName = principal.getName();
//		System.out.println("UserName: "+userName);
		
		
		//get the user detail using userNmae(email)
		User user = userRepository.getUserByUserName(userName);
//		System.out.println("user :"+user);
		
		m.addAttribute("user",user);

	}
	
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model m, Principal principal) {
		m.addAttribute("title","User Dashboard - Smart Phonebook Manager");
		return "normal/user_dashboard";
	}
	
	
	//add form handler
	@GetMapping("/add-phonebook")
	public String openAddPhonebookForm(Model m) {
		m.addAttribute("title","Add Phonebook - Smart Phonebook Manager");
		m.addAttribute("contact", new Contact());
		return "normal/add_phonebook_form";
	}
	
	//getting phonebook data
	@PostMapping("/process-contact")
	public String processPhonebook(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file ,Principal principal, HttpSession session) {
		try {
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			//processing and uploading file
			if(file.isEmpty()) {
				//
				System.out.println("file is empty");
				contact.setImage("phonebook.png");
			}else {
				//upload the file to the folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				
				//getiing the file path
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("image has been uploaded");
			}
			
			//adding user id in phonebook
			contact.setUser(user);
			
			user.getContacts().add(contact);
			this.userRepository.save(user);
			
			System.out.println("data added to databse");
			
			//success message print
			session.setAttribute("message", new Message("Successfully added your contact!!", "success"));
			
			System.out.println("Contact : "+contact);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			//error message print
			session.setAttribute("message", new Message("Something went wrong!! try again", "danger"));
			
		}
		return "normal/add_phonebook_form";
	}
	//show Phonebook handler
	//per page = 5 only
	@GetMapping("/show-phonebook/{page}")
	public String showPhonebook(@PathVariable("page") Integer page ,Model m, Principal principal) {
		m.addAttribute("title","Phonebook list - Smart Phonebook Manager");
		
		//contact list
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		//
		Pageable pageable  = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactByUser(user.getId(), pageable);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_phonebook";
	}
	
	//showing the details of phonebook
	@GetMapping("/contact/{cId}")
	public String showPhonebookDetail(@PathVariable("cId") Integer cId , Model m, Principal principal) {
				
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		//
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		
		//checking user for security purpose
		if(user.getId() == contact.getUser().getId()) {
			m.addAttribute("title",contact.getName());
			m.addAttribute("contact",contact);
		}
		
		
		return "normal/phonebook_deatil";
	}
	
	//delete phonebook
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model m, HttpSession session, Principal principal) {
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		//delete phonebook from user
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		
		session.setAttribute("message", new Message("contact deleted successfully", "success"));
		return "redirect:/user/show-phonebook/0";
	}
	
	//open upadate form phonebook
	@PostMapping("/update-phonebook/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId ,Model m) {
		m.addAttribute("title", "update phonebook - Smart Phonebook Manager");
		Contact contact= this.contactRepository.findById(cId).get();
		m.addAttribute("contact",contact);
		return "normal/update_form";
	}
		
	//update the phonebook data
	@RequestMapping(value="/process-update", method = RequestMethod.POST)
	public String phonebookUpdateHandler(@ModelAttribute Contact contact, Model m, @RequestParam("profileImage") MultipartFile file, HttpSession session, Principal principal)  {
		
		try {
			//old contact detail fetching
			Contact oldContactDetail = this.contactRepository.findById(contact.getcId()).get();
			
			//here we checking that user image is changed or not
			if(!file.isEmpty()) { 
				//delete old photo first
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				
				
				//updating image
				//getiing the file path
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
				
				//sending response message
				session.setAttribute("message", new Message("Phonebook updated successfully","success"));
				
				
			}else {
				//if image is not changing the we are just adding old one in new contact
				contact.setImage(oldContactDetail.getImage());
			}
			
			//setting up user id
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			
			this.contactRepository.save(contact);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "redirect:/user/contact/"+contact.getcId();
	}
	
	//user profile handler
	@GetMapping("/profile")
	public String userProfile(Model m) {
		m.addAttribute("title", "user profile - Smart Contact Manager");
		return "normal/profile";
	}
	
	//open settings handler
	@GetMapping("/settings")
	public String openSettings() {
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
		String user = principal.getName();
		User currentUser = this.userRepository.getUserByUserName(user);
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			//update the password
			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("your password updated successfully...", "success"));
		}else {
			//error msg
			session.setAttribute("message", new Message("Your old password is not correct", "danger"));
			return "redirect:/user/settings";		
		}
		
		return "redirect:/user/index";		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
