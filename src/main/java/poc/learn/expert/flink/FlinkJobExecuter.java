/**
 * 
 */
package poc.learn.expert.flink;

import java.time.Instant;
import java.util.UUID;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.windowing.WindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import poc.learn.expert.flink.support.FormattedWaveForm;

/**
 * @author 1013744
 *
 */
public class FlinkJobExecuter {

	/**
	 * 
	 */
	private static ObjectMapper mapper = new ObjectMapper();
	/**
	 * 
	 */
	private static IParser parser = FhirContext.forR4().newJsonParser();
	/**
	 * 
	 */
	private static String KAFKA_SERVER = "kafka:9092";
	/**
	 * 
	 */
	private static String WAVEFORM_TOPIC = "waveform";
	/**
	 * 
	 */
	private static String WAVEFORM_FORMATTED_TOPIC = "waveformformatted";
	/**
	 * 
	 */
	private static String WAVEFORM_CONSOLIDATED = "waveformconsolidated";
	/**
	 * 
	 */
	private static int FLINK_WINDOW_WAITING_IN_SECONDS = 2;

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		/*
		 * Flink enviornment
		 */
		StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		/*
		 * Job to format edm to sylla persistance format
		 */
		formatWaveform(env, KAFKA_SERVER, WAVEFORM_TOPIC, WAVEFORM_FORMATTED_TOPIC);
		/*
		 * Job to formatted waveform to consolidated waveform
		 */
		consolidationJob(env, KAFKA_SERVER, WAVEFORM_FORMATTED_TOPIC, WAVEFORM_CONSOLIDATED);
		/*
		 * flink job execution initiation
		 */
		env.execute();
	}

	/**
	 * @param env
	 * @param server
	 * @param inputTopic
	 * @param outputTopic
	 * @throws Exception
	 */
	public static void formatWaveform(StreamExecutionEnvironment env, String server, String inputTopic,
			String outputTopic) throws Exception {
		/*
		 * KafkaSource : data consume from kafka topic
		 */
		KafkaSource<String> kafkaSource = getKafkaSource(server, inputTopic);
		/*
		 * KafkaSource : data publish from kafka topic
		 */
		KafkaSink<String> sink = getKafkaSink(server, outputTopic);
		/*
		 * 
		 */
		DataStream<String> dStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), inputTopic);

		dStream.flatMap(new FlatMapFunction<String, String>() {
			@Override
			public void flatMap(String value, Collector<String> out) throws Exception {
				Bundle p = parser.parseResource(Bundle.class, value);
				for (BundleEntryComponent s : p.getEntry()) {
					Resource resource = s.getResource();
					if (resource.getClass().equals(Observation.class)) {
						Observation observation = (Observation) resource;
						FormattedWaveForm waveform = new FormattedWaveForm();
						waveform.setPatientname(observation.getSubject().getReference().replaceFirst("Patient/", ""));
						waveform.setReference(observation.getSubject().getReference());
						waveform.setEffectivedatetime(observation.getEffective().primitiveValue());
						waveform.setEffectiveTime(((DateTimeType) observation.getEffective()).getValue().getTime());
						waveform.setMdccode(observation.getCode().getText());
						waveform.setPeriod(observation.getValueSampledData().getPeriod().doubleValue());
						waveform.setDimensions(observation.getValueSampledData().getDimensions());
						waveform.setDevicereference(observation.getDevice().getReference());
						waveform.setId(String.valueOf(Instant.now().toEpochMilli()));
						waveform.setFactor(observation.getValueSampledData().getFactor().doubleValue());
						waveform.setData(observation.getValueSampledData().getData());
						String waveFormated = mapper.writeValueAsString(waveform);
						out.collect(waveFormated);
					}
				}
			}
		}).sinkTo(sink);
	}

	/**
	 * @param env
	 * @param server
	 * @param inputTopic
	 * @param outputTopic
	 * @throws Exception
	 */
	public static void consolidationJob(StreamExecutionEnvironment env, String server, String inputTopic,
			String outputTopic) throws Exception {
		/*
		 * KafkaSource : data consume from kafka topic
		 */
		KafkaSource<String> kafkaSource = getKafkaSource(server, inputTopic);
		/*
		 * KafkaSource : data publish from kafka topic
		 */
		KafkaSink<String> sink = getKafkaSink(server, outputTopic);
		/*
		 * creating stream from kafka 'waveform' topic
		 */
		DataStream<String> dStream = env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), inputTopic);

		dStream.flatMap(new FlatMapFunction<String, FormattedWaveForm>() {

			@Override
			public void flatMap(String value, Collector<FormattedWaveForm> out) throws Exception {
				FormattedWaveForm waveform = mapper.readValue(value, FormattedWaveForm.class);
				out.collect(waveform);
			}

		}).keyBy(new KeySelector<FormattedWaveForm, Tuple2<String, String>>() {
			@Override
			public Tuple2<String, String> getKey(FormattedWaveForm value) throws Exception {
				return Tuple2.of(value.getPatientname(), value.getMdccode());
			}
		}).window(TumblingProcessingTimeWindows
				.of(org.apache.flink.streaming.api.windowing.time.Time.seconds(FLINK_WINDOW_WAITING_IN_SECONDS)))
				.apply(new WindowFunction<FormattedWaveForm, String, Tuple2<String, String>, TimeWindow>() {
					@Override
					public void apply(Tuple2<String, String> key, TimeWindow window, Iterable<FormattedWaveForm> values,
							Collector<String> out) throws Exception {
						FormattedWaveForm existwf = null;
						for (FormattedWaveForm wf : values) {
							if (existwf == null) {
								existwf = wf;
							} else {
								existwf.setEffectivedatetime(wf.getEffectivedatetime());
								existwf.setData(existwf.getData() + " " + wf.getData());
							}
						}
						String output = mapper.writeValueAsString(existwf);
						out.collect(output);
					}
				}).sinkTo(sink);
	}

	/**
	 * @param server
	 * @param topic
	 * @return
	 */
	public static KafkaSource<String> getKafkaSource(String server, String topic) {
		KafkaSource<String> source = KafkaSource.<String>builder().setBootstrapServers(server).setTopics(topic)
				.setGroupId("kafkaGroup").setStartingOffsets(OffsetsInitializer.earliest())
				.setValueOnlyDeserializer(new SimpleStringSchema()).build();
		System.out.println("Kafka Source Service invoked------>");
		return source;
	}

	/**
	 * @param server
	 * @param topic
	 * @return
	 */
	public static KafkaSink<String> getKafkaSink(String server, String topic) {
		KafkaSink<String> sink = KafkaSink.<String>builder().setBootstrapServers(server)
				.setRecordSerializer(KafkaRecordSerializationSchema.builder().setTopic(topic)
						.setValueSerializationSchema(new SimpleStringSchema()).build())
				.setDeliverGuarantee(DeliveryGuarantee.AT_LEAST_ONCE).build();
		System.out.println("Kafka Sink Service invoked------>");
		return sink;
		
	}

}
