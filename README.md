# kafka-monitoring
**Description**
Software for getting kafka consumer group lag for console or prometheus output

Usage:

    1. build
    mvn clea package
    
    2. run
    java -jar target/km.jar -bootstrap-server localhost:9092 -groups cg1,cg2 -interval 5000 -out prometheus -prometheus-job test -prometheus-url localhost:9091/metrics describe
    
options:
 - operation - one of list: [describe]; 
 - bootstrapServer - kafka connection string;
 - groups - consumer-group names insterested for;
 - interval - fetch data interval;
 - out - one of list: [console, prometheus];
 - prometheus-url - pushApi prometeus url (without proto, see example above)
 - prometheus-job - job name 
   

