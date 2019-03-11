package edu.sjsu.cmpe275.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Share a secret with another user. A user can share a secrete that he has
 * created or has been shared with.
 * 
 * Happy Case:
 * 	1. Share a secrete successfully which the user has created.
 * 	2. Share a secrete successfully which is shared with the user with userId.
 * 
 * Network: 	IOException if there is a network failure
 * 
 * Validation: 	IllegalArgumentException if any argument is null
 * 	1. UserId is null
 * 	2. SecretId is null
 * 	3. Target UserId is null
 * 	4. All userID, secretId, targetUserId are null
 * 
 * NotAuthorizedException   
 * 	1. if the user with userId has not created SecretId
 *  2. Trying to share a secret which is not shared with him.
 *  3. If there does not exist a secret with the given UUID.
 *	4. If user tries to share a secret which is unshared with him.
 *
 * @author nirbhaykekre
 */
public class SecretServiceShareSecretTest extends BaseTestClass {
	/**
	 * Validation
	 * 1. UserId is null
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException if any argument is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public void shareSecretInvalidUserId() throws IllegalArgumentException, IOException {
		UUID id;
		try {
			id = secretService.createSecret("2123", "hello");
		}catch(IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		
		secretService.shareSecret(null, id, "123");
	}

	/**
	 * Validation
	 * 2. SecretId is null
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException if any argument is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public void shareSecretInvalidSecretId() throws IllegalArgumentException, IOException {
		secretService.shareSecret("321", null, "123");
	}

	/**
	 * Validation
	 * 3. Target UserId is null
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException if any argument is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public void shareSecretInvalidTargetUserId() throws IllegalArgumentException, IOException {
		UUID id;
		try {
			id = secretService.createSecret("2123", "hello");
		}catch(IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		secretService.shareSecret("321", id, null);
	}

	/**
	 * Validation
	 * 4. All userID, secretId, targetUserId are null
	 * 
	 * @throws IOException
	 * @throws IllegalArgumentException if any argument is null
	 */
	@Test(expected = IllegalArgumentException.class)
	public void shareSecretInvalidAll() throws IllegalArgumentException, IOException {
		secretService.shareSecret(null, null, null);
	}
	
	/**
	 * Authorization Check   
	 * 	1. if the user with userId has not created SecretId
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = NotAuthorizedException.class)
	public void shareSecretNotCreated() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1, id2;
		try {
			id1 = secretService.createSecret("user1", "Hello World!");
			id2 = secretService.createSecret("user2", "Hello World2");
		}catch(NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		secretService.shareSecret("user1", id2, "user2");
	}
	
	/**
	 * Authorization Check<br>   
	 * 	2. Trying to share a secret which is not shared with him.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = NotAuthorizedException.class)
	public void shareSecretNotShared() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1, id2;
		try {
			id1 = secretService.createSecret("user1", "Hello World!");
			id2 = secretService.createSecret("user2", "Hello World2");
			secretService.shareSecret("user1", id1, "user2");
			
		}catch(NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		//user1 does not have access of id2
		secretService.shareSecret("user1", id2, "user2");
	}
	
	/**
	 * Authorization Check<br>   
	 * 	3. If there does not exist a secret with the given UUID.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = NotAuthorizedException.class)
	public void shareSecretNotExisting() throws IllegalArgumentException, NotAuthorizedException, IOException {

		secretService.shareSecret("user1", UUID.randomUUID(), "user2");
	}
	
	/**
	 * Happy Case<br>   
	 * 	1. Share a secrete successfully which the user has created.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test
	public void shareExistingSecret() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1, id2;
		try {
			id1 = secretService.createSecret("user1", "Hello World!");
			id2 = secretService.createSecret("user2", "Hello World2");
		}catch(NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		secretService.shareSecret("user1", id1, "user2");
		Secret secret = secretService.readSecret("user2", id1);
		assertNotNull(secret);
		assertEquals("Hello World!", secret.getContent());
		
		//Making sure the secrete that was already created by him is still accessible 
		secret = secretService.readSecret("user2", id2);
		assertNotNull(secret);
		assertEquals("Hello World2", secret.getContent());
	}
	
	/**
	 * Happy Case<br>   
	 * 	1. Share a secrete successfully which the user has created.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test
	public void shareSharedSecret() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1, id2, id3;
		try {
			id1 = secretService.createSecret("user1", "Hello World!");
			id2 = secretService.createSecret("user2", "Hello World2");
			id3 = secretService.createSecret("user3", "Hello World3");
			secretService.shareSecret("user1", id1, "user2");
			
		}catch(NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		secretService.shareSecret("user2", id1, "user3");
		
		Secret secret = secretService.readSecret("user3", id1);
		assertNotNull(secret);
		assertEquals("Hello World!", secret.getContent());
		
		//Making sure the secrete that was already created by sharee is still accessible 
		secret = secretService.readSecret("user3", id3);
		assertNotNull(secret);
		assertEquals("Hello World3", secret.getContent());
	}
	
	/**
	 * Happy Case: 
	 * 3. 	Self Sharing is silently ignored
	 * 
	 * @throws IOException 
	 * @throws NotAuthorizedException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void shareSecretSelfSuccess() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1;
		id1 = secretService.createSecret("user1", "Hello World!");
		
		Secret secret = secretService.readSecret("user1", id1);
		assertNotNull(secret);
		assertEquals("Hello World!", secret.getContent());
		
		secretService.shareSecret("user1", id1, "user1");
		
		secret = secretService.readSecret("user1", id1);
		assertNotNull(secret);
		assertEquals("Hello World!", secret.getContent());
	}
	
	
	/**
	 * Authorization check<br>
	 * 4. If user tries to share a secret which is unshared with him.
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = NotAuthorizedException.class)
	public void shareAfterUnsharing() throws IllegalArgumentException, IOException {
		UUID id1;
		try {
		id1 = secretService.createSecret("user1", "Hello World!");
		secretService.shareSecret("user1", id1, "user2");
		secretService.shareSecret("user2", id1, "user3");
		secretService.unshareSecret("user1", id1, "user2");
		}catch(Exception e) {
			throw new RuntimeException();
		}
		secretService.shareSecret("user2", id1, "user4");
	}
	/**
	 * Network failure
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test(expected = IOException.class)
	@Ignore
	public void shareSecretNetworkFailure() throws IllegalArgumentException, IOException {
		UUID id = null ;
		try{
			id = secretService.createSecret("user1", "hello");
		}catch(Exception e) {
			
		}
		secretService.shareSecret("user1", id, "user1");
	}
}
