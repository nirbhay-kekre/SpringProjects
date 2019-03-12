package edu.sjsu.cmpe275.aop.aspect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;

import edu.sjsu.cmpe275.aop.Secret;

@Aspect
@Order(1)
public class StatsAspect {

	// Recourses for longest secret length.
	private int longestSecretLength = 0;

	// Recourses for most trusted user.
	private Map<String, Set<String>> targetUserToUniqueSharerSecretMap = new HashMap<String, Set<String>>();
	private String mostTrustedUser = null;
	private int mostTrustedUserCount = 0;

	// Recourses for best known secret stat.
	private Map<UUID, Set<String>> secretReadByUserMap = new HashMap<UUID, Set<String>>();
	private String bestKnownSecret = null;
	private int bestKnowSecretCount = 0;
	private Map<UUID, String> secretOwnerMap = new HashMap<UUID, String>();

	// Recourses for worst secret keeper stat.
	private Map<String, Set<String>> sharedWithUserUniqueSecretMap = new HashMap<String, Set<String>>();
	private Map<String, Set<String>> sharedByUserUniqueSecretMap = new HashMap<String, Set<String>>();

	@AfterReturning("execution(public * edu.sjsu.cmpe275.aop.SecretService.createSecret(..))")
	public void createSecretAdvice(JoinPoint joinPoint) {
		String secretContent = (String) joinPoint.getArgs()[1];
		if (secretContent != null && secretContent.length() > longestSecretLength) {
			longestSecretLength = secretContent.length();
		}
	}

	@AfterReturning(pointcut = "execution(public * edu.sjsu.cmpe275.aop.SecretService.createSecret(..))", returning = "secretId")
	public void createSecretAdvice(JoinPoint joinPoint, Object secretId) {
		String userId = (String) joinPoint.getArgs()[0];
		UUID secret = (UUID) secretId;
		secretOwnerMap.put(secret, userId);
	}

	@Around("execution(public * edu.sjsu.cmpe275.aop.SecretStats.getLengthOfLongestSecret(..))")
	public int getLongestSecretLengthAdvice(JoinPoint jointPoint) {
		return longestSecretLength;
	}

	@AfterReturning("execution(public * edu.sjsu.cmpe275.aop.SecretStats.resetStatsAndSystem(..))")
	public void resetStatsAdvice() {
		longestSecretLength = 0;
		targetUserToUniqueSharerSecretMap.clear();
		mostTrustedUser = null;
		mostTrustedUserCount = 0;
		secretReadByUserMap.clear();
		bestKnownSecret = null;
		bestKnowSecretCount = 0;
		sharedWithUserUniqueSecretMap.clear();
		sharedByUserUniqueSecretMap.clear();
	}

	@AfterReturning("execution(public * edu.sjsu.cmpe275.aop.SecretService.shareSecret(..))")
	public void updateMostTrustedUserStat(JoinPoint joinPoint) {
		String userId = (String) joinPoint.getArgs()[0];
		UUID secretId = (UUID) joinPoint.getArgs()[1];
		String targetUserId = (String) joinPoint.getArgs()[2];
		if (targetUserId.equals(userId)) {
			return;
		} else {
			Set<String> uniqueSharings = targetUserToUniqueSharerSecretMap.get(targetUserId);
			if (uniqueSharings == null) {
				uniqueSharings = new HashSet<String>();
				targetUserToUniqueSharerSecretMap.put(targetUserId, uniqueSharings);
			}
			uniqueSharings.add(userId + "##" + secretId.toString());
			if (uniqueSharings.size() > mostTrustedUserCount) {
				mostTrustedUser = targetUserId;
				mostTrustedUserCount = uniqueSharings.size();
			} else if (uniqueSharings.size() == mostTrustedUserCount && targetUserId.compareTo(mostTrustedUser) < 0) {
				mostTrustedUser = targetUserId;
			}
		}
	}

