.packageName = "GmmlR"
.debug = TRUE;
#######################################################
################## Utility functions ##################
#######################################################

#### OOP Utils ####
createMethod = function(methodName, signature, fn) {
	if(.debug) print(paste(">> creating method",methodName,"with signature",signature))
	if(!isGeneric(methodName)) {
		# Create generic for this method
		if(is.null(getGeneric(methodName))) { #Check if exists as Primitive
			if(exists(methodName, mode="function")) { #Check if exists as function
				if(.debug) print(paste(methodName,"is already defined as a function, calling makeStandardGeneric"))
				fun = eval(parse(text=methodName))
				fun = makeStandardGeneric(methodName, fun)
			}
			else  { #If nothing is defined yet, create a standardGeneric function
				if(.debug) print(paste(methodName,"is not yet defined as function, creating standardGeneric function"))
				fun = createStandardGeneric(fn, methodName)
			}
			if(.debug) print(paste("setting Generic for",methodName, " >function:"))
			if(.debug) print(fun)
			setGeneric(methodName, fun, where=parent.env(environment()))
		} else {
			#if(.debug) print(paste(methodName,"was a primitive, now creating generic"))
			#setGeneric(methodName, where=parent.env(environment())) #Exists as Primitive, set as Generic
		}
	}
	
	setMethod(methodName, signature, fn, where=parent.env(environment()))
}

createReplaceMethod = function(methodName, objectName, fn) {
	createMethod(paste(methodName, "<-", sep = ""), objectName, fn)
}

createStandardGeneric = function(fn, fname) {
	if(.debug) print(paste("Creating standard generic for",fname))
	fdef = "function("
	args = names(formals(fn))
	for(arg in args) { 
		fdef = paste(fdef, paste(arg, ","), sep="")
	}
	fdef = substr(fdef,1,nchar(fdef)-1) # Remove last comma
	if(length(grep("\\.\\.\\.", fdef)) == 0) 
		fdef = paste(fdef, "...", sep=",") # Add ... argument if not already defined
	fdef = paste(fdef, ") standardGeneric('", fname, "')", sep="")
	if(.debug) print(paste("evaluating function definition:",fdef))
	eval(parse(text=fdef))
}

