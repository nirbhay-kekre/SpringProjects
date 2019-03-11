package edu.sjsu.cmpe275.aop.aspect;

import java.io.IOException;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import edu.sjsu.cmpe275.aop.Secret;

@Aspect
@Order(2)
public class RetryAspect {
	
	@Around("execution(public void edu.sjsu.cmpe275.aop.SecretService.*(..))")
	public void shareUnShareRetryAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.printf("Retry aspect prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object result = null;
		try {
			result = joinPoint.proceed();
		} catch (IOException e) {
			System.out.printf("IOException occured in %s %s\n", joinPoint.getSignature().getName()," retrying (1/2)...");
			try {
				result = joinPoint.proceed();
			} catch (IOException e2) {
				System.out.printf("IOException occured in %s %s\n", joinPoint.getSignature().getName()," retrying (2/2)...");
				result = joinPoint.proceed();
			}
		}
	}
	
	@Around("execution(public java.util.UUID edu.sjsu.cmpe275.aop.SecretService.createSecret(..))")
	public UUID createSecretRetryAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.printf("Retry aspect prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object result = null;
		try {
			result = joinPoint.proceed();
		} catch (IOException e) {
			System.out.printf("IOException occured in %s %s\n", joinPoint.getSignature().getName()," retrying (1/2)...");
			try {
				result = joinPoint.proceed();
			} catch (IOException e2) {
				System.out.printf("IOException occured in %s %s\n", joinPoint.getSignature().getName()," retrying (2/2)...");
				result = joinPoint.proceed();
			}
		}
		return (UUID) result;
	}
	
	@Around("execution(public edu.sjsu.cmpe275.aop.Secret edu.sjsu.cmpe275.aop.SecretService.readSecret(..))")
	public Secret readSecretRetryAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.printf("Retry aspect prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object result = null;
		try {
			result = joinPoint.proceed();
		} catch (IOException e) {
			System.out.printf("IOException occured in %s %s\n", joinPoint.getSignature().getName()," retrying (1/2)...");
			try {
				result = joinPoint.proceed();
			} catch (IOException e2) {
				System.out.printf("IOException occured in %s %s\n", joinPoint.getSignature().getName()," retrying (2/2)...");
				result = joinPoint.proceed();
			}
		}
		return (Secret) result;
	}

}