	@AfterReturning("execution(public * edu.sjsu.cmpe275.aop.SecretService.shareSecret(..))")
	public void updateWorstSecretKeeperStat(JoinPoint joinPoint) {
		String userId = (String) joinPoint.getArgs()[0];
		UUID secretId = (UUID) joinPoint.getArgs()[1];
		String targetUserId = (String) joinPoint.getArgs()[2];
		if (targetUserId.equals(userId)) {
			return;
		} else {
			Set<String> uniqueSharingsWithUser = sharedWithUserUniqueSecretMap.get(targetUserId);
			if (uniqueSharingsWithUser == null) {
				uniqueSharingsWithUser = new HashSet<String>();
				sharedWithUserUniqueSecretMap.put(targetUserId, uniqueSharingsWithUser);
			}
			uniqueSharingsWithUser.add(userId + "##" + secretId.toString());

			Set<String> uniqueSharingsByUser = sharedByUserUniqueSecretMap.get(userId);
			if (uniqueSharingsByUser == null) {
				uniqueSharingsByUser = new HashSet<String>();
				sharedByUserUniqueSecretMap.put(userId, uniqueSharingsByUser);
			}
			uniqueSharingsByUser.add(targetUserId + "##" + secretId.toString());
		}
	}

	@AfterReturning(pointcut = "execution(public * edu.sjsu.cmpe275.aop.SecretService.readSecret(..))", returning = "content")
	public void updateBestKnownSecret(JoinPoint joinPoint, Object content) {
		String userId = (String) joinPoint.getArgs()[0];
		UUID secretId = (UUID) joinPoint.getArgs()[1];
		Secret secretContent = (Secret) content;
		if (this.secretOwnerMap.get(secretId).equals(userId)) {
			return;
		}
		Set<String> readers = secretReadByUserMap.get(secretId);
		if (readers == null) {
			readers = new HashSet<String>();
			secretReadByUserMap.put(secretId, readers);
		}
		readers.add(userId);
		if (bestKnowSecretCount < readers.size()) {
			bestKnowSecretCount = readers.size();
			bestKnownSecret = secretContent.getContent();
		} else if (bestKnowSecretCount == readers.size()) {
			if (secretContent.getContent() == null || bestKnownSecret == null) {
				bestKnownSecret = null;
			} else if (secretContent.getContent().compareTo(bestKnownSecret) < 0) {
				bestKnownSecret = secretContent.getContent();
			}
		}
	}

	@Around("execution(public * edu.sjsu.cmpe275.aop.SecretStats.getMostTrustedUser(..))")
	public String getMostTrustedUserAdvice(JoinPoint jointPoint) {
		return mostTrustedUser;
	}

	@Around("execution(public * edu.sjsu.cmpe275.aop.SecretStats.getBestKnownSecret(..))")
	public String getBestKnownSecretAdvice(JoinPoint jointPoint) {
		return bestKnownSecret;
	}

	@Around("execution(public * edu.sjsu.cmpe275.aop.SecretStats.getWorstSecretKeeper(..))")
	public String getWorstSecretKeeperAdvice(JoinPoint jointPoint) {
		Set<String> allUsers = new HashSet<String>();
		allUsers.addAll(sharedByUserUniqueSecretMap.keySet());
		allUsers.addAll(sharedWithUserUniqueSecretMap.keySet());
		int worstSecretKeeperScore = Integer.MAX_VALUE;
		String worstSecretKeeper = null;
		for (String user : allUsers) {
			Set<String> sharedWithUser = sharedWithUserUniqueSecretMap.get(user);
			Set<String> sharedByUser = sharedByUserUniqueSecretMap.get(user);
			int sharedWithUserCount = sharedWithUser == null ? 0 : sharedWithUser.size();
			int sharedByUserCount = sharedByUser == null ? 0 : sharedByUser.size();
			int currentUserScore = sharedWithUserCount - sharedByUserCount;
			if (currentUserScore < worstSecretKeeperScore) {
				worstSecretKeeperScore = currentUserScore;
				worstSecretKeeper = user;
			} else if (currentUserScore == worstSecretKeeperScore && worstSecretKeeper.compareTo(user) > 0) {
				worstSecretKeeper = user;
			}
		}
		return worstSecretKeeper;
	}
}
