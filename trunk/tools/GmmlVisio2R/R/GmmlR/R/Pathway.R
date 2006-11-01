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

createMethod("hasReference", c(ref = "ANY", pathway = "Pathway"), 
function(ref, pathway) {
	pwrefs = allRefs(pathway)
	if(class(ref) == "GeneProduct") {
		for(gpref in rownames(ref)) if(gpref %in% pwrefs) return(TRUE)
		return(FALSE)
	}
	else
		return(ref %in% pwrefs)
})

createMethod("matchReferences", c(refs = "ANY", pathway = "Pathway"), 
function(refs, pathway) {
	pwrefs = allRefs(pathway)
	if(class(refs) == "character") {
		return(refs %in% pwrefs)
	}
	else {
		return(sapply(refs, function(x) hasReference(x, pathway)))	
	}
})

createMethod("inReferences", c(pathway = "Pathway", refs = "ANY"), 
function(pathway, refs) {
	refMatch = matchReferences(refs, pathway)
	inPathway = refs[refMatch]
	pwrefs = allRefs(pathway)
	sapply(pathway, function(gp) {
		b = FALSE
		for(ref in inPathway) {
			if(ref == gp) {
				b = TRUE 
				break
			}
		}
		b
	})
})




