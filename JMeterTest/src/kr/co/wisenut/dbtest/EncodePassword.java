package kr.co.wisenut.dbtest;

import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

public class EncodePassword {

	public static void main(String[] args) {
		ShaPasswordEncoder passwordEncoder = new ShaPasswordEncoder();
		
		String newEncryptedPassword = passwordEncoder.encodePassword("kbstea@%)", null);
		
		System.out.println("New password : " + newEncryptedPassword);

	}

}
