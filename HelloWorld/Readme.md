## About this Project
This is a simple Spring application, which demonstrates a bean injection.<br>
Here's How it works:<br>
I have created an interface as follows which is implemented by GreeterImpl
```java
 interface Greeter {
	void setName(String name); // name of the author
	String getGreeting();
  }
```

And I have created a bean with name greeter for this class.

Later, After initializing Application context, I am injecting this bean to print "Hello World from greeter!"

## Prerequisites to run this project:
Should have Java and Maven installed on your machine.
## How to run this project:
1. Download the dependencies, run following command in project directory:
```bash
mvn clean install
```
2. run project:
```
mvn exec:java
```

You should get the output as below!!<br>
INFO: Loading XML bean definitions from class path resource [beans.xml]<br>
<b>Hello World from Nirbhay!</b><br>
[INFO] ------------------------------------------------------------------------<br>
[INFO] BUILD SUCCESS<br>
[INFO] ------------------------------------------------------------------------<br>
[INFO] Total time:  0.836 s<br>
[INFO] Finished at: 2019-02-16T19:07:51-08:00<br>
[INFO] ------------------------------------------------------------------------<br>
