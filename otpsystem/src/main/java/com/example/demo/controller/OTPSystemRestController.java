package com.example.demo.controller;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.OTPSystem;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@RestController
public class OTPSystemRestController {
	
	//store OTP data for each mobile number
	private Map<String, OTPSystem> otp_data = new HashMap<>(); 
	private final static String ACCOUNT_SID="ACdf334b7d5a6a8f6126bb7ba49f19f5c7";
	private final static String AUTH_ID="f719bd35859a3a504d9784fd858e34eb";
	
	
	//initialize the Twilio Instance
	static {
		Twilio.init(ACCOUNT_SID,AUTH_ID);
	}
	/*****************************************************************************/
	//API to send OTP to a particular number
	/*****************************************************************************/
	
	@RequestMapping(value="/mobilenumbers/{mobilenumber}/otp", method=RequestMethod.POST)
	//the return type of this method is entity object
	public ResponseEntity<Object> sendOTP(@PathVariable("mobilenumber")String mobilenumber){
		Random random = new Random();
		OTPSystem otpSystem = new OTPSystem();
		otpSystem.setOtp(String.valueOf((int)(random.nextInt(10000))));
		System.out.println((int)(random.nextInt(10000)));
		otpSystem.setMobilenumber(mobilenumber);
		otpSystem.setExpirytime(System.currentTimeMillis()+20000); //20 seconds
		
		otp_data.put(mobilenumber, otpSystem);
		
		//Message.creator().create() to sent SMS
		//From mobilenum, To mobilenum, TextMsg
		
		Message.creator(new PhoneNumber("+917470469048"), new PhoneNumber("+12176155861"), "Your OTP is "+	otpSystem.getOtp()).create();
		return new ResponseEntity<>("OTP Sent Successfully", HttpStatus.OK);
	}
	
	/*****************************************************************************/
	//API to validate OTP against a particular number
	/*****************************************************************************/
	
	@RequestMapping(value="/mobilenumbers/{mobilenumber}/otp", method=RequestMethod.PUT)
	public ResponseEntity<Object> verifyOTP(@PathVariable("mobilenumber")String mobilenumber, @RequestBody OTPSystem RequestBodyOTPSystem){
		
		if(RequestBodyOTPSystem.getOtp()==null || RequestBodyOTPSystem.getOtp().trim().length()<=0)
		{
			return new ResponseEntity<>("Please provide valid OTP", HttpStatus.BAD_REQUEST);	
		}
		
		if(otp_data.containsKey(mobilenumber))
		{
			OTPSystem otpSystem= otp_data.get(mobilenumber);
			if(otpSystem!=null)
			{
				if(otpSystem.getExpirytime()>=System.currentTimeMillis())
				{
					if(RequestBodyOTPSystem.getOtp().equals(otpSystem.getOtp()))
					{	
						otp_data.remove(mobilenumber); //remove once verified
						return new ResponseEntity<>("OTP Verified Successfully", HttpStatus.OK);	
					}
					return new ResponseEntity<>("OTP Not verified", HttpStatus.BAD_REQUEST);	
				}
				else
					return new ResponseEntity<>("OTP Expired", HttpStatus.BAD_REQUEST);	
			}
			else
				return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
		}
		return new ResponseEntity<>("Mobile Number not found", HttpStatus.NOT_FOUND);
	}
	
}
