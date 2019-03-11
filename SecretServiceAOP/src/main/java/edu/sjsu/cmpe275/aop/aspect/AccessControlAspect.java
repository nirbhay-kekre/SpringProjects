package edu.sjsu.cmpe275.aop.aspect;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;

import edu.sjsu.cmpe275.aop.NotAuthorizedException;
import edu.sjsu.cmpe275.aop.Secret;
import edu.sjsu.cmpe275.aop.SecretServiceImpl;

@Aspect
@Order(1)
public class AccessControlAspect {

	private Map<String, Set<UUID>> userSecretOwnershipMap = new HashMap<String, Set<UUID>>();
	private Map<String, Set<UUID>> userSharedSecretMap = new HashMap<String, Set<UUID>>();

	@AfterReturning(pointcut = "execution(public * edu.sjsu.cmpe275.aop.SecretService.createSecret(..))", returning = "secretId")
	public void addOwnerShip(JoinPoint joinPoint, Object secretId) {
		UUID secret = (UUID) secretId;
		String userId = (String) joinPoint.getArgs()[0];
		System.out.printf("Adding secret id %s under the ownership of %s.", secret.toString(), userId);
		Set<UUID> ownedSecrets = userSecretOwnershipMap.get(userId);
		if (ownedSecrets == null) {
			ownedSecrets = new HashSet<UUID>();
			userSecretOwnershipMap.put(userId, ownedSecrets);
		}
		ownedSecrets.add(secret);
	}
	
	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.shareSecret(..))")
	public void shareSecretProxy(JoinPoint joinPoint)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		String userId = (String) joinPoint.getArgs()[0];
		UUID secretId = (UUID) joinPoint.getArgs()[1];
		String targetUserId = (String) joinPoint.getArgs()[2];
		/*
		 *  1. if the user with userId has not created SecretId
		 *  2. Trying to share a secret which is not shared with him.
		 *  3. If there does not exist a secret with the given UUID.
		 */
		if(userId.equals(targetUserId)) {
			return;
		}else if ((isSecretOwnedByUser(userId, secretId) || isSecretSharedWithUser(userId, secretId))) {
			Set<UUID> sharedSecrets = userSharedSecretMap.get(targetUserId);
			if (sharedSecrets == null) {
				sharedSecrets = new HashSet<UUID>();
				userSharedSecretMap.put(targetUserId, sharedSecrets);
			}
			sharedSecrets.add(secretId);
		}else {
			throw new NotAuthorizedException();
		}
	}
	
	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.unshareSecret(..))")
	public void unshareSecretProxy(JoinPoint joinPoint)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		String userId = (String) joinPoint.getArgs()[0];
		UUID secretId = (UUID) joinPoint.getArgs()[1];
		String targetUserId = (String) joinPoint.getArgs()[2];
		/*
		 *  NotAuthorizedException:
		 * 	1.  If the user with userId has not created the given secret.
		 * 	2. 	If there does not exist a secret with the given UUID.
		 */
		if(userId.equals(targetUserId)) {
			return;
		}
		if (isSecretOwnedByUser(userId, secretId)) {
			Set<UUID> sharedSecrets = userSharedSecretMap.get(targetUserId);
			if (sharedSecrets != null) {
				sharedSecrets.remove(secretId);
			}
		}else {
			throw new NotAuthorizedException();
		}
		
	}

	/*
	 * NotAuthorizedException: 
	 * 1. If the given user has not created the SecretId which is asked. 
	 * 2. If the secret Id is not shared with him.
	 * 3. If secretId does not exist
	 */
	@Before("execution(public * edu.sjsu.cmpe275.aop.SecretService.readSecret(..))")
	public void readSecretProxyAdvice(JoinPoint joinPoint)
			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
		System.out.printf("Doing validation prior to the executuion of the metohd %s\n",
				joinPoint.getSignature().getName());
		Object[] arguments = joinPoint.getArgs();
		String userId = (String) arguments[0];
		UUID secretId = (UUID) arguments[1];
		SecretServiceImpl service = (SecretServiceImpl) joinPoint.getTarget();
		if (!( (isSecretOwnedByUser(userId, secretId)
				|| isSecretSharedWithUser(userId, secretId)))) {
			throw new NotAuthorizedException();
		}
	}

//	private boolean isSecretAlreadyExist(UUID secretId, SecretServiceImpl service)
//			throws IllegalArgumentException, IllegalAccessException, NoSuchFieldException, SecurityException {
//		Field f = service.getClass().getDeclaredField("secrets");
//		f.setAccessible(true);
//		Map<UUID, Secret> map = new HashMap<UUID, Secret>();
//		map = (Map<UUID, Secret>) f.get(service);
//		return map.get(secretId) != null;
//	}

	private boolean isSecretOwnedByUser(String userId, UUID secretId) {
		Set<UUID> ownedSecrets = userSecretOwnershipMap.get(userId);
		if (ownedSecrets == null) {
			return false;
		} else {
			return ownedSecrets.contains(secretId);
		}
	}
	private boolean isSecretSharedWithUser(String userId, UUID secretId) {
		Set<UUID> sharedSecrets = userSharedSecretMap.get(userId);
		if (sharedSecrets == null) {
			return false;
		} else {
			return sharedSecrets.contains(secretId);
		}
	}
	
	@AfterReturning("execution(public * edu.sjsu.cmpe275.aop.SecretStats.resetStatsAndSystem(..))")
	public void resetAccessControlResources() {
		userSecretOwnershipMap.clear();
		userSharedSecretMap.clear();
	}

}
