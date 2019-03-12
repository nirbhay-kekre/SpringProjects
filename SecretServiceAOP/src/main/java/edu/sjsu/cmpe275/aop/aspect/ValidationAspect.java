package edu.sjsu.cmpe275.aop.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

@Aspect
@Order(0)
public class ValidationAspect {
	
	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.createSecret(..))")
	public void createSecretValidationAdvice(JoinPoint joinPoint) {
		System.out.printf("Doing validation prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] arguments = joinPoint.getArgs();
		String userId = (String) arguments[0] ,secretContent= (String) arguments[1];
		if(userId == null || (secretContent != null && secretContent.length() >100)) {
			throw new IllegalArgumentException();
		}
	}
	
	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.readSecret(..)) || execution(public * edu.sjsu.cmpe275.aop.SecretService.shareSecret(..)) || execution(public * edu.sjsu.cmpe275.aop.SecretService.unshareSecret(..))")
	public void secretNullInputValidationAdvice(JoinPoint joinPoint) {
		System.out.printf("Doing validation prior to the executuion of the metohd %s\n", joinPoint.getSignature().getName());
		Object[] arguments = joinPoint.getArgs();
		for(Object arg : arguments) {
			if(arg == null) {
				throw new IllegalArgumentException();
			}
		}
		
	}
}
