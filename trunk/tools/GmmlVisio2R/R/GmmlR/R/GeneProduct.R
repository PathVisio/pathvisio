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
	TRUE
})

######################
#### Constructors ####
######################
GeneProduct <- function(refs) {
		colnames(refs) = c("id", "code")
		gp =	new("GeneProduct", refs)		
}

#################
#### Methods ####
#################

## Other ##
createMethod("name", "GeneProduct", function(x, ...) {
	validObject(x, test=TRUE)
	if(nrow(x) < 1) return(NA)
	paste(x[1,2],x[1,1],sep=":") # code:id
})

createReplaceMethod("addReference", c("GeneProduct", "vector"), function(x, value, ...) {
	rbind(x, value)
})

## Generic and Primitive implementations ##
createMethod("==", c("GeneProduct", "GeneProduct"), function(e1, e2) {
	validObject(e1, test=TRUE)
	validObject(e2, test=TRUE) #generates error when not valid
	
	for(i in 1:nrow(e1)) for(j in 1:nrow(e2)) 
		if(e1[i,1] == e2[j,1] && e1[i,2] == e2[j,2]) return(TRUE)
	FALSE
})
