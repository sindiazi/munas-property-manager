package com.example.rentalmanager.user.infrastructure.persistence.adapter;

import com.example.rentalmanager.user.application.port.output.UserPersistencePort;
import com.example.rentalmanager.user.domain.aggregate.User;
import com.example.rentalmanager.user.domain.valueobject.UserId;
import com.example.rentalmanager.user.infrastructure.persistence.entity.UserByEmailEntity;
import com.example.rentalmanager.user.infrastructure.persistence.entity.UserByUsernameEntity;
import com.example.rentalmanager.user.infrastructure.persistence.entity.UserEntity;
import com.example.rentalmanager.user.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.example.rentalmanager.user.infrastructure.persistence.repository.UserByEmailRepository;
import com.example.rentalmanager.user.infrastructure.persistence.repository.UserByUsernameRepository;
import com.example.rentalmanager.user.infrastructure.persistence.repository.UserCassandraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserCassandraRepository  repository;
    private final UserByUsernameRepository byUsernameRepository;
    private final UserByEmailRepository    byEmailRepository;
    private final UserPersistenceMapper    mapper;

    @Override
    public Mono<User> save(User user) {
        UserEntity entity = mapper.toEntity(user);
        // If email changed, delete the stale lookup entry before writing the new one
        return repository.findById(entity.getId())
                .flatMap(old -> {
                    Mono<Void> cleanup = old.getEmail().equals(entity.getEmail())
                            ? Mono.empty()
                            : byEmailRepository.deleteById(old.getEmail());
                    return cleanup.then(saveAll(entity));
                })
                .switchIfEmpty(saveAll(entity));
    }

    @Override public Mono<User>    findById(UserId id)        { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override public Flux<User>    findAll()                  { return repository.findAll().map(mapper::toDomain); }

    @Override
    public Mono<User> findByUsername(String username) {
        return byUsernameRepository.findById(username)
                .flatMap(lookup -> repository.findById(lookup.getUserId()))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return byEmailRepository.findById(email)
                .flatMap(lookup -> repository.findById(lookup.getUserId()))
                .map(mapper::toDomain);
    }

    @Override public Mono<Boolean> existsByUsername(String u) { return byUsernameRepository.existsById(u); }
    @Override public Mono<Boolean> existsByEmail(String e)    { return byEmailRepository.existsById(e); }

    private Mono<User> saveAll(UserEntity entity) {
        return repository.save(entity)
                .flatMap(saved -> byUsernameRepository.save(new UserByUsernameEntity(saved.getUsername(), saved.getId()))
                        .then(byEmailRepository.save(new UserByEmailEntity(saved.getEmail(), saved.getId())))
                        .thenReturn(saved))
                .map(mapper::toDomain);
    }
}
