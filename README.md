#####FLINK-KAFKA CONNECT POC GUIDE ##########

1) Docker compose file is available in combined and standalone mode.

	Prerequisite for poc.

	a) Java 1.8 or greater,maven,docker
	b) checkout below project and compile using maven
	  1) myhl7simulator
	  2) flinkexpert
	  3) patientdataservice
	  4) Download kafka-connect-cassandra-3.0.1-2.5.0-all.tar.gz from https://github.com/lensesio/stream-reactor/releases 
	     and extract it as jar and place the file in folder "flink-expert\docker\poc-exe\"

2) Combined docker set up

    Go to 'flink-expert\docker\poc-exe' folder and open command prompt or git bash then execute below scripts.
	
    1)For windows OS ,windows-host file needs changes for kafka.so find host file (file name is 'host' 
	and path: %WinDir%\System32\Drivers\Etc) and add the entry mentioned below.
	This is to register kafka as your loopback interface.
	
	127.0.0.1       kafka
	
    2) docker-compose -f docker-compose-poc.yml up -----> up and running kafka,flink and sylla.
	   docker-compose -f docker-compose-poc.yml down to stop the containers. 
		Note: go to docker desktop dash board and make sure ,kafka,flink and sylla is up and running.
		
	3) Create table in sylla database : go to docker desktop dashboard and take sylla container terminal and
	   type the command > cqlsh and execute scripts mentioned in 'flink-expert\docker\poc-exe\cassandra-sql.txt' line by line.
		
	4) docker-compose -f docker-compose-kafka-connect.yml up -------> up and running kafka-connect.
	   docker-compose -f docker-compose-kafka-connect.yml down to stop the containers.
	
	5) Run 'Hl7Simulator' spring boot application which will  run in 8080 port by default ,
    	Main class : com.hl7.learn.simulator.Hl7Simulator.
		
    6) Run standalone java program for flink :project is 'flinkexpert' ,Main class is poc.learn.expert.flinkFlinkJobExecuter. 	
	
	6) Run 'patient-data-service' spring boot application which will  run in 8989 port by default ,
    	Main class : poc.PatientDataServiceApplication
		
	7) Check the data in sylla DB from the sylla docker container terminal: select * from waveform_tr_tbl;
	
	8) To see Live stream data : http://localhost:8989/
	
	9) To see historic data : execute java class poc.patient.service.impl.Test
	   from the output "from time  :" as the value of  'fromTime' parameter in URL
						 "to time    :" as the value of  'toTIme' parameter in URL 
	  http://localhost:8989/patient/waveform?patientId=Patient-0&fromTime=1648617235351&toTIme=1648617240351
     	 
3) Standalone docker set up	    
	 
    a) compile 'flink-expert' project using maven and check the folder 'flink-expert\docker\poc-exe\poc-lib'
	and make sure latest jar is available "flinkexpert-jar-with-dependencies.jar".
	"flinkexpert-jar-with-dependencies.jar" is fat jar which contains project jar and its dependencies.
	
	b) execute docker compose, kafka-standalone.yml for kafka.
	
    c) execute docker compose, flink-standalone.yml	for flink
	    Note: Docker compose file contains command to submit the job as mentioned below and submitted job can be visualized in http://localhost:8087/
		
			command: standalone-job --job-classname poc.learn.expert.flink.FlinkJobExecuter
			volumes:
			  - ./poc-lib:/opt/flink/usrlib

	d) execute docker compose,sylla-standalone.yml for sylla.
	
	e) Create table in sylla database : go to docker desktop dashboard and take sylla container terminal and
	   type the command > cqlsh and execute scripts mentioned in 'flink-expert\docker\poc-exe\cassandra-sql.txt' line by line.
	   
	f) execute docker compose, docker-compose-kafka-connect.yml  for kafka-sink connect.
	
	g) Run 'Hl7Simulator' spring boot application which will  run in 8080 port by default ,
    	Main class : com.hl7.learn.simulator.Hl7Simulator.
		
	h) Flink job is already submitted in step c.
	
	i) Run 'patient-data-service' spring boot application which will  run in 8989 port by default ,
    	Main class : poc.PatientDataServiceApplication
		
	j) Check the data in sylla DB from the sylla docker container terminal: select * from waveform_tr_tbl;
	
	k) To see Live stream data : http://localhost:8989/
	
	l) To see historic data : execute java class poc.patient.service.impl.Test
	   from the output "from time  :" as the value of  'fromTime' parameter in URL
						 "to time    :" as the value of  'toTIme' parameter in URL 
	  http://localhost:8989/patient/waveform?patientId=Patient-0&fromTime=1648617235351&toTIme=1648617240351 
	
	
	   
   	



