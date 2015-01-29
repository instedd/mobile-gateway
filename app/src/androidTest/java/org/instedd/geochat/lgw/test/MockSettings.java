package org.instedd.geochat.lgw.test;

import org.instedd.geochat.lgw.ISettings;

public class MockSettings implements ISettings {

	private Boolean addPlusToOutgoing;
	
	public MockSettings(Boolean addPlusToOutgoing){
		this.addPlusToOutgoing = addPlusToOutgoing;
	}
	
	@Override
	public Boolean storedAddPlusToOutgoing() {
		return addPlusToOutgoing;
	}

}
