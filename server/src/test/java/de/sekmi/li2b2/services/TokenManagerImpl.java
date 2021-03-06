package de.sekmi.li2b2.services;

import java.security.Principal;

import de.sekmi.li2b2.services.token.AbstractTokenManager;

public class TokenManagerImpl extends AbstractTokenManager<Principal>{

	@Override
	public Principal createPrincipal(String name) {
		return new Principal() {
			@Override
			public String getName() {
				return name;
			}
		};
	}

}
