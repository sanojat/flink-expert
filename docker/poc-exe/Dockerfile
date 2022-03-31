FROM wurstmeister/kafka:latest

# we replace the default connect-standalone.properties so we can properly resolve to our local kafka docker development
COPY connect-standalone.properties /opt/kafka/config/

COPY sink-quickstart-cassandra.properties /opt/kafka/config/

# we replace the start command creating a connector instead.
COPY start-kafka.sh /usr/bin/

#transfering cassandra jar
COPY kafka-connect-cassandra-3.0.1-2.5.0-all.jar /usr/local/share

# permissions
RUN chmod a+x /usr/bin/start-kafka.sh
