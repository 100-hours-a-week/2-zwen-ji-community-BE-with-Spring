package com.example.project;

import com.example.project.domain.Comment;
import com.example.project.domain.Liked;
import com.example.project.domain.Post;
import com.example.project.domain.User;
import com.example.project.repository.CommentRepository;
import com.example.project.repository.LikedRepository;
import com.example.project.repository.PostRepository;
import com.example.project.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	@Transactional
	CommandLineRunner initDatabase(UserRepository userRepository,
								   PostRepository postRepository,
								   CommentRepository commentRepository,
								   LikedRepository likedRepository) {
		return args -> {

		};
	}
}
