require(pathVisio)

.zscore_impl = function(pathwaySet, dataSet, sets) {
	#Check arguments
	if(class(pathwaySet) != "PathwaySet")
		for(pw in pathwaySet) if(class(pw) != "Pathway") 
			stop("pathwaySet list contains non-Pathway elements")	
	if(class(dataSet) != "DataSet") 
		stop("dataSet argument is not of class 'DataSet'")
		
	if(is.null(dim(sets))) sets = cbind(sets)
	if(nrow(sets) != nrow(dataSet)) 
		stop("set argument should have the same length (or number of rows) as number of reporters in dataset")

	setnames = colnames(sets)
	if(is.null(setnames)) colnames(sets) = .setNames(sets, name(dataSet))
	
	#Progress reporting variables
	reporters = reporters(dataSet)	
	total = length(pathwaySet)
	current = 1
	
	dgenes = rownames(asEnsembl(dataSet))
	ugenes = unique(dgenes)
	
	pgenesTotal = asEnsembl(pathwaySet)
	
	ePws = lapply(pathwaySet, function(pw) { asEnsembl(pw) })
	nvalues = vector()
	for(i in 1:length(pathwaySet)) {
		nvalues[i] = .calcn(ePws[[i]], ugenes)
	}
	N = .calcN(ugenes, pgenesTotal)
	
	results = list()
	for(i in 1:ncol(sets)) {
		eset = asEnsembl(dataSet, sets[,i])
		uset = .uniqueSet(ugenes, dgenes, eset)
		
		R = .calcR(uset, ugenes, pgenesTotal)
		
		zscores = Pvalues = Evalues = rvalues = vector()
		for(j in 1:length(pathwaySet)) {
			pgenes = unique(ePws[[j]])
			rvalues[j] = .calcr(uset, ugenes, pgenes) ## genes in set and on pathway
			zscores[j] = .calcZ(N, R, nvalues[j], rvalues[j])
			Pvalues[j] = length(pathwaySet[[j]]) ##Original number of pathway genes
			Evalues[j] = length(pgenes) ##Number of ensembl genes on pathway
    }
    
    stats = cbind(Pvalues, Evalues, nvalues, rvalues, zscores)
    print(stats)
    colnames(stats) = c("Genes on pathway", "Ensembl on pathway", "n", "r", "zscore")
    
    globals = matrix(nrow = 5, ncol = 2)
    globals[1,] = c("Number of reporters measured", length(reporters))
    globals[2,] = c("Number ensembl genes measured", length(ugenes))
    globals[3,] = c("Number of reporters in set", sum(sets[,i]))
		globals[4,] = c("Measured gene-products on a pathway (N)", N)
    globals[5,] = c("Gene-products on a pathway and in set (R)", R)
    
		##Create a ResultSet to return
		results[[i]] = ResultSet(name = colnames(sets)[i], pathwaySet = pathwaySet, globals = globals, stats = stats)
	}
	if(length(results) == 1) return(results[[1]])
	else return(results)
}

.calcZ = function(N, R, n, r) {
    RoverN = R / N
	num = r - n*RoverN
	den = sqrt(n * RoverN * (1 - RoverN) * (1 - (n - 1)/(N - 1)))
    num/den
}

.calcN = function(ugenes, pgenesTotal) {
	pmatch = ugenes %in% pgenesTotal
	sum(pmatch)
}

.calcn = function(pgenes, ugenes) {
		measured = ugenes %in% pgenes
		sum(measured)
}

.calcR = function(uset, ugenes, pgenesTotal) {
	pmatch = ugenes[as.logical(uset)] %in% pgenesTotal
	sum(pmatch)
}

.calcr = function(uset, ugenes, pgenes) {
	pmatch = ugenes[as.logical(uset)] %in% pgenes
	sum(pmatch)
}
	
.uniqueSet = function(ugenes, egenes, set) {
	uset = ugenes
	for(i in 1:length(ugenes)) {
		m = grep(uset[i], egenes)
		uset[i] = sum(as.logical(set[m])) > 0
	}
	uset
}


zscore = VisioFunction(.zscore_impl,	name = "Z-score", 
	description = "Calculate z-score for the given pathways where 'sets' are one or more vectors which specify for each reporter whether it meets a criterion or not", 
	arg_descr = c(	"The pathways to calculate the z-scores for (instance of class 'PathwaySet')",
			"The measured data (instance of class 'DataSet')",
			"A logical vector that specifies for each reporter whether it meets a criterion or not, or a matrix (where the columns is a criterion)"),
	arg_class = c("PathwaySet", "DataSet", "matrix")
)