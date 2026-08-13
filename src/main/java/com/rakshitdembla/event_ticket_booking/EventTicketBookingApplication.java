package com.rakshitdembla.event_ticket_booking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EventTicketBookingApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventTicketBookingApplication.class, args);
	}

}
