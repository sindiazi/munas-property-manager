package com.example.rentalmanager.user.infrastructure.persistence.adapter;

import com.example.rentalmanager.user.application.port.output.UserPersistencePort;
import com.example.rentalmanager.user.domain.aggregate.User;
import com.example.rentalmanager.user.domain.valueobject.UserId;
import com.example.rentalmanager.user.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.example.rentalmanager.user.infrastructure.persistence.repository.UserCassandraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserCassandraRepository repository;
    private final UserPersistenceMapper   mapper;

    @Override public Mono<User>    save(User user)              { return repository.save(mapper.toEntity(user)).map(mapper::toDomain); }
    @Override public Mono<User>    findById(UserId id)          { return repository.findById(id.value()).map(mapper::toDomain); }
    @Override public Mono<User>    findByUsername(String u)     { return repository.findByUsername(u).map(mapper::toDomain); }
    @Override public Mono<User>    findByEmail(String e)        { return repository.findByEmail(e).map(mapper::toDomain); }
    @Override public Flux<User>    findAll()                    { return repository.findAll().map(mapper::toDomain); }
    @Override public Mono<Boolean> existsByUsername(String u)   { return repository.existsByUsername(u); }
    @Override public Mono<Boolean> existsByEmail(String e)      { return repository.existsByEmail(e); }
}
