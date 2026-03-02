package com.psy;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PsyRepository extends MongoRepository<PSession, String> {
}