#######################################################
################## Pathway class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("Pathway", 
	representation(
		name = "character",
		fileName = "character",
		geneProducts = "list"
	),
	prototype = list(
		fileName = character(),
		geneProducts = list()
	)
)

## Validity:
## 1: all elements in @geneProducts must be instance of class GeneProduct
setValidity("Pathway", function(x) {
	for(gp in geneProducts(x))
		if(!isClass("GeneProduct", gp)) return("list @geneProduct contains non-GeneProduct objects")
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("Pathway", 
	function(name, fileName = "", geneProducts = list()) {
		new("Pathway", name = name, fileName = fileName, geneProducts = geneProducts)
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "Pathway", function(x) x@name)
createMethod("fileName", "Pathway", function(x) x@fileName)
createMethod("geneProducts", "Pathway", function(x) x@geneProducts)

## Setters ##)
createReplaceMethod("name", "Pathway", function(x, value) {
	x@name = value
	x
})
createReplaceMethod("fileName", "Pathway", function(x, value) {
	x@fileName = value
	x
})
createReplaceMethod("geneProducts", "Pathway", function(x, value) {
	x@geneProducts = value
	if(!validObject(x)) stop("given GeneProducts list is not valid (check length and class of elements)")
	x
})

## Generic and Primitive implementations ##
createMethod("print", "Pathway", function(x, ...) {
	cat("Pathway:", "\t@name", paste("\t\t",name(x)), "\t@geneProducts", sep = "\n")
	for(gp in geneProducts(x)) cat("\t\t", name(gp), "\n");
})

createMethod("as.list", "Pathway", function(x, ...) { geneProducts(x) })

## Other ##
## Check for every GeneProduct in the list gps whether it is 
## in the pathway or not
## Returns a logical vector of the same length as gps
createMethod("hasGeneProduct", c("Pathway", "GeneProduct"), function(x, gps, ...) {
	present = logical(length(gps))
	pgps = geneProducts(x)
	for(i in 1:length(gps))
		for(gp in pgps) {
			bool = gp == gps[[i]]
			if(gp == gps[[i]]) {
				present[i] = TRUE
				break()
			}
		}
	present
})
			
