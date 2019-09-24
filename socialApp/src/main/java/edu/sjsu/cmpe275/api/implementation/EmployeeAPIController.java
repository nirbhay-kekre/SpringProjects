package edu.sjsu.cmpe275.api.implementation;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import edu.sjsu.cmpe275.api.controller.interfaces.IEmployeeAPI;
import edu.sjsu.cmpe275.api.model.Employee;

@Controller
public class EmployeeAPIController implements IEmployeeAPI {

	@Override
	public ResponseEntity<Employee> getEmployee(Long id, String format) {
		String type = "application/"+format;
				HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", type+"; charset=UTF-8");
		return new ResponseEntity<Employee>(new Employee(), headers, HttpStatus.NOT_IMPLEMENTED);
	}

	@Override
	public ResponseEntity<Employee> deleteEmployee(Long id, String format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<Employee> createEmployee(String name, String email, String title, String street, String city,
			String state, String zip, String employerId, String managerId, String format) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ResponseEntity<Employee> updateEmployee(String name, String email, String title, String street, String city,
			String state, String zip, String employerId, String managerId, String format) {
		// TODO Auto-generated method stub
		return null;
	}

}
