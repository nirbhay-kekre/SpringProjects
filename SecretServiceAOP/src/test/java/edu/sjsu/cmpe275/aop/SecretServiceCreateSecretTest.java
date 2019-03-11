package edu.sjsu.cmpe275.aop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;


/**
 * Create Secret test cases
 * 
 * Expected:
 * Happy case:	Creates a secret in the service. A new Secret object is created, identified
 * 				by randomly generated UUID, with the current user as the owner of the secret.
 * 				1. Check content after creating secret.
 * 				2. same content should have different id.
 *  
 * Network Failure: Throws IOException if there is a network failure.
 * 
 * Validation: Throw IllegalArgumentException
 * 		1. if the userId is null.
 * 		2. the secretContent is more than 100 characters.
 * 		3. userId is null as well as SecretContent is more than 100 characters
 *
 * @author nirbhaykekre
 *
 */
public class SecretServiceCreateSecretTest extends BaseTestClass {
	
	
	/**
	 * Validation Test 
	 * 1. if the userId is null.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createSecretValidationInvalidUserIdTest() throws IllegalArgumentException, IOException {
		secretService.createSecret(null, "InvalidUserId");
	}

	/**
	 * Validation Test 
	 * 2. the secretContent is more than 100 characters.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createSecretValidationInvalidSecretContentTest() throws IllegalArgumentException, IOException {
		secretService.createSecret("123",
				"01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
	}

	/**
	 * Validation Test 
	 * 3. userId is null as well as SecretContent is more than 100 characters
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void createSecretValidationInvalidUserIdAndSecretContentTest() throws IllegalArgumentException, IOException {
		secretService.createSecret(null,
				"01234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789");
	}
	
	/**
	 * Network Failure Test
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test(expected = IOException.class)
	@Ignore
	public void createSecretNetworkFailure() throws IllegalArgumentException, IOException {
		secretService.createSecret("2123", "hello");
	}

	/**
	 * Happy case 
	 * 1. Check content after creating secret.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IOException
	 */
	@Test
	public void createSecretSuccess() throws IllegalArgumentException, IOException {
		UUID id1 = secretService.createSecret("user1", "hello");
		assertNotNull(id1);
		Secret sec = secretService.readSecret("user1", id1);
		assertNotNull(sec);
		assertEquals("hello", sec.getContent());
	}
	
	
	/**
	 * Happy Case:
	 * 2. same content should have different id. i.e, no duplication check
	 * 
	 * @throws IOException 
	 * @throws IllegalArgumentException 
	 */
	@Test
	public void createSecretSuccessNoDuplication() throws IllegalArgumentException, IOException {
		UUID id1 = secretService.createSecret("user1", "hello");
		assertNotNull(id1);
		UUID id2 = secretService.createSecret("user1", "hello");
		assertNotNull(id2);
		assertNotEquals(id1, id2);
	}
}