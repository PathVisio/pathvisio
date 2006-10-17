#######################################################
################## Pathway class ######################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("Pathway", contains = "list",
	representation(
		name = "character",
		fileName = "character",
		allRefs = "character" #Vector conaining all references of the geneproducts in this pathway (for performance of match method)
	),
	prototype(
		name = character(),
		fileName = character(),
		allRefs = character()
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
		pw = new("Pathway", geneProducts, name = name, fileName = fileName)
		allRefs(pw) = getAllRefs(pw)
		pw
	}
)

######################
#### Functions	  ####
######################
getAllRefs = function(pw) {
	allRefs = character()
	for(gp in pw) allRefs = append(allRefs, rownames(gp)) ##TODO: filter out duplicates...
	allRefs
}

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "Pathway", function(x) x@name)
createMethod("fileName", "Pathway", function(x) x@fileName)
createMethod("allRefs", "Pathway", function(x) x@allRefs)

## Setters ##)
createReplaceMethod("name", "Pathway", function(x, value) {
	x@name = value
	x
})
createReplaceMethod("fileName", "Pathway", function(x, value) {
	x@fileName = value
	x
})
createReplaceMethod("allRefs", "Pathway", function(x, value) {
	x@allRefs = value
	x
})

## Generic and Primitive implementations ##
setMethod("print", "Pathway", function(x, ...) {
	cat("Pathway:", "\t@name", paste("\t\t",name(x)), "\t@geneProducts", sep = "\n")
	for(gp in x) cat("\t\t", name(gp), "\n");
})

createMethod("match", c(x = "ANY", table = "Pathway"), 
function(x, table, nomatch = NA, incomparables = FALSE) {
	pwrefs = allRefs(table)
	if(length(x) == 1)
		return(x %in% pwrefs)
	if(class(x) == "GeneProduct")
		for(ref in rownames(x)) if(ref %in% pwrefs) return(TRUE)
	FALSE
})
