package edu.sjsu.cmpe275.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Test;

/**
 * 1. resetStatsAndSystem:
 * 		1.1 Reset all the four measurements.
 *		1.2	It also clears up all secret objects ever created and their sharing/unsharing as if the system
 * 			is starting fresh for any purpose related to the metrics below.
 * 
 * 2. getLengthOfLongestSecret: 
 * 		2.1 when there is no secret created, ouput should be 0
 * 		2.2 for other cases check with different order of lengths
 * 
 * 3. getMostTrustedUser:
 * 		3.1	Two secrets with the same content but different UUIDs are considered different secrets.
 * 		3.2	Unsharing does NOT affect this stat. 
 * 		3.3	If Alice and Bob share the same secret with Carl once each, it's
 * 			considered as two total sharing occurrences with Carl.
 * 		3.4 If Alice shares the same secret he created with Carl five times and later unshares it, it is
 * 			still considered one sharing occurrence. 
 * 		3.5	Sharing a message with a user himself does NOT count for the purpose of this stat.
 * 		3.6 If there is a tie, return the 1st of such users based on alphabetical order of the user ID.
 * 		3.7 Only successful sharing matters here; if no users has been successfully shared
 * 			with any secret, return null.
 * 
 * 4. getWorstSecretKeeper:
 * 		4.1	If Alice and Bob share the same message with Carl three times each, and Carl shares the same
 * 			message with Doug, Ed, and Fred, Carl's net sharing balance is 2-3 = -1.
 *		4.2	Again, sharing/unsharing with one himself does not count here.
 * 		4.3	the ID of the person with the smallest net sharing balance. If there
 *         	is a tie, return the 1st of such users based on alphabetical order of
 *         	the user ID.
 *      4.4	If no users has been successfully shared with any secret, return null.
 *      
 * 5. getBestKnownSecret:
 * 	  	5.1	Returns the secret that has been successfully read by the biggest number of
 * 			different users, OTHER THAN the creator himself.
 * 		5.2 If the same secret is read by the same user more than once successfully, it is still considered 
 * 			as one.
 * 		5.3 If Alice shares a secret with Bob, Bob reads this secret, and later Alice
 * 			unshares it from Bob, Bob's read still counts because it was successful.
 *  	5.4 If no secrets are ever read by users other	than the creators, return null.
 *  	5.5 If there is a tie, return based on the alphabetic order of the secret content.
 *  
 * @author nirbhaykekre
 *
 */
public class SecretStatsTest extends BaseTestClass{
	/**
	 * 1. resetStatsAndSystem:
	 * 		1.1 Reset all the four measurements.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public void resetStatsAndSystemFouMeasure() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("user1", "Hello This is secret");
		int length = secretStats.getLengthOfLongestSecret();
		assertEquals("Hello This is secret".length(), length);
		secretService.shareSecret("user1", secretId1, "user2");
		secretService.readSecret("user2", secretId1);
		assertEquals("user2", secretStats.getMostTrustedUser());
		assertEquals("user1", secretStats.getWorstSecretKeeper());
		assertEquals("Hello This is secret",secretStats.getBestKnownSecret());
		
		secretStats.resetStatsAndSystem();
		
		assertEquals(0, secretStats.getLengthOfLongestSecret());
		assertNull(secretStats.getMostTrustedUser());
		assertNull(secretStats.getWorstSecretKeeper());
		assertNull(secretStats.getBestKnownSecret());
	}
	/**
	 * 1. resetStatsAndSystem:
	 * 		1.2	It also clears up all secret objects ever created and their sharing/unsharing as if the system
	 * 			is starting fresh for any purpose related to the metrics below.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	public void resetStatsAndSystemRestSecret() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("user1", "Hello This is secret");
		assertEquals("Hello This is secret", secretService.readSecret("user1", secretId1));
		
		secretStats.resetStatsAndSystem();
		boolean flag =false;
		try {
			 secretService.readSecret("user1", secretId1);
		}catch(NotAuthorizedException e) {
			flag =true;
		}
		assertTrue(flag);
		assertEquals(0, secretStats.getLengthOfLongestSecret());
		assertNull(secretStats.getMostTrustedUser());
		assertNull(secretStats.getWorstSecretKeeper());
		assertNull(secretStats.getBestKnownSecret());
	}
	
	
	/**
	 * 2. getLengthOfLongestSecret: 
	 * 		2.1 when there is no secret created, ouput should be 0
	 */
	@Test
	public void getLengthOfLongestSecretNoSecreteCreated() {
		int length = secretStats.getLengthOfLongestSecret();
		assertEquals(0, length);
	}
	
