package edu.sjsu.cmpe275.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;

/**
* ReadSecret test cases
* 
* Expected: 
* Happy Case:
* 		1.	Reads a secret by its ID. A user can read a secrete that he has created.
* 		2. 	Reads a secret by its ID. A user can read a secrete that has been shared with him.
* 
* Validation: 	Throws IllegalArgumentException if any argument is null.
* 		1. User Id is null
* 		2. Secret Id is null
* 		3. Both User Id and Secret Id are null
* 
* Network Failure: Throws IOException if there is a network failure.
* 
* NotAuthorizedException: 	
* 		1. 	If the given user has not created the SecretId which is asked.
* 		2. 	If the secret Id is not shared with him.
* 		3. 	If secretId does not exist
* 		4. 	Try to read a secret which is unshared with him.
* 
* 
* @author nirbhaykekre
*/
public class SecretServiceReadSecretTest extends BaseTestClass{
	/**
	 * Validation Test
	 * 1. User Id is null
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void readSecretNullUserId() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id ;
		//putting try catch to make sure we are validating IllegalArgumentException of readSecret
		try{
			id =secretService.createSecret("user1", "hello");
		}catch(IllegalArgumentException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		secretService.readSecret(null, id);
	}

	/**
	 * Validation Test
	 * 2. Secret Id is null
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void readSecretNullSecretId() throws IllegalArgumentException, NotAuthorizedException, IOException {
		secretService.readSecret("user1", null);
	}

	/**
	 * Validation Test
	 * 3. Both User Id and Secret Id are null
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void readSecretNullSecretIdAndUserId() throws IllegalArgumentException, NotAuthorizedException, IOException {
		secretService.readSecret(null, null);
	}

	
	/**
	 * Network failure Test
	 * IOException is expected
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = IOException.class)
	@Ignore
	public void readSecretNetworkFailure() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id = null ;
		try{
			id = secretService.createSecret("user1", "hello");
		}catch(Exception e) {
			
		}
		secretService.readSecret("user1", id);
	}


	/**
	 * Authorization Test
	 * 1. 	If the given user has not created the SecretId which is asked.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = NotAuthorizedException.class)
	public void readSecretNotCreatedByUserNotAuth()
			throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1, id2;
		//putting try catch to make sure we are validating not authorized exception of readSecret
		try {
			id1 = secretService.createSecret("user1", "hello");
			id2 = secretService.createSecret("user2", "world");
		} catch (NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		//user2 is trying to access id1 which is not created by him 
		secretService.readSecret("user2", id1);
	}

	/**
	 * Authorization Test
	 * 2. If the secret Id is not shared with him.
	 *  
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = NotAuthorizedException.class)
	public void readSecretNotSharedWithUserNotAuth() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1,id2;
		//putting try catch to make sure we are validating not authorized exception of readSecret
		try {
			id1 = secretService.createSecret("user1", "hello");
			id2 = secretService.createSecret("user2", "world");
			
			//user1 shared secret id1 with user2
			secretService.shareSecret("user1", id1, "user2");
		} catch (NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

		//user1 tries to access user2's secret which is not shared with him
		secretService.readSecret("user1", id2);
	}

	/**
	 * Authorization Test
	 * 3. If secretId does not exist
	 *  
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test(expected = NotAuthorizedException.class)
	public void readNotExistingSecretNotAuth()
			throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1;
		//putting try catch to make sure we are validating not authorized exception of readSecret
		try {
			id1 = secretService.createSecret("user1", "hello");
		} catch (NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		secretService.readSecret("user1", UUID.randomUUID());
	}
	
	/**
	 * Authorization Test<br>
	 * 4. If user tries to Read a secret which is unshared with him.
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
			Secret s = secretService.readSecret("user2", id1);
			assertNotNull(s);
			assertEquals("Hello World!", s.getContent());
			secretService.unshareSecret("user1", id1, "user2");
		} catch (Exception e) {
			throw new RuntimeException();
		}
		secretService.readSecret("user2", id1);
	}
	
	/**
	 * Happy test
	 * 1. Reads a secret by its ID. A user can read a secrete that he has created.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test
	public void readCreatedSecretSuccess() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1;
		//putting try catch to make sure we are validating readSecret
		try {
			id1 = secretService.createSecret("user1", "hello");
		} catch (NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		Secret sec = secretService.readSecret("user1", id1);
		assertNotNull(sec);
		assertEquals("hello", sec.getContent());

	}
	
	/**
	 * Happy test
	 * 2. Reads a secret by its ID. A user can read a secrete that has been shared with him.
	 * 
	 * @throws IllegalArgumentException
	 * @throws NotAuthorizedException
	 * @throws IOException
	 */
	@Test
	public void readSharedSecretSuccess() throws IllegalArgumentException, NotAuthorizedException, IOException {
		UUID id1,id2;
		//putting try catch to make sure we are validating readSecret
		try {
			id1 = secretService.createSecret("user1", "hello");
			id2 = secretService.createSecret("user2", "World");
			secretService.shareSecret("user1", id1, "user2");
		} catch (NotAuthorizedException e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
		Secret sec = secretService.readSecret("user2", id1);
		assertNotNull(sec);
		assertEquals("hello", sec.getContent());

	}
}
