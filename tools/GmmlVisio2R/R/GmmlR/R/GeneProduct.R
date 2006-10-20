#######################################################
################## GeneProduct class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("GeneProduct", contains = "matrix", 
		prototype(matrix(ncol=2, dimnames = list(NULL, c("ids", "code"))))
)

## Validity:
## 1: length(ids) == length(systems)
setValidity("GeneProduct", function(object) {
	if(ncol(object) != 2) return("malformed matrix")
	if(is.null(rownames(object))) return("no row names, should be string of the form 'code:id'")
	TRUE
})

######################
#### Constructors ####
######################
GeneProduct <- function(refs) {
		if(is.null(dim(refs))) return(GeneProductFromString(refs))
		colnames(refs) = c("id", "code")
		rownames(refs) = getRowNames(refs)
		gp =	new("GeneProduct", refs)		
}

GeneProductFromString = function(str) {
	idcode = parseGpString(str)
	gp = GeneProduct(rbind(idcode))
	rownames(gp) = str
	gp
}

##################
#### Functions ###
##################
parseGpString = function(str) {
	rev(strsplit(str, ":")[[1]])
}

getGpString = function(idcode) {
	paste(idcode[2],":",idcode[1],sep="")
}

getRowNames = function(refs) {
	apply(refs, 1, function(ref) getGpString(ref))
}

#################
#### Methods ####
#################

## Other ##
createMethod("name", "GeneProduct", function(x, ...) {
	validObject(x, test=TRUE)
	if(nrow(x) < 1) return(NA)
	nm = getGpString(x[1,])
	if(nrow(x) > 1) nm = paste(nm, "...", nrow(x), "more references");
	nm
})

createReplaceMethod("addReference", c("GeneProduct", "character"), function(x, value, ...) {
	if(length(value) == 1) value = parseGpString(value)
	print(matrix(value, ncol = 2))
	addReference(x) = matrix(value, ncol = 2)
	x
})

createReplaceMethod("addReference", c("GeneProduct", "matrix"), function(x, value, ...) {
	rownames(value) = getRowNames(value)
	GeneProduct(rbind(x, value))
})

## Generic and Primitive implementations ##
createMethod("==", c("GeneProduct", "GeneProduct"), function(e1, e2) {
	validObject(e1, test=TRUE)
	validObject(e2, test=TRUE) #generates error when not valid
	e2refs = rownames(e2);
	for(ref in rownames(e1)) if(ref %in% e2refs) return(TRUE)
	FALSE
})

createMethod("==", c("GeneProduct", "character"), function(e1, e2) {
	validObject(e1, test=TRUE)
	if(length(e2) > 1) getGpString(e2) %in% rownames(e1)
	else e2 %in% rownames(e1)
})

createMethod("==", c("character", "GeneProduct"), function(e1, e2) {
	e2 == e1
})