	/**
	 * 2. getLengthOfLongestSecret: 
	 * 		2.2 for other cases check with different order of lengths
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getLengthOfLongestSecretSuccess() throws IllegalArgumentException, IOException {
		secretService.createSecret("user1", "Hello This is secret");
		int length = secretStats.getLengthOfLongestSecret();
		assertEquals("Hello This is secret".length(), length);
		
		secretService.createSecret("user1", "Hello");
		length = secretStats.getLengthOfLongestSecret();
		assertEquals("Hello This is secret".length(), length);
		
		secretService.createSecret("user1", "Hello This is bigger secret");
		length = secretStats.getLengthOfLongestSecret();
		assertEquals("Hello This is bigger secret".length(), length);
	}
	
	/**
	 * 3. getMostTrustedUser:
	 * 		3.1	Two secrets with the same content but different UUIDs are considered different secrets.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getMostTrustedUserSameContentNonUniqueUUID() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("user1", "Hello This is secret");
		UUID secretId2 = secretService.createSecret("user1", "Hello This is secret");
		assertNull(secretStats.getMostTrustedUser());
		secretService.shareSecret("user1", secretId1, "user2");
		assertEquals("user2", secretStats.getMostTrustedUser());
		secretService.shareSecret("user1", secretId2, "user3");
		assertEquals("user2", secretStats.getMostTrustedUser());
		secretService.shareSecret("user1", secretId1, "user3");
		assertEquals("user3", secretStats.getMostTrustedUser());
		
	}
	
	/**
	 * 3. getMostTrustedUser:
	 * 		3.2	Unsharing does NOT affect this stat. 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getMostTrustedUserUnshare() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("user1", "Hello This is secret");
		UUID secretId2 = secretService.createSecret("user1", "Hello This is secret");
		assertNull(secretStats.getMostTrustedUser());
		secretService.shareSecret("user1", secretId1, "user2");
		assertEquals("user2", secretStats.getMostTrustedUser());
		secretService.shareSecret("user1", secretId2, "user3");
		assertEquals("user2", secretStats.getMostTrustedUser());
		secretService.shareSecret("user1", secretId1, "user3");
		assertEquals("user3", secretStats.getMostTrustedUser());
		secretService.unshareSecret("user1", secretId1, "user3");
		assertEquals("user3", secretStats.getMostTrustedUser());
		secretService.unshareSecret("user1", secretId2, "user3");
		assertEquals("user3", secretStats.getMostTrustedUser());
	}
	
	/**
	 * getMostTrustedUser
	 * 3.3	If Alice and Bob share the same secret with Carl once each, it's
	 * 		considered as two total sharing occurrences with Carl.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getMostTrustedUserSameSecretDifferentSharer() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Carl", secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Bob");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Bob");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Bob", secretId1, "Carl");
		assertEquals("Carl", secretStats.getMostTrustedUser());
		
	}
	
	/**
	 * getMostTrustedUser
	 * 3.4 	If Alice shares the same secret he created with Carl five times and later unshares it, it is
	 * 		still considered one sharing occurrence. 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getMostTrustedUserReshareUnshare() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Carl", secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Bob");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.unshareSecret("Alice", secretId1, "Bob");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.unshareSecret("Alice", secretId1, "Carl");
		assertEquals("Bob", secretStats.getMostTrustedUser());
	}
	
	/**
	 * getMostTrustedUser
	 * 3.5	Sharing a message with a user himself does NOT count for the purpose of this stat.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getMostTrustedUserSelfSharing() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		
		secretService.shareSecret("Alice", secretId1, "Alice");
		assertNull(secretStats.getMostTrustedUser());
		
		secretService.unshareSecret("Alice", secretId1, "Alice");
		assertNull(secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Alice");
		assertNull(secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Bob");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		secretService.unshareSecret("Alice", secretId1, "Alice");
		assertEquals("Bob", secretStats.getMostTrustedUser());
	}
	
	/**
	 * getMostTrustedUser
	 * 3.6 If there is a tie, return the 1st of such users based on alphabetical order of the user ID.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getMostTrustedUserTie() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		
		//Carl:1, Bob: 0 
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Carl", secretStats.getMostTrustedUser());
		
		//Carl: 1, Bob: 1
		secretService.shareSecret("Alice", secretId1, "Bob");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
		//Carl: 2, Bob: 1
		secretService.shareSecret("Bob", secretId1, "Carl");
		assertEquals("Carl", secretStats.getMostTrustedUser());
		
		//Carl: 2, Bob: 2
		secretService.shareSecret("Carl", secretId1, "Bob");
		assertEquals("Bob", secretStats.getMostTrustedUser());
		
	}
	
	/**
	 * getMostTrustedUser
	 * 3.7 	Only successful sharing matters here; if no users has been successfully shared
	 * 		with any secret, return null.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getMostTrustedUserSuccessMatters() throws IllegalArgumentException, IOException {
		assertNull(secretStats.getMostTrustedUser());
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		assertNull(secretStats.getMostTrustedUser());
		
		boolean flag =false;
		try {
			secretService.shareSecret("Bob", secretId1, "Carl");
		}catch(NotAuthorizedException e) {
			flag =true;
		}
		assertTrue(flag);
		assertNull(secretStats.getMostTrustedUser());
		
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Carl", secretStats.getMostTrustedUser());
		
		flag =false;
		try {
			secretService.shareSecret("Dough", secretId1, "Bob");
		}catch(NotAuthorizedException e) {
			flag =true;
		}
		assertTrue(flag);
		assertEquals("Carl", secretStats.getMostTrustedUser());
	}
	
	/**
	 * 4. getWorstSecretKeeper:
	 * 		4.1	If Alice and Bob share the same message with Carl three times each, and Carl shares the same
	 * 			message with Doug, Ed, and Fred, Carl's net sharing balance is 2-3 = -1.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getWorstSecretKeeperHappyCase() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");

		//Alice: 0-1 = -1, Bob: 1-0 = 1
		secretService.shareSecret("Alice", secretId1, "Bob");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-1 = -1, Bob: 1-1 = 0, Carl=1-0 =1
		secretService.shareSecret("Bob", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-1 = -1, Bob: 1-1 = 0, Carl=1-0 =1 
		secretService.shareSecret("Bob", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-0 = 2
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());		
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-1 = 1, Doug = 1-0 = 1
		secretService.shareSecret("Carl", secretId1, "Doug");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-2 = 0, Doug = 1-0 = 1,  Ed = 1-0 = 1
		secretService.shareSecret("Carl", secretId1, "Ed");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-3 = -1, Doug = 1-0 = 1,  Ed = 1-0 = 1 ,  Fred = 1-0 = 1
		secretService.shareSecret("Carl", secretId1, "Fred");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-4 = -2, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1 G =1
		secretService.shareSecret("Carl", secretId1, "Gred");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-5 = -3, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1,  G = 1, H = 1
		secretService.shareSecret("Carl", secretId1, "Hred");
		assertEquals("Carl", secretStats.getWorstSecretKeeper());
		
	}
	
	/**
	 * 4. getWorstSecretKeeper:
	 * 		4.2	Again, sharing/unsharing with one himself does not count here.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getWorstSecretKeeperUnsharingNoEffect() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");

		//Alice: 0
		secretService.shareSecret("Alice", secretId1, "Alice");
		assertNull(secretStats.getWorstSecretKeeper());
		
		//Alice: 0
		secretService.unshareSecret("Alice", secretId1, "Alice");
		assertNull( secretStats.getWorstSecretKeeper());
		
		//Alice: 0-1 = -1, Bob: 1-0 = 1
		secretService.shareSecret("Alice", secretId1, "Bob");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-1 = -1, Bob: 1-1 = 0, Carl=1-0 =1
		secretService.shareSecret("Bob", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-1 = -1, Bob: 1-1 = 0, Carl=1-0 =1 
		secretService.shareSecret("Bob", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-0 = 2
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());		
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-1 = 1, Doug = 1-0 = 1
		secretService.shareSecret("Carl", secretId1, "Doug");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-2 = 0, Doug = 1-0 = 1,  Ed = 1-0 = 1
		secretService.shareSecret("Carl", secretId1, "Ed");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-3 = -1, Doug = 1-0 = 1,  Ed = 1-0 = 1 ,  Fred = 1-0 = 1
		secretService.shareSecret("Carl", secretId1, "Fred");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-4 = -2, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1 G =1
		secretService.shareSecret("Carl", secretId1, "Gred");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-4 = -2, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1 G =1
		secretService.shareSecret("Carl", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-4 = -2, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1 G =1
		secretService.unshareSecret("Alice", secretId1, "Alice");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-5 = -3, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1,  G = 1, H = 1
		secretService.shareSecret("Carl", secretId1, "Hred");
		assertEquals("Carl", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-5 = -3, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1,  G = 1, H = 1
		secretService.shareSecret("Alice", secretId1, "Alice");
		assertEquals("Carl", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-5 = -3, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1,  G = 1, H = 1
		secretService.unshareSecret("Carl", secretId1, "Carl");
		assertEquals("Carl", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-5 = -3, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1,  G = 1, H = 1
		secretService.unshareSecret("Alice", secretId1, "Carl");
		assertEquals("Carl", secretStats.getWorstSecretKeeper());
	}
	
	/**
	 * 4. getWorstSecretKeeper:
	 * 		4.3	the ID of the person with the smallest net sharing balance. If there
	 *         	is a tie, return the 1st of such users based on alphabetical order of
	 *         	the user ID.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getWorstSecretKeeperTie() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");

		//Alice: 0-1 = -1, Bob: 1-0 = 1
		secretService.shareSecret("Alice", secretId1, "Bob");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-1 = -1, Bob: 1-1 = 0, Carl=1-0 =1
		secretService.shareSecret("Bob", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-1 = -1, Bob: 1-1 = 0, Carl=1-0 =1 
		secretService.shareSecret("Bob", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-0 = 2
		secretService.shareSecret("Alice", secretId1, "Carl");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());		
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-1 = 1, Doug = 1-0 = 1
		secretService.shareSecret("Carl", secretId1, "Doug");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-2 = 0, Doug = 1-0 = 1,  Ed = 1-0 = 1
		secretService.shareSecret("Carl", secretId1, "Ed");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Alice: 0-2 = -2, Bob: 1-1 = 0, Carl=2-3 = -1, Doug = 1-0 = 1,  Ed = 1-0 = 1 ,  Fred = 1-0 = 1
		secretService.shareSecret("Carl", secretId1, "Fred");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//Tie A & C
		//A: 0-2 = -2, B: 1-1 = 0, C=2-4 = -2, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1 G =1
		secretService.shareSecret("Carl", secretId1, "Gred");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
		//A: 0-2 = -2, B: 1-1 = 0, C=2-5 = -3, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1,  G = 1, H = 1
		secretService.shareSecret("Carl", secretId1, "Hred");
		assertEquals("Carl", secretStats.getWorstSecretKeeper());
		
		//Tie A & C
		//A: 0-3 = -3, B: 1-1 = 0, C=2-5 = -3, D = 1-0 = 1,  E = 1-0 = 1 ,  F = 1-0 = 1 G =1
		secretService.shareSecret("Alice", secretId1, "Gred");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
	}
	
	/**
	 * 4. getWorstSecretKeeper:
	 * 		4.1	If Alice and Bob share the same message with Carl three times each, and Carl shares the same
	 * 			message with Doug, Ed, and Fred, Carl's net sharing balance is 2-3 = -1.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getWorstSecretKeeperNullTillNoSharing() throws IllegalArgumentException, IOException {
		assertNull(secretStats.getWorstSecretKeeper());
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		assertNull(secretStats.getWorstSecretKeeper());
		UUID secretId2 = secretService.createSecret("Bob", "Hello This is secret");
		assertNull(secretStats.getWorstSecretKeeper());
		
		boolean flag =false;
		try {
			secretService.shareSecret("Bob", secretId1, "Alice");
		} catch (NotAuthorizedException e) {
			flag = true;
		}
		assertTrue(flag);
		assertNull(secretStats.getWorstSecretKeeper());
		
		secretService.shareSecret("Bob", secretId2, "Alice");
		assertEquals("Bob", secretStats.getWorstSecretKeeper());
		
		secretService.shareSecret("Alice", secretId1, "Bob");
		assertEquals("Alice", secretStats.getWorstSecretKeeper());
		
	}
	
	/**
	 * 5. getBestKnownSecret:
	 * 	  	5.1	Returns the secret that has been successfully read by the biggest number of
	 * 			different users, OTHER THAN the creator himself.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getBestKnownSecretHappy() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		UUID secretId2 = secretService.createSecret("Doug", "And this is World");
		
		assertNull(secretStats.getBestKnownSecret());
		secretService.readSecret("Alice", secretId1);
		assertNull(secretStats.getBestKnownSecret());
		boolean flag =false;
		try {
			secretService.readSecret("Bob", secretId1);
		}catch(NotAuthorizedException e) {
			flag = true;
		}
		assertTrue(flag);
		assertNull(secretStats.getBestKnownSecret());
		secretService.shareSecret("Alice", secretId1,"Bob");
		secretService.shareSecret("Alice", secretId1,"Carl");
		secretService.readSecret("Bob", secretId1);
		
		assertEquals("Hello This is secret", secretStats.getBestKnownSecret());
		secretService.readSecret("Doug", secretId2);
		assertEquals("Hello This is secret",secretStats.getBestKnownSecret());
		
		secretService.shareSecret("Doug", secretId2,"Bob");
		secretService.readSecret("Bob", secretId2);
		assertEquals("And this is World", secretStats.getBestKnownSecret());
		secretService.readSecret("Bob", secretId1);
		assertEquals("And this is World", secretStats.getBestKnownSecret());
		secretService.readSecret("Alice", secretId1);
		assertEquals("And this is World", secretStats.getBestKnownSecret());
		secretService.readSecret("Carl", secretId1);
		assertEquals("Hello This is secret",secretStats.getBestKnownSecret());
		
	}
	
	/**
	 * 5.2 	If the same secret is read by the same user more than once successfully, it is still considered 
	 * 		as one.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getBestKnownSecretReadSameMessage() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		UUID secretId2 = secretService.createSecret("Doug", "And this is World");
		secretService.shareSecret("Alice", secretId1,"Bob");
		secretService.shareSecret("Alice", secretId1,"Carl");
		secretService.shareSecret("Doug", secretId2,"Carl");
		
		secretService.readSecret("Carl", secretId2);
		assertEquals("And this is World", secretStats.getBestKnownSecret());
		secretService.readSecret("Bob", secretId1);
		assertEquals("And this is World",secretStats.getBestKnownSecret());
		secretService.readSecret("Bob", secretId1);
		secretService.readSecret("Bob", secretId1);
		secretService.readSecret("Bob", secretId1);
		assertEquals("And this is World",secretStats.getBestKnownSecret());
		secretService.readSecret("Carl", secretId1);
		assertEquals("Hello This is secret",secretStats.getBestKnownSecret());
	}
	
	/**
	 * 5.3 	If Alice shares a secret with Bob, Bob reads this secret, and later Alice
	 * 		unshares it from Bob, Bob's read still counts because it was successful.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getBestKnownSecretUnsharing() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		secretService.shareSecret("Alice", secretId1,"Bob");
		secretService.readSecret("Bob", secretId1);
		assertEquals("Hello This is secret",secretStats.getBestKnownSecret());
		secretService.unshareSecret("Alice", secretId1,"Bob");
		assertEquals("Hello This is secret",secretStats.getBestKnownSecret());
	}
	
	/**
	 * 5.4 If no secrets are ever read by users other than the creators, return null.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getBestKnownSecretNull() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		assertNull(secretStats.getBestKnownSecret());
		secretService.readSecret("Alice", secretId1);
		assertNull(secretStats.getBestKnownSecret());
		
	}
	
	/**
	 * 5.4 If no secrets are ever read by users other than the creators, return null.
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void getBestKnownSecretTie() throws IllegalArgumentException, IOException {
		UUID secretId1 = secretService.createSecret("Alice", "Hello This is secret");
		UUID secretId2 = secretService.createSecret("Doug", "And this is World");
		secretService.shareSecret("Alice", secretId1,"Bob");
		secretService.shareSecret("Doug", secretId2,"Bob");
		
		secretService.readSecret("Bob", secretId1);
		assertEquals("Hello This is secret",secretStats.getBestKnownSecret());
		secretService.readSecret("Bob", secretId2);
		assertEquals("And this is World",secretStats.getBestKnownSecret());
		
	}
	
	
}
