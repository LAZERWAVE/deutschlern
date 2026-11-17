
package com.deutschlern.messaging.stream;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@Configuration
@EnableKafkaStreams
public class LearningAnalyticsStream {

    @Bean
    public KTable<String, Long> studentWordCounts(
            StreamsBuilder streamsBuilder) {

        KTable<String, Long> counts = streamsBuilder
                .stream(
                        "word-learned",
                        Consumed.with(Serdes.String(), Serdes.String())
                )
                .groupByKey(
                        Grouped.with(Serdes.String(), Serdes.String())
                )
                .count(
                        Materialized
                                .<String, Long, org.apache.kafka.streams.state.KeyValueStore<
                                        org.apache.kafka.common.utils.Bytes, byte[]
                                        >>as("student-word-counts")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Long())
                );

        counts.toStream()
                .mapValues(String::valueOf)
                .to(
                        "student-word-counts",
                        Produced.with(Serdes.String(), Serdes.String())
                );

        return counts;
    }
}
