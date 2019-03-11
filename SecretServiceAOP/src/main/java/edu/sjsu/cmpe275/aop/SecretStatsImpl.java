package edu.sjsu.cmpe275.aop;

import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;

public class SecretStatsImpl implements SecretStats {
	@Autowired
	SecretService secretService;

	@Override
	public void resetStatsAndSystem() {
		/*
		System.out.println("Reseting secrets in Secret Service");
		if (AopUtils.isAopProxy(secretService) && secretService instanceof Advised) {
			Advised advised = (Advised) secretService;
			try {
				((SecretServiceImpl) advised.getTargetSource().getTarget()).secrets.clear();
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException();
			}
		}else{
			((SecretServiceImpl) secretService).secrets.clear();
		}
		*/
	}

	@Override
	public int getLengthOfLongestSecret() {
		return 0;
	}

	@Override
	public String getMostTrustedUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getWorstSecretKeeper() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getBestKnownSecret() {
		// TODO Auto-generated method stub
		return null;
	}

}
