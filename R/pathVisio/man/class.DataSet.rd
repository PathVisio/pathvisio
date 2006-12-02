\name{DataSet-class}
\docType{class}
\alias{DataSet-class}
\alias{class:DataSet}
\alias{DataSet}
\alias{name,DataSet-method}
\alias{name<-,DataSet-method}
\alias{reporters,DataSet-method}
\alias{reporters<-,DataSet-method}
\alias{samlpes,DataSet-method}
\alias{samples<-,DataSet-method}
\alias{print,DataSet-method}
\alias{dataByReporter,DataSet-method}

\title{Class "DataSet"}
\description{ This class represents experimental data exported from Path-Visio. Every row is a reporter (e.g. microarray probe) and every column is a sample (e.g. a disease stage). } 

\section{Extends}{
   Directly extends class \code{\link{matrix}}.
}

\section{Creating objects}{
Objects can be created by calls of the form 
\code{DataSet(name, reporters, data)} where\cr
\code{reporters} A vector with reporter names. In order to link the reporters to \code{\link{GeneProduct} objects, they need to be in the form \code{"code:id"}. If this argument is missing, the rownames of \code{data} will be taken as reporter names;\cr
\code{data} is a matrix with experimental data, where every row is a reporter (e.g. microarray probe) and every column is a sample (e.g. a disease stage);\cr
the other arguments are slot values
}

\section{Slots}{
  \describe{
    \item{name}{ the name of the dataset }
  }
}

\section{Methods}{
  \describe{
    \item{name}{
    	\code{signature(x = "DataSet")}:
    	accessor for the slot 'name'
    }
    \item{samples}{
    	\code{signature(x = "DataSet")}:
    	get the sample names of this dataset (equivalent to \code{colnames(x)})
    }
    \item{reporters}{
    	\code{signature(x = "DataSet")}:
    	get the reporter names of this dataset (equivalent to \code{rownames(x)})
    }
    \item{print}{
    	\code{signature(x = "DataSet")}:
    	prints this object in readable form
    }
   \item{print}{
    	\code{signature(obj = "DataSet", reporter = "character")}:
    	get all rows of data that belong to the given reporter
    }
   }
}
\author{Thomas Kelder (BiGCaT)}

\keyword{methods}
\keyword{classes} 
