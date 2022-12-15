package com.fhb.fis.rollback.transfer.util;


import java.util.Arrays;

public enum ApplicationCodes {

	DEPOSITS("SAV", "DDA", "MMA", "TCD"),
	LOANS("LOC"),
	COMMERCIAL_LOANS("ODP", "ILN", "LOC NTE"),
	CARD("CCD", "CCP", "CCS"),
	MORTGAGE("MTG"),
	UNKNOWN;
	
	private String[] codes;
	
	private ApplicationCodes(String... codes) {
		this.codes = codes;
	}

	/**
	 * Obtains the enum value from the enum code (e.g.: DDA, CCD,..)
	 * @param code
	 * @return
	 */
	public static ApplicationCodes fromCode(String code) {

		return doFromCode(code);
	}
    
	/**
	 * Obtains the enum value from the enum name (e.g.: DEPOSITS, LOANS,..)
	 * @param code
	 * @return
	 */
    public static ApplicationCodes fromName(String name) {
    	return doFromName(name);
    }
    
    public static ApplicationCodes fromCodeOrName(String codeOrName) {
    	
    	ApplicationCodes result = doFromCode(codeOrName);
    	
    	if (null == result || ApplicationCodes.UNKNOWN.equals(result)) {
    		
    		result = doFromName(codeOrName);
    		
    	}
    	
    	return result;
    }
    

	/**
	 * Obtains the enum value from the enum name code (e.g.: DDA, CCD,..) or the name (e.g.: DEPOSITS, LOANS,..).
	 * 
	 * @param code
	 * @return
	 */
    private static ApplicationCodes doFromCode(String code) {

	    ApplicationCodes result = ApplicationCodes.UNKNOWN; 
	    
	    if (null != code) {
	        
	        //Match by full name
    	    for (ApplicationCodes applicationCode: ApplicationCodes.values()) {
    	        
    	        //Cases like CCD or CCD REG should be resolved to CCD
    	        if (Arrays.stream(applicationCode.codes).anyMatch(code::equals)) {
    	            
    	            result = applicationCode;
    	            
    	            break;
    	        } 
    	    }
    	    
    	    //If no match, match by first 3
    	    if (ApplicationCodes.UNKNOWN == result && null  != code && code.length() > 3)
                for (ApplicationCodes applicationCode: ApplicationCodes.values()) {
                    
                    //Cases like CCD or CCD REG should be resolved to CCD
                    if (Arrays.stream(applicationCode.codes).anyMatch(code.subSequence(0, 3)::equals)) {
                        
                        result = applicationCode;
                        
                        break;                  
                        
                    }
                }
	    }
	    
	    return result;
    }
    
    private static ApplicationCodes doFromName(String name) {

        ApplicationCodes result = ApplicationCodes.UNKNOWN; 
        
        if (null != name) {
            for (ApplicationCodes applicationCode: ApplicationCodes.values()) {
                
                if (applicationCode.name().equals(name)) {
                    
                    result = applicationCode;
                    
                    break;
                }
                
            }
        }
        
        return result;
    }
	
}
