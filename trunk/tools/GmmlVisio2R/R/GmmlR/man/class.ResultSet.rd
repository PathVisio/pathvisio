\name{ResultSet-class}
\docType{class}
\alias{ResultSet-class}
\alias{class:ResultSet}
\alias{ResultSet}
\alias{name,ResultSet-method}
\alias{fileNames,ResultSet-method}
\alias{pathways,ResultSet-method}

\title{Class "ResultSet"}
\description{ This class represents a result of a statistical test on a \code{\link{PathwaySet} in a way that the results can be displayed in Gmml-Visio} 

\section{Extends}{
   Directly extends class \code{\link{matrix}}.
}

\section{Creating objects}{
Objects can be created by calls of the form 
\code{ResultSet(name, pathwaySet, stats)} where\cr
\code{pathwaySet} is an object of class \code{\link{PathwaySet}};\cr
\code{stats} is a matrix with an equal number of rows as the length of \code{pathwaySet} and an arbitrary number of columns, containing results of a statistical test on the pathways in \code{pathwaySet}\cr
the other arguments are slot values
}

\section{Slots}{
  \describe{
    \item{name}{ the name of the resultset }
  }
}

\section{Methods}{
  \describe{
    \item{name}{
    	\code{signature(x = "ResultSet")}:
    	accessor for the slot 'name'
    }
    \item{fileNames}{
    	\code{signature(x = "ResultSet")}:
    	get the fileNames of the pathways in this resultset (NULL if not available)
    }
    \item{pathways}{
    	\code{signature(x = "ResultSet")}:
    	get the names of the pathways in this resultset (NULL if not available)
    }
   }
}
\author{Thomas Kelder (BiGCaT)}

\keyword{methods}
\keyword{classes} 
