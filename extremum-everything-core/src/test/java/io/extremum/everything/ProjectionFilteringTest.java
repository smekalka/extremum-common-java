package io.extremum.everything;

import io.extremum.mongo.model.MongoCommonModel;
import io.extremum.common.annotation.ModelName;
import io.extremum.everything.collection.Projection;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author rpuch
 */
class ProjectionFilteringTest {
    private static final ZonedDateTime YEAR_2000 = ZonedDateTime.of(2000, 1, 1, 0, 0, 0, 0, ZoneId.of("UTC"));
    private static final ZonedDateTime YEAR_2005 = YEAR_2000.plusYears(5);
    private static final ZonedDateTime YEAR_2010 = YEAR_2000.plusYears(10);

    private final Projection since2000Till2010 = Projection.sinceUntil(YEAR_2000, YEAR_2010);

    @Test
    void whenDateTimeIsBetweenSinceAndUntil_thenWeShouldAcceptIt() {
        assertThat(since2000Till2010.accepts(YEAR_2000), is(true));
        assertThat(since2000Till2010.accepts(YEAR_2005), is(true));
    }

    @Test
    void whenDateTimeIsBeforeSince_thenWeShouldNotAcceptIt() {
        assertThat(since2000Till2010.accepts(YEAR_2000.minusNanos(1)), is(false));
    }

    @Test
    void whenDateTimeIsAfterSince_thenWeShouldNotAcceptIt() {
        assertThat(since2000Till2010.accepts(YEAR_2010.plusNanos(1)), is(false));
    }

    @Test
    void whenDateTimeIsNull_thenWeShouldAcceptIt() {
        assertThat(since2000Till2010.accepts((ZonedDateTime) null), is(true));
    }

    @Test
    void whenSinceIsNull_thenItShouldWorkAsMinusInfinityWithDateTime() {
        Projection projection = Projection.sinceUntil(null, YEAR_2010);

        assertThat(projection.accepts(YEAR_2005), is(true));
    }

    @Test
    void whenUntilIsNull_thenItShouldWorkAsPlusInfinityWithDateTime() {
        Projection projection = Projection.sinceUntil(YEAR_2000, null);

        assertThat(projection.accepts(YEAR_2005), is(true));
    }

    @Test
    void whenCreatedIsBetweenSinceAndUntil_thenWeShouldAcceptTheModel() {
        assertThat(since2000Till2010.accepts(new TestModel(YEAR_2000)), is(true));
        assertThat(since2000Till2010.accepts(new TestModel(YEAR_2005)), is(true));
    }

    @Test
    void whenCreatedIsBeforeSince_thenWeShouldNotAcceptTheModel() {
        assertThat(since2000Till2010.accepts(new TestModel(YEAR_2000.minusNanos(1))), is(false));
    }

    @Test
    void whenCreatedIsAfterSince_thenWeShouldNotAcceptTheModel() {
        assertThat(since2000Till2010.accepts(new TestModel(YEAR_2010.plusNanos(1))), is(false));
    }

    @Test
    void whenCreatedIsNull_thenWeShouldAcceptTheModel() {
        assertThat(since2000Till2010.accepts(new TestModel(null)), is(true));
    }

    @Test
    void whenSinceIsNull_thenItShouldWorkAsMinusInfinityWithModel() {
        Projection projection = Projection.sinceUntil(null, YEAR_2010);

        assertThat(projection.accepts(new TestModel(YEAR_2005)), is(true));
    }

    @Test
    void whenUntilIsNull_thenItShouldWorkAsPlusInfinityWithModel() {
        Projection projection = Projection.sinceUntil(YEAR_2000, null);

        assertThat(projection.accepts(new TestModel(YEAR_2005)), is(true));
    }

    @Test
    void sinceShouldBeIncludedInAcceptedIntervalWithDateTime() {
        assertThat(since2000Till2010.accepts(YEAR_2000), is(true));
    }

    @Test
    void sinceShouldBeIncludedInAcceptedIntervalWithModel() {
        assertThat(since2000Till2010.accepts(new TestModel(YEAR_2000)), is(true));
    }

    @Test
    void untilShouldNotBeIncludedInAcceptedIntervalWithDateTime() {
        assertThat(since2000Till2010.accepts(YEAR_2010), is(false));
    }

    @Test
    void untilShouldNotBeIncludedInAcceptedIntervalWithModel() {
        assertThat(since2000Till2010.accepts(new TestModel(YEAR_2010)), is(false));
    }

    @ModelName("Test")
    private static class TestModel extends MongoCommonModel {
        TestModel(ZonedDateTime created) {
            setCreated(created);
        }
    }
}