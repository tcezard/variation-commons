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
package uk.ac.ebi.eva.commons.mongodb.services;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.mongodb.MongoDbConfigurationBuilder;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import uk.ac.ebi.eva.commons.core.models.Region;
import uk.ac.ebi.eva.commons.core.models.VariantType;
import uk.ac.ebi.eva.commons.core.models.ws.VariantSourceEntryWithSampleNames;
import uk.ac.ebi.eva.commons.core.models.ws.VariantWithSamplesAndAnnotation;
import uk.ac.ebi.eva.commons.mongodb.configuration.EvaRepositoriesConfiguration;
import uk.ac.ebi.eva.commons.mongodb.configuration.MongoRepositoryTestConfiguration;
import uk.ac.ebi.eva.commons.mongodb.entities.VariantMongo;
import uk.ac.ebi.eva.commons.mongodb.filter.FilterBuilder;
import uk.ac.ebi.eva.commons.mongodb.filter.VariantRepositoryFilter;
import uk.ac.ebi.eva.commons.mongodb.test.rule.FixSpringMongoDbRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@TestPropertySource("classpath:eva.properties")
@UsingDataSet(locations = {
        "/test-data/variants.json",
        "/test-data/annotations.json",
        "/test-data/files.json",
        "/test-data/annotation_metadata.json"})
@ContextConfiguration(classes = {MongoRepositoryTestConfiguration.class, EvaRepositoriesConfiguration.class})
public class VariantWithSamplesAndAnnotationServiceTest {

    private static final String TEST_DB = "test-db";

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = new FixSpringMongoDbRule(
            MongoDbConfigurationBuilder.mongoDb().databaseName(TEST_DB).build());

    @Autowired
    private VariantWithSamplesAndAnnotationsService service;

    @Test
    public void testFindByRegionsAndComplexFilters() throws AnnotationMetadataNotFoundException {
        Region region = new Region("11", 190062L, 190064L);
        List<Region> regions = new ArrayList<>();
        regions.add(region);

        List<VariantWithSamplesAndAnnotation> variantEntityList = service.findByRegionsAndComplexFilters(
                regions, null, null, null, new PageRequest(0, 10000));

        assertEquals(1, variantEntityList.size());

        for (VariantSourceEntryWithSampleNames variantSourceEntry : variantEntityList.get(0).getSourceEntries()) {
            if (variantSourceEntry.getFileId().equals("ERZX00051")) {
                assertEquals(28, variantSourceEntry.getCohortStats().size());
                assertFalse(variantSourceEntry.getSamplesData().isEmpty());
                Map<String, Map<String, String>> samplesData = variantSourceEntry.getSamplesDataMap();
                assertEquals("0|1", samplesData.get("HG03805").get("GT"));
            } else if (variantSourceEntry.getFileId().equals("ERZX00075")) {
                assertEquals(1, variantSourceEntry.getCohortStats().size());
                assertTrue(variantSourceEntry.getSamplesData().isEmpty());
            }
        }
        assertNotNull(variantEntityList.get(0).getAnnotation());
    }

    @Test
    public void testFindChromosomeBoundaries() {
        // single study in filter
        assertEquals(193051L, service.findChromosomeLowestReportedCoordinate("11", Collections.singletonList("PRJEB8661")).longValue());
        assertEquals(193959L, service.findChromosomeHighestReportedCoordinate("11", Collections.singletonList("PRJEB8661")).longValue());

        // two studies in filter
        assertEquals(190010L, service.findChromosomeLowestReportedCoordinate("11", Arrays.asList("PRJEB8661", "PRJEB6930")).longValue());
        assertEquals(194190L, service.findChromosomeHighestReportedCoordinate("11", Arrays.asList("PRJEB8661", "PRJEB6930")).longValue());

        // null is returned if a study has no variants in a chromosome
        assertNull(service.findChromosomeLowestReportedCoordinate("11", Arrays.asList("PRJEB5870")));
        assertNull(service.findChromosomeHighestReportedCoordinate("11", Arrays.asList("PRJEB5870")));
    }

    @Test
    public void testCountTotalNumberOfVariants() {
        // the returned number of variants should be the same as the number of variants in the test database
        assertEquals(498, service.countTotalNumberOfVariants());
    }

    @Test
    public void testfindbyRegionAndOtherBeaconFilters() {
        Region startRange = new Region("9", 10099L, 10099L);
        Region endRange = new Region("9", 10099L, 10099L);

        Pageable pageable = new PageRequest(0, 1000);

        List<VariantRepositoryFilter> filters = new FilterBuilder().getBeaconFilters("A", "T",
                VariantType.SNV, Collections.singletonList("PRJEB5829"));

        List<VariantMongo> variantMongoList = service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters,
                pageable);

        assertTrue(variantMongoList.size() > 0);
        assertEquals("9", variantMongoList.get(0).getChromosome());
        assertEquals("A", variantMongoList.get(0).getReference());
        assertEquals("T", variantMongoList.get(0).getAlternate());
        assertEquals(VariantType.SNV, variantMongoList.get(0).getType());


        filters = new FilterBuilder().getBeaconFilters("A", "T", VariantType.SNV, null);
        assertTrue(service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters, pageable).size() > 0);

        filters = new FilterBuilder().getBeaconFilters("A", "T", null, null);
        assertTrue(service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters, pageable).size() > 0);

        filters = new FilterBuilder().getBeaconFilters("A", null, VariantType.SNV, null);
        assertTrue(service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters, pageable).size() > 0);

        endRange = new Region("9", 10098L, 10098L);
        assertFalse(service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters, pageable).size() > 0);

    }

    @Test
    public void testfindbyRegionAndOtherBeaconFiltersWithRanges() {
        Region startRange = new Region("11", 190238L, 190276L);
        Region endRange = new Region("11", 190238L, 190276L);

        Pageable pageable = new PageRequest(0, 1000);

        List<VariantRepositoryFilter> filters = new FilterBuilder().getBeaconFilters("A", null, null, null);

        assertEquals(2, service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters,
                pageable).size());

        filters = new FilterBuilder().getBeaconFilters("A", "T", null, null);

        assertEquals(1, service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters,
                pageable).size());

        assertEquals("11_190238_A_T", service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters,
                pageable).get(0).getId());

        filters = new FilterBuilder().getBeaconFilters("A", "C", null, null);

        assertEquals(1, service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters,
                pageable).size());

        assertEquals("11_190276_A_C", service.findByRegionAndOtherBeaconFilters(startRange, endRange, filters,
                pageable).get(0).getId());
    }
}

