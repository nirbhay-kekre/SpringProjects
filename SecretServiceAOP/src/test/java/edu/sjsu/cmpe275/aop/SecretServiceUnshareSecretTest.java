package edu.sjsu.cmpe275.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Ushare secret test cases<br>
 * Expected : 		<p>Unshare the current user's secret with another user. A user can ONLY unshare
 * 					a secret that he has created. Unsharing a message one has created with
 * 					himself is allowed but silently ignored, as one always has access to the
 * 					messages he has created.</p>
 * 
 * Network Failure: IOException
 * 
 * Validation:		IllegalArgumentException if any argument is null
 * 	1.	userId is null
 * 	2.	secretId is null
 * 	3.	targetUserId is null
 * 	4.	All userId, secretId, targetUserId are null
 * 
 * NotAuthorizedException:
 * 	1.  If the user with userId has not created the given secret.
 * 	2. 	If there does not exist a secret with the given UUID.
 *
 * Happy Case:
 * 	1. If the user with userId has created the given secret and trying to unshare without sharing
 * 	2. If the user with userId has created the given secret and trying to unshare
 *  3. self unsharing should be silently ignored
 * 
 * @author nirbhaykekre
 */
public class SecretServiceUnshareSecretTest extends BaseTestClass {
	/**
	 * Validation:
	 * 1. userId is null
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void unshareSecretInvalidUserId() throws IllegalArgumentException, IOException {
		UUID id = secretService.createSecret("2123", "hello");
		secretService.unshareSecret(null, id, "123");
	}
	
	/**
	 * Validation:
	 * 2. secretId is null
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void unshareSecretInvalidSecretId() throws IllegalArgumentException, IOException {
		secretService.unshareSecret("321", null, "123");
	}
	
	/**
	 * Validation:
	 * 3. targetUserId is null
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void unshareSecretInvalidTargetUserId() throws IllegalArgumentException, IOException {
		UUID id = secretService.createSecret("2123", "hello");
		secretService.unshareSecret("321", id, null);
	}

	/**
	 * Validation:
	 * 4. All userId, secretId, targetUserId are null
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void unshareSecretInvalidAll() throws IllegalArgumentException, IOException {
		secretService.unshareSecret(null, null, null);
	}
	
	/**
	 * Authorized Check
	 * 	1.  If the user with userId has not created the given secret.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected= NotAuthorizedException.class)
	public void unshareSecretNotCreatedByUser() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1, id2, id3;
		try {
			id1 = secretService.createSecret("user1", "Hello World!");
			id2 = secretService.createSecret("user2", "Hello World2");
			id3 = secretService.createSecret("user3", "Hello World2");
			secretService.shareSecret("user1", id1, "user2");
			secretService.shareSecret("user1", id1, "user3");
		}catch(NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		// user2 & 3 has access of id1 but since id1 is not created by user2, he can't
		// share it with user3
		secretService.unshareSecret("user2", id1, "user3");
	}
	
	/**
	 * Authorized Check
	 * 	2. 	If there does not exist a secret with the given UUID.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected= NotAuthorizedException.class)
	public void unshareNonExistingSecret() throws IllegalArgumentException, NotAuthorizedException, IOException {
		secretService.unshareSecret("user2", UUID.randomUUID(), "user3");
	}
	
	/**
	 * Happy Case: 
	 * 1. 	If the user with userId has created the given secret and
	 * 		trying to unshare without sharing
	 * 
	 * @throws IOException 
	 * @throws NotAuthorizedException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void unshareSecretSuccessWithoutSharing() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1, id2, id3;
		try {
			id1 = secretService.createSecret("user1", "Hello World!");
			id2 = secretService.createSecret("user2", "Hello World2");
		}catch(NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		secretService.unshareSecret("user1", id1, "user2");
	}
	
	/**
	 * Happy Case: 
	 * 2. 	If the user with userId has created the given secret and
	 * 		trying to unshare
	 * 
	 * @throws IOException 
	 * @throws NotAuthorizedException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void unshareSecretSuccess() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1, id2;
		try {
			id1 = secretService.createSecret("user1", "Hello World!");
			id2 = secretService.createSecret("user2", "Hello World2");
			secretService.shareSecret("user1", id1, "user2");
		}catch(NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		Secret secret = secretService.readSecret("user2", id1);
		assertNotNull(secret);
		assertEquals("Hello World!", secret.getContent());
		boolean flag = false;
		try {
			secretService.unshareSecret("user2", id1, "user3");
		}catch(NotAuthorizedException e) {
			flag = true;
		}
		assertTrue(flag);
	}
	
	/**
	 * Happy Case: 
	 * 3. 	Self unsharing is silently ignored
	 * 
	 * @throws IOException 
	 * @throws NotAuthorizedException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void unshareSecretSelfSuccess() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1, id2;
		id1 = secretService.createSecret("user1", "Hello World!");
		
		Secret secret = secretService.readSecret("user1", id1);
		assertNotNull(secret);
		assertEquals("Hello World!", secret.getContent());
		
		secretService.unshareSecret("user1", id1, "user1");
		
		secret = secretService.readSecret("user1", id1);
		assertNotNull(secret);
		assertEquals("Hello World!", secret.getContent());
	}
	
	/**
	 * Network failure
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test(expected = IOException.class)
	@Ignore
	public void unshareSecretNetworkFailure() throws IllegalArgumentException, IOException {
		UUID id = null ;
		try{
			id = secretService.createSecret("user1", "hello");
		}catch(Exception e) {
			
		}
		secretService.unshareSecret("user1", id, "user1");
	}
}
