package com.example.rentalmanager.user.domain.repository;

import com.example.rentalmanager.user.domain.aggregate.User;
import com.example.rentalmanager.user.domain.valueobject.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> save(User user);
    Mono<User> findById(UserId id);
    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
    Flux<User> findAll();
    Mono<Boolean> existsByUsername(String username);
    Mono<Boolean> existsByEmail(String email);
}
