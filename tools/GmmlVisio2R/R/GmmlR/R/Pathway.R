#######################################################
################## Pathway class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("Pathway", contains = "list",
	representation(
		name = "character",
		fileName = "character"
	),
	prototype(
		name = character(),
		fileName = character()
	)
)

## Validity:
## 1: all elements in @geneProducts must be instance of class GeneProduct
setValidity("Pathway", function(object) {
	for(gp in object)
		if(!isClass("GeneProduct", gp)) return("list @geneProduct contains non-GeneProduct objects")
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("Pathway", 
	function(name = character(), fileName = character(), geneProducts = list()) {
		new("Pathway", geneProducts, name = name, fileName = fileName)
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "Pathway", function(x) x@name)
createMethod("fileName", "Pathway", function(x) x@fileName)

## Setters ##)
createReplaceMethod("name", "Pathway", function(x, value) {
	x@name = value
	x
})
createReplaceMethod("fileName", "Pathway", function(x, value) {
	x@fileName = value
	x
})

## Generic and Primitive implementations ##
setMethod("print", "Pathway", function(x, ...) {
	cat("Pathway:", "\t@name", paste("\t\t",name(x)), "\t@geneProducts", sep = "\n")
	for(gp in x) cat("\t\t", name(gp), "\n");
})

## Other ##
createMethod("hasGeneProduct", c("Pathway", "GeneProduct"), function(x, gp, ...) {
	for(pgp in x) if(gp == pgp) return(TRUE)
	FALSE
})

createMethod("hasGeneProduct", c("Pathway", "list"), function(x, gp, ...) {
	sapply(gp, function(g) hasGeneProduct(x, g))
})
