/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.commons.mongodb.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import uk.ac.ebi.eva.commons.mongodb.entities.AnnotationMetadataMongo;

import java.util.List;

/**
 * Spring MongoRepository for querying collection of AnnotationMetadataMongo documents
 */
@Repository
public interface AnnotationMetadataRepository extends MongoRepository<AnnotationMetadataMongo, String> {

    /**
     * Query for all AnnotationMetadataMongo from collection, ordered first by cache version descending and then vep version
     * descending.
     *
     * @return List of AnnotationMetadataMongo objects
     */
    List<AnnotationMetadataMongo> findAllByOrderByCacheVersionDescVepVersionDesc();

    List<AnnotationMetadataMongo> findByDefaultVersionTrue();

    List<AnnotationMetadataMongo> findByCacheVersionAndVepVersion(String cacheVersion, String vepVersion);

}
