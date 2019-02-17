package com.sjsu.cmpe275.helloWorld;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        ApplicationContext appContext = new ClassPathXmlApplicationContext("beans.xml");
        Greeter greeter = appContext.getBean("greeter", Greeter.class);
        System.out.println(greeter.getGreeting());
    }
}
